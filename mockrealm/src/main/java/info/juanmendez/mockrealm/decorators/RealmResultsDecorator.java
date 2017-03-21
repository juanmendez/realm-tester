package info.juanmendez.mockrealm.decorators;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.dependencies.TransactionObservable;
import info.juanmendez.mockrealm.models.Query;
import info.juanmendez.mockrealm.models.TransactionEvent;
import info.juanmendez.mockrealm.utils.QueryTracker;
import info.juanmendez.mockrealm.utils.RealmModelUtil;
import info.juanmendez.mockrealm.utils.SubscriptionsUtil;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmResultsDecorator {

    private static SubscriptionsUtil<RealmResults, RealmChangeListener> subscriptionsUtil = new SubscriptionsUtil<>();

    public static RealmResults create(QueryTracker queryTracker ){

        RealmResults realmResults = queryTracker.getRealmResults();
        RealmList<RealmModel> results = queryTracker.getQueryList();

        doAnswer(new Answer<RealmQuery>() {

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                QueryTracker resultsQueryTracker = queryTracker.clone();
                resultsQueryTracker.appendQuery( new Query(Compare.startTopGroup, new Object[]{results}));

                RealmQuery realmQuery = RealmQueryDecorator.create(resultsQueryTracker);

                return realmQuery;
            }
        }).when(realmResults).where();

        handleBasicActions( realmResults, results );
        handleDeleteMethods( realmResults, results );
        handleMathMethods( realmResults, results );
        handleAsyncMethods( queryTracker );

        return realmResults;
    }

    private static void handleBasicActions( RealmResults realmResults, RealmList<RealmModel> list){

        doAnswer(positionInvokation -> {
            int position = (int) positionInvokation.getArguments()[0];
            return list.get( position );
        }).when( realmResults).get(anyInt());

        doAnswer(invocation -> {
            return list.size();
        }).when( realmResults ).size();

        doAnswer(invocation -> {
            return list.isEmpty();
        }).when( realmResults ).isEmpty();

        doAnswer(invocation -> {
            return list.iterator();
        }).when( realmResults ).iterator();


        doAnswer(new Answer<RealmObject>() {
            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                RealmObject value = (RealmObject) invocationOnMock.getArguments()[0];
                list.set(index, value);
                return value;
            }
        }).when( realmResults ).set(anyInt(), any(RealmObject.class) );

        doAnswer(new Answer<RealmModel>() {
            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                return list.get(index);
            }
        }).when( realmResults ).listIterator(anyInt());

    }

    private static void handleDeleteMethods( RealmResults realmResults, RealmList<RealmModel> list ){


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteAllFromRealm();
            }
        }).when(realmResults).deleteAllFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteFirstFromRealm();
            }
        }).when( realmResults  ).deleteFirstFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteLastFromRealm();
            }
        }).when( realmResults ).deleteLastFromRealm();


        doAnswer( new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                int position = (int) invocation.getArguments()[0];
                list.deleteFromRealm( position );

                return null;
            }
        }).when( realmResults ).deleteFromRealm( anyInt() );
    }

    private static void handleMathMethods( RealmResults realmResults, RealmList<RealmModel> list ){

        when( realmResults.sum( anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.sum( fieldName );
                }

                return  null;
            }
        });

        when( realmResults.average(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.average(fieldName);
                }

                return  null;
            }
        });


        when( realmResults.max(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.max(fieldName);
                }
                return  null;
            }
        });

        when( realmResults.min(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.min(fieldName);
                }
                return  null;
            }
        });
    }

    private static void handleAsyncMethods( QueryTracker queryTracker){

        RealmResults realmResults = queryTracker.getRealmResults();

        doAnswer(invocation -> {

            //execute query once associated
            RealmChangeListener listener = (RealmChangeListener) invocation.getArguments()[0];
            Observable.fromCallable(() -> queryTracker.rewind())
                    .subscribeOn(RealmDecorator.getTransactionScheduler())
                    .observeOn( RealmDecorator.getResponseScheduler() )
                    .subscribe(results -> {
                        listener.onChange( results );
            });


            final String[] json = new String[2];

            //whenever there is a transaction ending, we compare previous result with current one.
            //we transform both results as json objects and just do a check if strings are not the same
            subscriptionsUtil.add( realmResults,
                    listener,
                    TransactionObservable.asObservable()
                            .subscribe( transactionEvent -> {

                                if( transactionEvent.getState() == TransactionEvent.END_TRANSACTION ){

                                    String initialJson = "", currrentJson = "";

                                    RealmResults<RealmModel> results = queryTracker.getRealmResults();
                                    initialJson = RealmModelUtil.toString( results );

                                    results = queryTracker.rewind();
                                    currrentJson = RealmModelUtil.toString( results );

                                    if( !initialJson.equals( currrentJson )){
                                        listener.onChange( results );
                                    }
                                }
                            })
            );

            return null;
        }).when( realmResults ).addChangeListener(any(RealmChangeListener.class));


        doAnswer(invocation -> {
            RealmChangeListener listener = (RealmChangeListener) invocation.getArguments()[0];
            subscriptionsUtil.remove(listener);
            return null;
        }).when( realmResults ).removeChangeListener( any(RealmChangeListener.class));


        doAnswer(invocation -> {
            subscriptionsUtil.removeAll( realmResults );
            return null;
        }).when( realmResults ).removeChangeListeners();


        doAnswer(invocation -> {
            BehaviorSubject<RealmResults> subject = BehaviorSubject.create();

            subject.subscribeOn(RealmDecorator.getTransactionScheduler())
                    .observeOn( RealmDecorator.getResponseScheduler() );

            //first time make a call!
            Observable.fromCallable(() -> queryTracker.rewind())
                    .subscribeOn(RealmDecorator.getTransactionScheduler())
                    .observeOn( RealmDecorator.getResponseScheduler() )
                    .subscribe(results -> {
                        subject.onNext( results );
                    });

            TransactionObservable.asObservable()
                    .subscribe(transactionEvent -> {

                if( transactionEvent.getState() == TransactionEvent.END_TRANSACTION ){
                    String initialJson = "", currrentJson = "";

                    RealmResults<RealmModel> results = queryTracker.getRealmResults();

                    initialJson = RealmModelUtil.toString( results );

                    results = queryTracker.rewind();
                    currrentJson = RealmModelUtil.toString( results );

                    if( !initialJson.equals( currrentJson )){
                        subject.onNext( results );
                    }
                }
            });

            return subject;
        }).when( realmResults ).asObservable();
    }
}