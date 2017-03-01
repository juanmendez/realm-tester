package info.juanmendez.mock.realm.factories;

import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

            System.out.println( "create spied realmList from whenNew");
            RealmList list = spy(new RealmList<>());
            Object[] args = invocation.getArguments();

            for (Object arg:args) {
                list.add((RealmModel)arg);
            }

            //handleDeleteMethods( list );
            //handleMathMethods( list );

            return list;
        });
    }

    public static RealmList<RealmModel> create(){
        System.out.println( "create spied realmList directly");
        RealmList list = spy(new RealmList());
       // handleDeleteMethods( list );
       // handleMathMethods( list );
        return list;
    }


    public static RealmList<RealmModel> create(RealmList<RealmModel> realList ){
        RealmList list = spy(new RealmList());

        for (RealmModel realmModel:realList) {
            list.add( realmModel );
        }

        handleDeleteMethods( list );
        handleMathMethods( list );
        return list;
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

    private static void handleMathMethods(RealmList list) {

        //min value
        doAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                String field = (String) invocation.getArguments()[0];
                Number value, minValue = null;

                for (Object item: list ) {

                    value = (Number) Whitebox.getInternalState( item, field );

                    if( minValue == null )
                        minValue = value;
                    else
                    if(  value.floatValue() < minValue.floatValue()  ){
                        minValue = value;
                    }
                }

                return minValue;
            }
        }).when( list ).min(anyString()) ;


        //max value
        doAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                String field = (String) invocation.getArguments()[0];
                Number value, maxValue = null;

                for (Object item: list ) {

                    value = (Number) Whitebox.getInternalState( item, field );

                    if( maxValue == null )
                        maxValue = value;
                    else
                    if(  value.floatValue() > maxValue.floatValue()  ){
                        maxValue = value;
                    }
                }

                return maxValue;
            }
        }).when( list ).max(anyString()) ;


        //sum value
        doAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                String field = (String) invocation.getArguments()[0];
                float value, sumValue = 0;

                for (Object item: list ) {

                    sumValue += ((Number) Whitebox.getInternalState( item, field )).floatValue();
                }

                return sumValue;
            }
        }).when( list ).sum(anyString()) ;


        //average value
        doAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                String field = (String) invocation.getArguments()[0];
                float value, sumValue = 0;

                for (Object item: list ) {

                    sumValue += ((Number) Whitebox.getInternalState( item, field )).floatValue();
                }

                return (sumValue/list.size());
            }
        }).when( list ).average(anyString()) ;
    }
}