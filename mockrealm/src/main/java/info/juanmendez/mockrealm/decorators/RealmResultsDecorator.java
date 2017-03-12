package info.juanmendez.mockrealm.decorators;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.Query;
import info.juanmendez.mockrealm.models.QueryNest;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmResultsDecorator {

    public static RealmResults create( QueryNest queryNest){
        return create( queryNest, PowerMockito.mock( RealmResults.class ) );
    }

    public static RealmResults create( QueryNest queryNest, RealmResults realmResults ){

        RealmList<RealmModel> results = queryNest.getQueryList();

        doAnswer(positionInvokation -> {
            int position = (int) positionInvokation.getArguments()[0];
            return results.get( position );
        }).when( realmResults).get(anyInt());

        doReturn( results.size() ).when( realmResults ).size();

        doReturn( results.iterator() ).when( realmResults ).iterator();

        doAnswer(new Answer<RealmObject>() {
            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                RealmObject value = (RealmObject) invocationOnMock.getArguments()[0];
                results.set(index, value);
                return value;
            }
        }).when( realmResults ).set(anyInt(), any(RealmObject.class) );

        doAnswer(new Answer<RealmModel>() {
            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                return results.get(index);
            }
        }).when( realmResults ).listIterator(anyInt());

        doAnswer(new Answer<RealmQuery>() {

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                QueryNest resultsQueryNest = queryNest.clone();
                resultsQueryNest.appendQuery( new Query(Compare.startTopGroup, new Object[]{results}));

                RealmQuery realmQuery = RealmQueryDecorator.create(resultsQueryNest);
                RealmStorage.getQueryMap().put(realmQuery, resultsQueryNest);

                return realmQuery;
            }
        }).when(realmResults).where();

        handleDeleteMethods( realmResults, results );
        handleMathMethods( realmResults, results );

        return realmResults;
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
}