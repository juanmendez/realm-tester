package info.juanmendez.mock.realm.factories;

import info.juanmendez.mock.realm.dependencies.RealmMatchers;
import info.juanmendez.mock.realm.dependencies.RealmStorage;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmFactory {

    public static Realm create(){
        Realm realm = PowerMockito.mock(Realm.class );
        prepare(realm);
        return  realm;
    }

    private static void prepare(Realm realm){

        when( Realm.getDefaultInstance() ).thenReturn( realm );

        when( realm.createObject( Mockito.argThat(new RealmMatchers.ClassMatcher<>(RealmObject.class)) ) ).thenAnswer(new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class clazz = (Class) invocationOnMock.getArguments()[0];
                HashMap<Class, ArrayList<RealmObject>> realmMap = RealmStorage.getRealmMap()
                        ;
                if( !realmMap.containsKey(clazz)){
                    realmMap.put(clazz, new ArrayList<>());
                }

                Constructor constructor = clazz.getConstructor();
                RealmObject realmObject = (RealmObject) constructor.newInstance();

                realmMap.get(clazz).add( realmObject);

                return realmObject;
            }
        });

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


        when( realm.copyToRealm(Mockito.any( RealmObject.class ))).thenAnswer( new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {

                if( invocationOnMock.getArguments().length > 0 ){
                    RealmObject realmObject = (RealmObject) invocationOnMock.getArguments()[0];
                    Class clazz = realmObject.getClass();
                    HashMap<Class, ArrayList<RealmObject>> realmMap = RealmStorage.getRealmMap();

                    if( !realmMap.containsKey(clazz)){
                        realmMap.put(clazz, new ArrayList<>());
                    }

                    realmMap.get( clazz ).add( realmObject );
                    return realmObject;
                }

                return null;
            }
        });

         when( realm.where( Mockito.argThat( new RealmMatchers.ClassMatcher<>(RealmObject.class))  ) ).then(new Answer<RealmQuery>(){

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                //clear list being queried
                Class clazz = (Class) invocationOnMock.getArguments()[0];
                return QueryFactory.create( clazz );
            }
        });
    }
}