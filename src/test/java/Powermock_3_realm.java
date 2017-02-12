import info.juanmendez.learn.realms.RealmDog;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by musta on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Realm.class)
public class Powermock_3_realm
{
    Realm realm;

    HashMap<Class, ArrayList<RealmObject>> hashMap;

    @Before
    public void before(){

        hashMap = new HashMap<>();

        PowerMockito.mockStatic( Realm.class );

        realm = PowerMockito.mock(Realm.class );
        PowerMockito.when( Realm.getDefaultInstance() ).thenReturn( realm );

        PowerMockito.when( realm.createObject( Mockito.argThat(new ClassMatcher<RealmObject>(RealmObject.class)) ) ).thenAnswer( new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class clazz = (Class) invocationOnMock.getArguments()[0];

                if( !hashMap.containsKey(clazz)){
                    hashMap.put(clazz, new ArrayList<>());
                }

                Constructor constructor = clazz.getConstructor();
                RealmObject realmObject = (RealmObject) constructor.newInstance();

                hashMap.get(clazz).add( realmObject);

                return realmObject;
            }
        });

        PowerMockito.when( realm.copyToRealm(Mockito.any( RealmObject.class ))).thenAnswer( new Answer<RealmObject>(){

            @Override
            public RealmObject answer(InvocationOnMock invocationOnMock) throws Throwable {

                RealmObject realmObject = (RealmObject) invocationOnMock.getArguments()[0];
                Class clazz = realmObject.getClass();

                if( !hashMap.containsKey(clazz)){
                    hashMap.put(clazz, new ArrayList<>());
                }

                hashMap.get( clazz ).add( realmObject );
                return realmObject;
            }
        });
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
    public void checkDog(){
        RealmDog dog = realm.createObject(RealmDog.class);
        dog.setAge(1);
        dog.setName("Max");

       RealmResults<RealmDog> dogs = realm.where(RealmDog.class).equalTo("age", 1).findAll();

       assertNotNull("dogs no dogs", dogs);
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
}
