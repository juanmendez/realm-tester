package info.juanmendez.mock.realm.factories;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.concurrent.Callable;

import info.juanmendez.mock.realm.dependencies.RealmMatchers;
import info.juanmendez.mock.realm.dependencies.RealmStorage;
import info.juanmendez.mock.realm.models.QueryWatch;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmFactory {

    private static Scheduler observerScheduler = Schedulers.immediate();
    private static Scheduler subscriberScheduler = Schedulers.immediate();

    public static Realm create() throws Exception {
        Realm realm = mock(Realm.class );
        prepare(realm);
        prepareTransactions(realm);
        return  realm;
    }

    public static void setTransactionScheduler(Scheduler observerScheduler) {
        RealmFactory.observerScheduler = observerScheduler;
    }

    public static void setResponseScheduler(Scheduler subscriberScheduler) {
        RealmFactory.subscriberScheduler = subscriberScheduler;
    }

    private static void prepare(Realm realm) throws Exception {

        HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
        HashMap<Class, QueryWatch> queryMap = RealmStorage.getQueryMap();

        when(Realm.getDefaultInstance()).thenReturn(realm);

        when( realm.createObject( Mockito.argThat(new RealmMatchers.ClassMatcher<>(RealmModel.class)) ) ).thenAnswer(new Answer<RealmModel>(){

            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class clazz = (Class) invocationOnMock.getArguments()[0];
                HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
                if( !realmMap.containsKey(clazz)){
                    realmMap.put(clazz, new RealmList<>());
                }

                Constructor constructor = clazz.getConstructor();
                RealmModel realmModel = (RealmModel) constructor.newInstance();

                realmMap.get(clazz).add( realmModel);

                return realmModel;
            }
        });


        when( realm.copyToRealm(Mockito.any( RealmModel.class ))).thenAnswer( new Answer<RealmModel>(){

            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {

                if( invocationOnMock.getArguments().length > 0 ){
                    RealmModel realmModel = (RealmModel) invocationOnMock.getArguments()[0];
                    Class clazz = realmModel.getClass();
                    HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();

                    if( !realmMap.containsKey(clazz)){
                        realmMap.put(clazz, new RealmList<>());
                    }

                    realmMap.get( clazz ).add( realmModel );
                    return realmModel;
                }

                return null;
            }
        });

         when( realm.where( Mockito.argThat( new RealmMatchers.ClassMatcher<>(RealmModel.class))  ) ).then(new Answer<RealmQuery>(){

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                //clear list being queried
                Class clazz = (Class) invocationOnMock.getArguments()[0];

                QueryWatch queryWatch = new QueryWatch();
                queryWatch.onTopGroupBegin( realmMap.get(clazz));
                queryMap.put(clazz, queryWatch );

                return QueryFactory.create( clazz );
            }
        });
    }

    private static void prepareTransactions( Realm realm ){

        //call execute() in Realm.Transaction object received.
        doAnswer( new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length > 0 ){
                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                    transaction.execute( realm );
                }
                return null;
            }
        }).when( realm ).executeTransaction(any( Realm.Transaction.class ));

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length > 0 ){

                    Observable.fromCallable(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                            transaction.execute( realm );
                            return null;
                        }
                    }).subscribeOn( observerScheduler ).subscribe(aVoid -> {});

                }
                return null;
            }
        }).when( realm ).executeTransactionAsync(any( Realm.Transaction.class ));


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any( Realm.Transaction.OnSuccess.class ))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {

                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        Subscription subscription = Observable.fromCallable(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                if( invocation.getArguments().length >=1 ){
                                    System.out.println( "running on " + Thread.currentThread().getName() );
                                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                                    transaction.execute( realm );

                                    return true;
                                }

                                return false;
                            }
                        })
                        .subscribeOn(observerScheduler)
                        .observeOn( subscriberScheduler )
                        .subscribe(aBoolean -> {
                            if(  aBoolean && invocation.getArguments().length >=2 ){
                                Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                onSuccess.onSuccess();
                            }

                        });

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {
                                subscription.unsubscribe();
                            }

                            @Override
                            public boolean isCancelled() {return subscription.isUnsubscribed();}
                        };
                    }
                }
        );


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any( Realm.Transaction.OnSuccess.class ), any(Realm.Transaction.OnError.class))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {

                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        Subscription subscription = Observable.fromCallable(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                if( invocation.getArguments().length >=1 ){
                                    System.out.println( "running on " + Thread.currentThread().getName() );
                                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                                    transaction.execute( realm );

                                    return true;
                                }

                                return false;
                            }
                        })
                        .subscribeOn(observerScheduler)
                        .observeOn( subscriberScheduler )
                        .subscribe(aBoolean -> {
                            if(  aBoolean && invocation.getArguments().length >=2 ){
                                Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                onSuccess.onSuccess();
                            }

                        }, throwable -> {

                            if( invocation.getArguments().length >=3 ){
                                Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                onError.onError(throwable);
                            }

                        });

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {
                                subscription.unsubscribe();
                            }

                            @Override
                            public boolean isCancelled() {return subscription.isUnsubscribed();}
                        };
                    }
                }
        );


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any(Realm.Transaction.OnError.class))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {

                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        Subscription subscription = Observable.fromCallable(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                if( invocation.getArguments().length >=1 ){
                                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                                    transaction.execute( realm );

                                    return true;
                                }

                                return false;
                            }
                        })
                        .subscribeOn(observerScheduler)
                        .observeOn( subscriberScheduler )
                        .subscribe(aBoolean -> {
                            if(  aBoolean && invocation.getArguments().length >=2 ){
                                Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                onSuccess.onSuccess();
                            }

                        }, throwable -> {

                            if( invocation.getArguments().length >=2 ){
                                Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                onError.onError(throwable);
                            }

                        });

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {
                                subscription.unsubscribe();
                            }

                            @Override
                            public boolean isCancelled() {return subscription.isUnsubscribed();}
                        };
                    }
                }
        );
    }
}