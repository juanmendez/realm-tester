package info.juanmendez.mock.realm.factories;

import info.juanmendez.mock.realm.dependencies.RealmMatchers;
import info.juanmendez.mock.realm.dependencies.RealmStorage;
import io.realm.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.lang.reflect.Constructor;
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
        prepareTransactions(realm);
        return  realm;
    }

    private static void prepare(Realm realm){

        when( Realm.getDefaultInstance() ).thenReturn( realm );

        when( realm.createObject( Mockito.argThat(new RealmMatchers.ClassMatcher<>(RealmModel.class)) ) ).thenAnswer(new Answer<RealmModel>(){

            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class clazz = (Class) invocationOnMock.getArguments()[0];
                HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
                if( !realmMap.containsKey(clazz)){
                    realmMap.put(clazz, new RealmList<>());
                }

                Constructor constructor = clazz.getConstructor();
                RealmModel RealmModel = (RealmModel) constructor.newInstance();

                realmMap.get(clazz).add( RealmModel);

                return RealmModel;
            }
        });


        when( realm.copyToRealm(Mockito.any( RealmModel.class ))).thenAnswer( new Answer<RealmModel>(){

            @Override
            public RealmModel answer(InvocationOnMock invocationOnMock) throws Throwable {

                if( invocationOnMock.getArguments().length > 0 ){
                    RealmModel RealmModel = (RealmModel) invocationOnMock.getArguments()[0];
                    Class clazz = RealmModel.getClass();
                    HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();

                    if( !realmMap.containsKey(clazz)){
                        realmMap.put(clazz, new RealmList<>());
                    }

                    realmMap.get( clazz ).add( RealmModel );
                    return RealmModel;
                }

                return null;
            }
        });

         when( realm.where( Mockito.argThat( new RealmMatchers.ClassMatcher<>(RealmModel.class))  ) ).then(new Answer<RealmQuery>(){

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                //clear list being queried
                Class clazz = (Class) invocationOnMock.getArguments()[0];
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
                    Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                    transaction.execute( realm );
                }
                return null;
            }
        }).when( realm ).executeTransactionAsync(any( Realm.Transaction.class ));


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any( Realm.Transaction.OnSuccess.class ))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {


                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        if( invocation.getArguments().length >=1 ){
                            Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];
                            transaction.execute( realm );
                        }

                        if( invocation.getArguments().length >=2 ){
                            Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                            onSuccess.onSuccess();
                        }

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {}

                            @Override
                            public boolean isCancelled() {return false;}
                        };
                    }
                }
        );


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any( Realm.Transaction.OnSuccess.class ), any(Realm.Transaction.OnError.class))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {


                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        if( invocation.getArguments().length >=1 ){
                            Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];

                            try{
                                transaction.execute( realm );

                                if( invocation.getArguments().length >=2 ){
                                    Realm.Transaction.OnSuccess onSuccess = (Realm.Transaction.OnSuccess) invocation.getArguments()[1];
                                    onSuccess.onSuccess();
                                }
                            }catch (Throwable error ){
                                if( invocation.getArguments().length >=3 ){
                                    Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                    onError.onError(error);
                                }
                            }
                        }

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {}

                            @Override
                            public boolean isCancelled() {return false;}
                        };
                    }
                }
        );


        when( realm.executeTransactionAsync(any( Realm.Transaction.class ), any(Realm.Transaction.OnError.class))  ).thenAnswer(
                new Answer<RealmAsyncTask>() {

                    @Override
                    public RealmAsyncTask answer(InvocationOnMock invocation) throws Throwable {

                        if( invocation.getArguments().length >=1 ){
                            Realm.Transaction transaction = (Realm.Transaction) invocation.getArguments()[0];

                            try{
                                transaction.execute( realm );
                            }catch (Throwable error ){
                                if( invocation.getArguments().length >=2 ){
                                    Realm.Transaction.OnError onError = (Realm.Transaction.OnError) invocation.getArguments()[2];
                                    onError.onError(error);
                                }
                            }
                        }

                        //this is just to meet requirements
                        return new RealmAsyncTask() {
                            @Override
                            public void cancel() {}

                            @Override
                            public boolean isCancelled() {return false;}
                        };
                    }
                }
        );
    }
}