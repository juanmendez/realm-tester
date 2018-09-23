package info.juanmendez.realmtester.decorators;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.concurrent.Callable;

import info.juanmendez.realmtester.demo.models.Query;
import info.juanmendez.realmtester.demo.models.TransactionEvent;
import info.juanmendez.realmtester.dependencies.Compare;
import info.juanmendez.realmtester.dependencies.RealmMatchers;
import info.juanmendez.realmtester.dependencies.RealmStorage;
import info.juanmendez.realmtester.dependencies.TransactionObservable;
import info.juanmendez.realmtester.utils.QueryTracker;
import info.juanmendez.realmtester.utils.RealmModelUtil;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class RealmDecorator {

    /**
     * Only stick to Schedulers.immediate() for now. I tried others but they don't seem to work well
     * in Robolectric
     */
    private static Scheduler observerScheduler = Schedulers.immediate();
    private static Scheduler subscriberScheduler = Schedulers.immediate();

    public static Realm prepare() throws Exception {

        Realm realm = mock(Realm.class);
        prepare(realm);
        handleAsyncTransactions(realm);
        handleSyncTransactions(realm);
        return realm;
    }

    public static Scheduler getTransactionScheduler() {
        return observerScheduler;
    }

    public static Scheduler getResponseScheduler() {
        return subscriberScheduler;
    }

    private static void prepare(Realm realm) throws Exception {

        doNothing().when(Realm.class, "init", any());

        when(Realm.deleteRealm(any(RealmConfiguration.class))).thenReturn(true);

        HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();

        when(Realm.getDefaultInstance()).thenReturn(realm);

        when(Realm.deleteRealm(any(RealmConfiguration.class))).thenAnswer(invocation -> {
            RealmStorage.clear();
            return null;
        });

        doAnswer((Answer<Void>) invocation -> null)
                .when(Realm.class, "setDefaultConfiguration", any());

        when(realm.createObject(Mockito.argThat(
                new RealmMatchers.ClassMatcher<>(RealmModel.class))))
                .thenAnswer(invocation -> {
                        Class clazz = (Class) invocation.getArguments()[0];

                        if (!realmMap.containsKey(clazz)) {
                            realmMap.put(clazz, RealmListDecorator.create());
                        }

                        RealmModel realmModel = RealmModelDecorator.create(clazz, true);
                        RealmStorage.addModel(realmModel);

                        return realmModel;
                });

        //realm.copyToRealm( realmModel )
        when(realm.copyToRealm(Mockito.any(RealmModel.class)))
                .thenAnswer((Answer<RealmModel>) invocationOnMock -> {
                    RealmModel newRealmModel = (RealmModel) invocationOnMock.getArguments()[0];
                    return createOrUpdate(newRealmModel);
                });

        //realm.copyToRealmOrUpdate( realmModel ) same as realm.copyToRealm( realmModel )
        when(realm.copyToRealmOrUpdate(Mockito.any(RealmModel.class)))
                .thenAnswer((Answer<RealmModel>) invocationOnMock -> {
                    RealmModel newRealmModel = (RealmModel) invocationOnMock.getArguments()[0];
                    return createOrUpdate(newRealmModel);
                });

        when(realm.where(Mockito.argThat(
                new RealmMatchers.ClassMatcher<>(RealmModel.class))))
                .then((Answer<RealmQuery>) invocationOnMock -> {
                    //clear list being queried
                    Class clazz = (Class) invocationOnMock.getArguments()[0];
                    QueryTracker queryTracker = new QueryTracker(clazz);

                    RealmQuery realmQuery = RealmQueryDecorator.create(queryTracker);

                    if (!realmMap.containsKey(clazz)) {
                        realmMap.put(clazz, new RealmList<>());
                    }

                    queryTracker.appendQuery(
                            Query.build()
                                    .setCondition(Compare.startTopGroup)
                                    .setArgs(new Object[]{realmMap.get(clazz)})
                    );

                    return realmQuery;
                });
    }

    private static RealmModel createOrUpdate(RealmModel newRealmModel) {
        HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
        Class clazz = RealmModelUtil.getClass(newRealmModel);

        if (!realmMap.containsKey(clazz)) {
            realmMap.put(clazz, RealmListDecorator.create());
        }

        RealmModel updatedRealmModel = RealmModelUtil.tryToUpdate(newRealmModel);

        if (updatedRealmModel != null) {
            return updatedRealmModel;
        }

        newRealmModel = RealmModelDecorator.decorate(newRealmModel);
        RealmStorage.addModel(newRealmModel);
        return newRealmModel;
    }

    private static void handleAsyncTransactions(Realm realm) {

        //call execute() in Realm.Transaction object received.
        doAnswer((Answer<Void>) invocation -> {

            if (invocation.getArguments().length > 0) {
                Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];

                queueTransaction(() -> {
                    transaction.execute(realm);
                    return null;
                });
            }
            return null;
        }).when(realm).executeTransaction(any(Realm.Transaction.class));

        doAnswer((Answer<Void>) invocation -> {

            if (invocation.getArguments().length > 0) {

                queueTransaction(() -> {
                    Observable.fromCallable((Callable<Void>) () -> {
                        Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                        transaction.execute(realm);
                        return null;
                    })
                            .subscribeOn(getTransactionScheduler())
                            .observeOn(getResponseScheduler()).subscribe(aVoid -> {
                    });

                    return null;
                });

            }

            return null;
        }).when(realm).executeTransactionAsync(any(Realm.Transaction.class));


        when(realm.executeTransactionAsync(any(Realm.Transaction.class), any(Realm.Transaction.OnSuccess.class))).thenAnswer(
                (Answer<RealmAsyncTask>) invocation -> {

                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];

                    return queueTransaction(() -> {

                        Observable.fromCallable(() -> {
                            if (invocation.getArguments().length >= 1) {
                                transaction.execute(realm);
                                return true;
                            }
                            return false;
                        })
                                .subscribeOn(getTransactionScheduler())
                                .observeOn(getResponseScheduler())
                                .subscribe(aBoolean -> {
                                    if (aBoolean && invocation.getArguments().length >= 2) {
                                        Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                        onSuccess.onSuccess();
                                    }
                                });

                        return null;
                    });
                }
        );


        when(realm.executeTransactionAsync(any(Realm.Transaction.class), any(Realm.Transaction.OnSuccess.class), any(Realm.Transaction.OnError.class))).thenAnswer(
                (Answer<RealmAsyncTask>) invocation -> queueTransaction(() -> {
                    Observable.fromCallable(() -> {
                        if (invocation.getArguments().length >= 1) {
                            Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                            transaction.execute(realm);
                            return true;
                        }

                        return false;
                    })
                            .subscribeOn(observerScheduler)
                            .observeOn(subscriberScheduler)
                            .subscribe(aBoolean -> {
                                if (aBoolean && invocation.getArguments().length >= 2) {
                                    Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                    onSuccess.onSuccess();
                                }

                            }, throwable -> {

                                if (invocation.getArguments().length >= 3) {
                                    Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                    onError.onError(throwable);
                                }

                            });

                    return null;
                })
        );


        when(realm.executeTransactionAsync(any(Realm.Transaction.class), any(Realm.Transaction.OnError.class))).thenAnswer(
                (Answer<RealmAsyncTask>) invocation -> {
                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];

                    return queueTransaction(() -> {

                        Observable.fromCallable(() -> {
                            if (invocation.getArguments().length >= 1) {
                                return true;
                            }

                            return false;
                        })
                                .subscribeOn(observerScheduler)
                                .observeOn(subscriberScheduler)
                                .subscribe(aBoolean -> {
                                    if (aBoolean && invocation.getArguments().length >= 2) {
                                        Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                        onSuccess.onSuccess();
                                    }

                                }, throwable -> {

                                    if (invocation.getArguments().length >= 2) {
                                        Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                        onError.onError(throwable);
                                    }

                                });

                        return null;
                    });
                }
        );
    }

    private static void handleSyncTransactions(Realm realm) {

        TransactionObservable.KeyTransaction transaction = new TransactionObservable.KeyTransaction(realm.toString());

        doAnswer(invocation -> {
            TransactionObservable.startRequest(transaction);
            return null;
        }).when(realm).beginTransaction();


        doAnswer(invocation -> {
            TransactionObservable.endRequest(transaction);
            return null;
        }).when(realm).commitTransaction();
    }

    /**
     * Parameter funk serves as a key to TransactionObservable.startRequest()
     *
     * @param funk code to execute when TransactionObservable allows it.
     * @return RealmAsyncTask uses funk key to cancel transaction, or check if it has been canceled
     */
    private static RealmAsyncTask queueTransaction(Func0 funk) {

        TransactionObservable.startRequest(funk,
                TransactionObservable.asObservable()
                        .filter(transactionEvent ->
                                transactionEvent.getState() == TransactionEvent.START_TRANSACTION
                                        && transactionEvent.getTarget() == funk)
                        .subscribe(o -> {
                            funk.call();
                            TransactionObservable.endRequest(funk);
                        })
        );

        return new RealmAsyncTask() {
            @Override
            public void cancel() {
                TransactionObservable.cancel(funk);
            }

            @Override
            public boolean isCancelled() {
                return TransactionObservable.isCanceled(funk);
            }
        };
    }
}