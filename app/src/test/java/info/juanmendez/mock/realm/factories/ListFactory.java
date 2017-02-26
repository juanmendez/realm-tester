package info.juanmendez.mock.realm.factories;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by Juan Mendez on 2/25/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class ListFactory {

    public static void prepare() throws Exception {

        whenNew( RealmList.class ).withArguments(anyVararg()).thenAnswer(invocation -> {

            RealmList list = spy(new RealmList());
            Object[] args = invocation.getArguments();

            for (Object arg:args) {
                list.add((RealmModel)arg);
            }

            handleDeleteMethods( list );

            return list;
        });

    }

    private  static void handleDeleteMethods( RealmList list ){

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {

                for (Object realmModel: list) {
                    deleteRealmModel( (RealmModel) realmModel );
                }

                return true;
            }
        }).when( list ).deleteAllFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {

                if( !list.isEmpty() ){
                    RealmModel realmModel = list.get(0);
                    deleteRealmModel( realmModel );
                    return true;
                }

                return false;
            }
        }).when( list ).deleteFirstFromRealm();


        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {

                int position = (int) invocation.getArguments()[0];

                if( !list.isEmpty() && list.size()-1 >= position ){
                    RealmModel realmModel = list.get( position );

                    deleteRealmModel( realmModel );
                    return true;
                }

                return false;
            }
        }).when( list ).deleteLastFromRealm() ;


        doAnswer( new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                if( !list.isEmpty()){
                    RealmModel realmModel = list.get( list.size() - 1 );

                    deleteRealmModel( realmModel );
                }

                return null;
            }
        }).when( list ).deleteFromRealm( anyInt() );
    }

    private static void deleteRealmModel( RealmModel realmModel ){
        if( realmModel instanceof RealmObject){
            ((RealmObject) realmModel ).deleteFromRealm();
        }
        else{
            RealmObject.deleteFromRealm( realmModel );
        }
    }
}