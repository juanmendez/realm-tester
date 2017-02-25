package info.juanmendez.mock.realm.factories;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import info.juanmendez.mock.realm.dependencies.RealmStorage;
import info.juanmendez.mock.realm.models.QueryWatch;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class ResultsFactory {

    public static RealmResults create( Class clazz ){

        QueryWatch queryWatch = RealmStorage.getQueryMap().get(clazz);
        RealmList<RealmModel> results = queryWatch.getQueryList();
        RealmResults mockedResults = PowerMockito.mock( RealmResults.class );

        when( mockedResults.get(anyInt())).thenAnswer(positionInvokation -> {
            int position = (int) positionInvokation.getArguments()[0];
            return results.get( position );
        });

        when( mockedResults.size() ).thenReturn( results.size() );
        when( mockedResults.iterator() ).thenReturn( results.iterator() );
        when( mockedResults.set(anyInt(), any(RealmObject.class)) ).thenAnswer(new Answer<RealmObject>() {
            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                RealmObject value = (RealmObject) invocationOnMock.getArguments()[0];
                results.set(index, value);
                return value;
            }
        });

        when( mockedResults.listIterator() ).thenReturn( results.listIterator() );

        when( mockedResults.listIterator(anyInt()) ).thenAnswer(new Answer<RealmModel>() {
            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                int index = (int) invocationOnMock.getArguments()[0];
                return results.get(index);
            }
        });

        when( mockedResults.where() ).then(new Answer<RealmQuery>(){

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                QueryWatch queryWatch = new QueryWatch();
                queryWatch.onTopGroupBegin(results);
                RealmStorage.getQueryMap().put( clazz, queryWatch );

                return QueryFactory.create( clazz );
            }
        });

        return mockedResults;
    }
}