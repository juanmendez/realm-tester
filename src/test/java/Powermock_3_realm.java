import android.content.Context;
import info.juanmendez.learn.realms.RealmDog;
import io.realm.*;
import io.realm.internal.RealmCore;
import io.realm.log.RealmLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by musta on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class, RealmLog.class, RealmAsyncTask.class, Realm.Transaction.class })
public class Powermock_3_realm
{
    Realm realm;
    RealmConfiguration realmConfiguration;

    //keep collections of realmObjects keyed by immediate class.
    HashMap<Class, ArrayList<RealmObject>> realmMap;

    //collections queried keyed by immediate class
    HashMap<Class, ArrayList<RealmObject> > queryMap;

    @Before
    public void before() throws Exception {

        realmMap = new HashMap<>();
        queryMap = new HashMap<>();

        mockStatic( Realm.class );
        mockStatic( RealmConfiguration.class );
        mockStatic( RealmQuery.class );
        mockStatic( RealmResults.class );
        mockStatic( RealmCore.class );
        mockStatic( RealmLog.class );
        mockStatic( RealmAsyncTask.class );
        mockStatic( Realm.Transaction.class );

        realm = PowerMockito.mock(Realm.class );
        realmConfiguration = PowerMockito.mock(RealmConfiguration.class);
        PowerMockito.doNothing().when( RealmCore.class );
        RealmCore.loadLibrary( any( Context.class) );

        when( Realm.getDefaultInstance() ).thenReturn( realm );

        //TODO: when will be moved into a realm factory
        when( realm.createObject( Mockito.argThat(new ClassMatcher<>(RealmObject.class)) ) ).thenAnswer( new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class clazz = (Class) invocationOnMock.getArguments()[0];

                if( !realmMap.containsKey(clazz)){
                    realmMap.put(clazz, new ArrayList<>());
                }

                Constructor constructor = clazz.getConstructor();
                RealmObject realmObject = (RealmObject) constructor.newInstance();

                realmMap.get(clazz).add( realmObject);

                return realmObject;
            }
        });

        //TODO: when will be moved into a realm factory
        RealmAsyncTask realmAsyncTask = mock( RealmAsyncTask.class );

        //TODO: when will be moved into a realm factory
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


        //TODO: when will be moved into a realm factory
        when( realm.copyToRealm(Mockito.any( RealmObject.class ))).thenAnswer( new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {

                if( invocationOnMock.getArguments().length > 0 ){
                    RealmObject realmObject = (RealmObject) invocationOnMock.getArguments()[0];
                    Class clazz = realmObject.getClass();

                    if( !realmMap.containsKey(clazz)){
                        realmMap.put(clazz, new ArrayList<>());
                    }

                    realmMap.get( clazz ).add( realmObject );
                    return realmObject;
                }

                return null;
            }
        });

        //TODO: when will be moved into a realm factory
        when( realm.where( Mockito.argThat( new ClassMatcher<>(RealmObject.class))  ) ).then( new Answer<RealmQuery>(){

            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                //clear list being queried
                Class clazz = (Class) invocationOnMock.getArguments()[0];
                queryMap.put(clazz, realmMap.get(clazz));

                RealmQuery realmQuery = mock(RealmQuery.class);
                when( realmQuery.toString() ).thenReturn( "Realm:" + clazz.getName() );
                Whitebox.setInternalState( realmQuery, "clazz", clazz);
                prepareToQuery( realmQuery, clazz );

                return realmQuery;
            }
        });
    }

    private void prepareToQuery(RealmQuery realmQuery, Class clazz ){

        when( realmQuery.findAll() ).thenAnswer(invocationOnMock ->{

            //TODO: all these stubbing done on realmResults will go into a factory
            ArrayList<RealmObject> realResults = queryMap.get(clazz);
            RealmResults realmResults = PowerMockito.mock( RealmResults.class );

            when( realmResults.get(anyInt())).thenAnswer(positionInvokation -> {
                int position = (int) positionInvokation.getArguments()[0];
                return realResults.get( position );
            });

            when( realmResults.size() ).thenReturn( realResults.size() );
            when( realmResults.iterator() ).thenReturn( realResults.iterator() );
            when( realmResults.set(anyInt(), any(RealmObject.class)) ).thenAnswer(new Answer<RealmObject>() {
                @Override
                public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                    int index = (int) invocationOnMock.getArguments()[0];
                    RealmObject value = (RealmObject) invocationOnMock.getArguments()[0];
                    realResults.set(index, value);
                    return value;
                }
            });

            when( realmResults.listIterator() ).thenReturn( realResults.listIterator() );

            when( realmResults.listIterator(anyInt()) ).thenAnswer(new Answer<RealmObject>() {
                @Override
                public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                    int index = (int) invocationOnMock.getArguments()[0];
                    return realResults.get(index);
                }
            });

            return realmResults;
        });

        when( realmQuery.findFirst()).thenAnswer(invocationOnMock -> {
            ArrayList<RealmObject> realResults = queryMap.get(clazz);
            return realResults.get(0);
        });

        //TODO whens will also be moved over another class
        when( realmQuery.lessThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );


        when( realmQuery.lessThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual ) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );

        when( realmQuery.greaterThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
         when( realmQuery.greaterThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.more) );
       when( realmQuery.greaterThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.more) );

        when( realmQuery.greaterThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );

        when( realmQuery.equalTo( anyString(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );

    }

    /**
     * This method filters using args.condition and updates queryMap<realmQuery.clazz, collection>
     * @param realmQuery queryMap.get( realmQuery.clazz ) gives key to get collection from queryMap
     * @param condition based on Compare.enums
     * @return the same args.realmQuery
     */
    private Answer<RealmQuery> createComparison( RealmQuery realmQuery, String condition ){

        Class realmQueryClass = (Class) Whitebox.getInternalState( realmQuery, "clazz");

        return new Answer<RealmQuery>() {
            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

                String type = (String) invocationOnMock.getArguments()[0];
                Object value = invocationOnMock.getArguments()[1];
                Class clazz = value.getClass();

                if( type.isEmpty() )
                    return realmQuery;

                ArrayList<RealmObject> queriedList = new ArrayList<>();
                ArrayList<RealmObject> searchList = new ArrayList<>();
                searchList = queryMap.get(realmQueryClass);

                for (RealmObject realmObject: searchList) {

                    //RunTimeErrorException if search field is not found in realmQueryClass
                    Object thisValue = Whitebox.getInternalState( realmObject, type );

                    if( thisValue != null ){

                        if( condition == Compare.equal ){

                            if( clazz == Date.class && ( ((Date)thisValue) ).compareTo( (Date)value ) == 0 ){
                                queriedList.add( realmObject);
                            }else if(value.equals(thisValue)){
                                queriedList.add( realmObject);
                            }
                        }
                        else
                        if( condition == Compare.less){

                            if( clazz == Date.class && ( ((Date)thisValue) ).compareTo( (Date)value ) < 0 ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Byte.class && ((byte)thisValue) < ((byte)value)){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Integer.class && ((int)thisValue) < ((int)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Double.class && ((double)thisValue) < ((double)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Long.class && ((long)thisValue) < ((long)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Float.class && ((float)thisValue) < ((float)value) ){
                                queriedList.add( realmObject);
                            }
                        }
                        else if( condition == Compare.lessOrEqual){

                            if( clazz == Date.class && ( ((Date)thisValue) ).compareTo( (Date)value ) <= 0 ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Byte.class && ((byte)thisValue) <= ((byte)value)){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Integer.class && ((int)thisValue) <= ((int)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Double.class && ((double)thisValue) <= ((double)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Long.class && ((long)thisValue) <= ((long)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Float.class && ((float)thisValue) <= ((float)value) ){
                                queriedList.add( realmObject);
                            }
                        }
                        else if( condition == Compare.more ){

                            if( clazz == Date.class && ( ((Date)thisValue) ).compareTo( (Date)value ) > 0 ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Byte.class && ((byte)thisValue) > ((byte)value)){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Integer.class && ((int)thisValue) > ((int)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Double.class && ((double)thisValue) > ((double)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Long.class && ((long)thisValue) > ((long)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Float.class && ((float)thisValue) > ((float)value) ){
                                queriedList.add( realmObject);
                            }
                        }
                        else if( condition == Compare.moreOrEqual ){

                            if( clazz == Date.class && ( ((Date)thisValue) ).compareTo( (Date)value ) >= 0 ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Byte.class && ((byte)thisValue) >= ((byte)value)){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Integer.class && ((int)thisValue) >= ((int)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Double.class && ((double)thisValue) >= ((double)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Long.class && ((long)thisValue) >= ((long)value) ){
                                queriedList.add( realmObject);
                            }
                            else if( clazz == Float.class && ((float)thisValue) >= ((float)value) ){
                                queriedList.add( realmObject);
                            }
                        }

                    /*
                    TODO: more conditions to search and update queryMap<realmQueryClass, collection>
                    else if( compare == Compare.contains && clazz == String.class ){


                    }
                    else if( compare == Compare.startsWith && clazz == String.class ){


                    }
                    else if( compare == Compare.endsWith && clazz == String.class ){


                    }*/

                    }
                }

                queryMap.put( realmQueryClass, queriedList);

                return realmQuery;
            }
        };
    }

    private void getClass( RealmQuery realmQuery ){
    }

    @Test
    public void checkIfDefaultIsOurRealm(){
        assertEquals("is the same?", realm, Realm.getDefaultInstance());
    }

    @Test
    public void testCreateObject(){
        assertNotNull( realm.createObject(RealmDog.class));
    }

    @Test
    public void testCopyToRealm() throws Exception {

        RealmDog dog = new RealmDog();
         dog.setName("Max");
        dog.setAge(1);

        assertEquals("is same dog?", dog, realm.copyToRealm( dog ) );
    }

    @Test
    public void testExecuteTransaction(){
        realm.executeTransaction( realm1 -> {
            RealmDog dog = realm.createObject(RealmDog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        assertNotNull("there is now a List<RealmDog.class> in realmMap", realmMap.get(RealmDog.class) );
        assertTrue( "there is one element in ",  realmMap.get(RealmDog.class).size() == 1);
    }

    @Test
    public void testConditions(){
        RealmDog dog = realm.createObject(RealmDog.class);
        dog.setAge(1);
        dog.setName("Max");
        dog.setBirthdate( new Date(2011, 6, 10));

        dog = realm.createObject(RealmDog.class);
        dog.setAge(2);
        dog.setName("Rex");
        dog.setBirthdate( new Date(2016, 6, 10));


        RealmResults<RealmDog> dogs = realm.where(RealmDog.class).greaterThanOrEqualTo("birthdate", new Date(2009, 6, 10) ).findAll();
        assertNotNull( "dog is found", dogs  );

        for( int i = 0; i < dogs.size(); i++ ){
            System.out.println( "dog: " + dogs.get(i).getName() );
        }

        for( RealmDog iDog: dogs ){
            System.out.println( "dog: " + iDog.getName() );
        }
    }

    //http://stackoverflow.com/questions/7500312/mockito-match-any-class-argument
    class ClassMatcher<T> extends ArgumentMatcher<Class<T>> {

        private final Class<T> targetClass;

        public ClassMatcher(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        public boolean matches(Object obj) {

            if (obj != null && obj instanceof Class) {
                return targetClass.isAssignableFrom((Class<T>) obj);
            }
            return false;
        }
    }

    /**
     * this is done out of pure fun. Seeing how to create a matcher
     * whose class must extend the one given.
     * @param <T>
     */
    class InstanceMatcher<T> extends ArgumentMatcher<T>{

        private final Class<T> targetClass;

        public InstanceMatcher(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public boolean matches(Object o) {
            return targetClass.isInstance( o );
        }
    }


    class ArgMatcher<T> extends ArgumentMatcher<T>{

        @Override
        public boolean matches(Object o) {
            return false;
        }
    }

    class Compare{
        static final String less = "less";
        static final String lessOrEqual = "lessOrEqual";
        static final String equal = "equal";
        static final String more = "more";
        static final String moreOrEqual = "moreOrEqual";
    }
}
