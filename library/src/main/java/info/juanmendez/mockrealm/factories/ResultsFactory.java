package info.juanmendez.mockrealm.factories;

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
public class ResultsFactory {

    public static RealmResults create( QueryNest queryNest){

        RealmList<RealmModel> results = queryNest.getQueryList();
        RealmResults mockedResults = PowerMockito.mock( RealmResults.class );

        doAnswer(positionInvokation -> {
            int position = (int) positionInvokation.getArguments()[0];
            return results.get( position );
        }).when( mockedResults).get(anyInt());

        doReturn( results.size() ).when( mockedResults ).size();
        doReturn( results.iterator() ).when( mockedResults ).iterator();

        doAnswer(new Answer<RealmObject>() {
            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                RealmObject value = (RealmObject) invocationOnMock.getArguments()[0];
                results.set(index, value);
                return value;
            }
        }).when( mockedResults ).set(anyInt(), any(RealmObject.class) );


        doAnswer(new Answer<RealmModel>() {
            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                return results.get(index);
            }
        }).when( mockedResults ).listIterator(anyInt());

        doAnswer(new Answer<RealmQuery>() {

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                QueryNest resultsQueryNest = queryNest.clone();
                resultsQueryNest.appendQuery( new Query(Compare.startTopGroup, new Object[]{results}));

                RealmQuery realmQuery = QueryFactory.create(resultsQueryNest);
                RealmStorage.getQueryMap().put(realmQuery, resultsQueryNest);

                return realmQuery;
            }
        }).when(mockedResults).where();

        handleDeleteMethods( mockedResults, results );
        handleMathMethods( mockedResults, results );
        return mockedResults;
    }


    /**
     * TODO: handlers for deleting methods is not yet tested, and neither used yet
     */
    private static void handleDeleteMethods( RealmResults mockedResults, RealmList<RealmModel> list ){


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteAllFromRealm();
            }
        }).when(mockedResults).deleteAllFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteFirstFromRealm();
            }
        }).when( mockedResults  ).deleteFirstFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return list.deleteLastFromRealm();
            }
        }).when( mockedResults ).deleteLastFromRealm();


        doAnswer( new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                int position = (int) invocation.getArguments()[0];
                list.deleteFromRealm( position );

                return null;
            }
        }).when( mockedResults ).deleteFromRealm( anyInt() );
    }

    private static void handleMathMethods( RealmResults mockedResults, RealmList<RealmModel> list ){

        when( mockedResults.sum( anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.sum( fieldName );
                }

                return  null;
            }
        });

        when( mockedResults.average(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.average(fieldName);
                }

                return  null;
            }
        });


        when( mockedResults.max(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    String fieldName = (String) invocation.getArguments()[0];
                    return list.max(fieldName);
                }
                return  null;
            }
        });

        when( mockedResults.min(anyString()) ).thenAnswer(new Answer<Number>() {
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