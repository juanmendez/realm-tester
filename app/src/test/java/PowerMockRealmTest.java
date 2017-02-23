import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.juanmendez.mock.realm.MockRealm;
import info.juanmendez.mock.realm.factories.RealmFactory;
import info.juanmendez.mock.realm.models.Dog;
import info.juanmendez.mock.realm.models.Person;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 *
 * regex /public\s+static\s+(\w+)\s+(\w+)\((.*)?\)/gm
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class, RealmObject.class })
public class PowerMockRealmTest
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

    /**
     * Realm mocked is suppose to bounce back same object to copyToRealm
     * @throws Exception
     */
    @Test
    public void testCopyToRealm() throws Exception {

        Dog dog = new Dog();
        dog.setName("Max");
        dog.setAge(1);

        realm.copyToRealm( dog );
    }

    /**
     * So lets see if I can create an object, and I can get a realmResult back, and check the size to be equal to 1
     */
    @Test
    public void testExecuteTransaction(){
        realm.executeTransaction( realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        assertEquals( "there is now one element available", realm.where(Dog.class).findAll().size(), 1 );
    }

    /**
     * assures we can get back dogs who were born after 2009.
     */
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

        //iteration is working
        for( Dog _dog: dogs ){
            System.out.println( "dog: " + _dog.getName() );
        }

        //between
        dogs = realm.where( Dog.class ).between("birthdate", new Date( 2016, 6, 10  ), new Date(2017, 0, 1)).findAll();

        assertEquals( "There is only one dog born between during or after 07/10/2016, and 01/01/2017", dogs.size(), 1 );
    }

    @Test
    public void testCaseSensitivity(){

        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));

        RealmResults<Dog> dogs = realm.where(Dog.class).contains("name", "id", Case.INSENSITIVE ).findAll();
        assertEquals( "Two dogs contain 'id' cased-insensitive", dogs.size(), 2  );


        dogs = realm.where(Dog.class).contains("name", "id" ).findAll();
        assertEquals( "One dog contains 'id' cased sensitive", dogs.size(), 1  );


        dogs = realm.where(Dog.class).endsWith("name", "dez", Case.SENSITIVE  ).findAll();
        assertEquals( "Two dogs end with 'dez' case-sensitive", dogs.size(), 2  );
    }

    @Test
    public void testCount(){
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));

        long numOfDogs = realm.where(Dog.class).endsWith("name", "dez", Case.SENSITIVE  ).count();
        assertEquals( "Two dogs end with 'dez' case-sensitive", numOfDogs, 2  );
    }

    @Test
    public void testSynchronousTransaction(){
        realm.executeTransaction(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        assertEquals( "Synchronous added first item", realm.where(Dog.class).findFirst().getName(), "Max" );
    }

    @Test
    public void testAsyncTransactionOnSuccessAndError(){

        /*
           when testing just with PowerMockito we use main thread as schedulers.
           if you are using Robolectric then use instead:
            RealmFactory.setTransactionScheduler(Schedulers.computation());
            RealmFactory.setResponseScheduler(AndroidSchedulers.mainThread());
       */


        realm.executeTransactionAsync( realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));

        }, () ->{
            System.out.println( "this dog made was succesfully saved!");
        });



        realm.executeTransactionAsync( realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        assertEquals( "There are two items found after async transactions", realm.where(Dog.class).findAll().size(), 2 );

        realm.executeTransactionAsync( realm1 -> {
            throw new  RuntimeException("Making a big deal because there are no more dogs to add" );
        }, () ->{
            System.out.println( "transaction was succesful!" );
        }, error -> {
            System.err.println( "transaction didn't go well: " + error.getMessage() );
        });
    }

    @Test
    public void testDelimitedByPeriod(){
        String str = "abc.def.xyz";
        List<String> items = Arrays.asList(str.split("\\."));

        assertEquals("there are 3 items", items.size(), 3 );
    }

    @Test
    public void testLinkingQueries(){

        Person person;
        Dog dog;

        //person 1
        person = realm.createObject( Person.class );
        person.setName( "Pete" );

        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2011, 6, 10));
        person.getDogs().add( dog );

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));
        person.getDogs().add( dog );


        //person 2
        person = realm.createObject( Person.class );
        person.setName( "Roger" );

        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Martinez");
        dog.setBirthdate( new Date(2011, 6, 10));
        person.getDogs().add( dog );

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Flores");
        dog.setBirthdate( new Date(2016, 6, 10));
        person.getDogs().add( dog );

        RealmResults<Person> people = realm.where(Person.class).contains("dogs.name", "Flores" ).findAll();

        assertEquals( "there is one person found with such dog", 1, people.size());
    }


    @Test
    public void testOrQueries(){

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2011, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate( new Date(2012, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate( new Date(2014, 6, 10));


        RealmResults<Dog> dogs = realm.where(Dog.class).contains( "name", "Mendez" ).or().contains("name", "Fernandez" ).findAll();
        assertEquals( "There are three dogs with those last names", dogs.size(), 3 );

        //lets do the same criteria but this time from the three dogs, lets find the ones born before the date
        dogs = realm.where( Dog.class ).contains("name", "Mendez").or().contains("name", "Fernandez")
                .beginGroup().lessThan("birthdate", new Date(2013, 0, 1 ) ).endGroup().findAll();

        //Idalgo and Hernan were born before 2013
        assertEquals( "There are two dogs born before the given date", dogs.size(), 2 );
    }

    @Test
    public void testQueryAgainstRealmResults(){

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2011, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate( new Date(2012, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate( new Date(2014, 6, 10));

        RealmResults<Dog> dogs = realm.where(Dog.class).contains( "name", "Mendez" ).or().contains("name", "Fernandez" ).findAll();

        //rather than previous test, lets skip grouping and query against dogs
        dogs = dogs.where().lessThan("birthdate", new Date(2013, 0, 1 ) ).findAll();

        assertEquals( "There are two dogs born before the given date", dogs.size(), 2 );
    }
}
