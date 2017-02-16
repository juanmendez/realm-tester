import info.juanmendez.learn.realms.models.Dog;
import info.juanmendez.mock.realm.MockRealm;
import info.juanmendez.mock.realm.factories.RealmFactory;
import io.realm.*;
import io.realm.internal.RealmCore;
import io.realm.log.RealmLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class, RealmLog.class, RealmAsyncTask.class, Realm.Transaction.class })
public class RealmMock
{
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
        realm = RealmFactory.create();
    }
    @Test
    public void checkIfDefaultIsOurRealm(){
        assertEquals("is the same?", realm, Realm.getDefaultInstance());
    }

    @Test
    public void testCreateObject(){
        assertNotNull( realm.createObject(Dog.class));
    }

    @Test
    public void testCopyToRealm() throws Exception {

        Dog dog = new Dog();
         dog.setName("Max");
        dog.setAge(1);

        assertEquals("is same dog?", dog, realm.copyToRealm( dog ) );
    }

    @Test
    public void testExecuteTransaction(){
        realm.executeTransaction( realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        //assertNotNull("there is now a List<Dog.class> in realmMap", realmMap.get(Dog.class) );
        //assertTrue( "there is one element in ",  realmMap.get(Dog.class).size() == 1);
    }

    @Test
    public void testConditions(){
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Max");
        dog.setBirthdate( new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Rex");
        dog.setBirthdate( new Date(2016, 6, 10));


        RealmResults<Dog> dogs = realm.where(Dog.class).greaterThanOrEqualTo("birthdate", new Date(2009, 6, 10) ).findAll();
        assertNotNull( "dog is found", dogs  );

        for( int i = 0; i < dogs.size(); i++ ){
            System.out.println( "dog: " + dogs.get(i).getName() );
        }

        for( Dog iDog: dogs ){
            System.out.println( "dog: " + iDog.getName() );
        }
    }


    class ArgMatcher<T> extends ArgumentMatcher<T>{

        @Override
        public boolean matches(Object o) {
            return false;
        }
    }
}
