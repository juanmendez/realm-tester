package info.juanmendez.mockrealmdemo;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.ModelEmit;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
public class PowerMockRealmTest
{
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
        realm = Realm.getDefaultInstance();
    }

    @Test
    public void shouldBeSameRealm(){
        assertEquals("is the same?", realm, Realm.getDefaultInstance());
    }

    @Test
    public void shouldCreateObject(){
        RealmStorage.clear();
        assertNotNull( realm.createObject(Dog.class));
    }

    /**
     * Realm mocked is suppose to bounce back same object to copyToRealm
     * @throws Exception
     */
    @Test
    public void shouldCopyToRealm() throws Exception {
        RealmStorage.clear();
        Dog dog = new Dog();
        dog.setName("Max");
        dog.setAge(1);

        realm.copyToRealm( dog );

        assertEquals( "There is one dog", realm.where( Dog.class ).count(), 1);
    }

    /**
     * So lets see if I can prepare an object, and I can get a realmResult back, and check the size to be equal to 1
     */
    @Test
    public void shouldExecuteTransaction(){
        RealmStorage.clear();
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
    public void shouldQueryByConditions(){
        RealmStorage.clear();
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
    public void shouldQueryByCaseSensitivity(){

        RealmStorage.clear();

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
    public void shouldCount(){
        RealmStorage.clear();

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
    public void shouldQuerySynchronousTransaction(){
        RealmStorage.clear();

        realm.executeTransaction(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate( new Date(2011, 6, 10));
        });

        assertEquals( "Synchronous added first item", realm.where(Dog.class).findFirst().getName(), "Max" );
    }

    @Test
    public void shouldQueryAsyncTransactionOnSuccessAndError(){

        RealmStorage.clear();

        /*
           when testing just with PowerMockito we use main thread as schedulers.
           if you are using Robolectric then use instead:
            RealmDecorator.setTransactionScheduler(Schedulers.computation());
            RealmDecorator.setResponseScheduler(AndroidSchedulers.mainThread());
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
    public void shouldDoLinkingQueries(){
        RealmStorage.clear();

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
    public void shouldQueryByOr(){

        RealmStorage.clear();

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
    public void shouldQueryAgainstRealmResults(){

        RealmStorage.clear();

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

    @Test
    public void shouldDeleteRealmObject(){
        RealmStorage.clear();

        Dog dog = realm.createObject( Dog.class );
        dog.setName("Max");
        dog.setAge(1);
        dog.setId(1);
        dog.setBirthdate( new Date() );

        Person person = realm.createObject( Person.class );
        person.setFavoriteDog( dog );
        person.setDogs( new RealmList<>(dog, dog, dog, dog ));

        dog.deleteFromRealm();

        //assertEquals( "The only dog added has been removed", realm.where(Dog.class).count(), 0 );
        assertNull( "Person's favorite dog is gone", person.getFavoriteDog() );
    }

    @Test
    public void findModelRelationship(){

        RealmStorage.clear();

        Person person = realm.createObject( Person.class );
        person.setDogs( new RealmList<>());

        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(person);

        for (Field field: fieldSet) {

            if( RealmModel.class.isAssignableFrom( field.getType() )){
                System.out.println( "we will watch for " + field.getName() );
            }
        }
    }

    @Test
    public void shouldFilterByPersonClass(){

        RealmStorage.clear();

        RealmList list = new RealmList(new Dog(), new Dog(), new Dog() );
        list.add( new Dog() );
        list.add( new Person() );
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Person() );
        list.add( new Person() );
        list.add( new Person() );
        list.add( new Person() );
        list.add( new Person() );

        Observable.from( list )
                .ofType(Person.class)
                .subscribe(o -> {
                    System.out.println( o.getClass().getSimpleName() );
        });
    }

    @Test
    public void shouldBeIn(){

        RealmStorage.clear();

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

        RealmResults<Dog> dogs = realm.where( Dog.class ).in( "age", new Integer[]{2,5} ).findAll();

        assertEquals( "There are two dogs born before the given date", dogs.size(), 3 );

        dogs = realm.where( Dog.class ).in( "name", new String[]{"Idalgo Mendez"} ).findAll();
        assertEquals( "There is one dog with that name", dogs.size(), 1 );

        dogs = realm.where( Dog.class ).in( "name", new String[]{"IdAlgo MendEz", "HERNAN FerNandeZ"}, Case.INSENSITIVE ).findAll();
        assertEquals( "There are two dogs with those names", dogs.size(), 2 );


        dogs = realm.where( Dog.class ).in( "name", new String[]{"IdAlgo MendEz", "HERNAN FerNandeZ"} ).findAll();
        assertEquals( "There are no dogs with names like that", dogs.size(), 0 );

        dogs = realm.where( Dog.class ).in( "birthdate", new Date[]{new Date(2011, 6, 10), new Date(2014, 6, 10)} ).findAll();
        assertEquals( "There are two dogs with those names", dogs.size(), 2 );
    }


    @Test
    public void shouldDoMax(){

        RealmStorage.clear();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate( new Date(2010, 6, 9));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Fido Fernandez");
        dog.setBirthdate( new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate( new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate( new Date(2012, 2, 1));



        assertEquals( "max age is ", 6, realm.where(Dog.class).max("age").intValue());
        assertEquals( "min age is ", 1, realm.where(Dog.class).min("age").intValue());
        assertEquals( "average age is ", Double.valueOf((float) 3.5), Double.valueOf(realm.where(Dog.class).average("age")) );
    }


    @Test
    public void shouldCreateADogInMainActivity(){
        RealmStorage.clear();

        realm.executeTransactionAsync(realm1 -> {
            Dog dog = realm1.createObject( Dog.class );
            dog.setName("Max");
            dog.setAge(1);
            dog.setId(1);
            dog.setBirthdate( new Date() );

            Person person = realm1.createObject( Person.class );
            person.setDogs( new RealmList<>(dog));
        }, () -> {
            System.out.println( "MainActivity. Number of dogs: " + realm.where(Person.class).findFirst().getClass().getSuperclass() );
        }, error -> {
            System.err.println( "MainActivity. There was an error completing transaction" + error.getMessage() );
        });

        Assert.assertEquals( "MainActivity entered one dog!", realm.where(Dog.class).count(), 1 );
        Assert.assertEquals( "MainActivity entered one person!", realm.where(Person.class).count(), 1 );
    }

    @Test
    public void shouldModelObservableWork(){
        RealmStorage.clear();

        RealmObservable.add(
                RealmObservable.asObservable()
                .subscribe(modelEmit -> System.out.println( "onNext " + modelEmit.getState() ))
        );


        //lets now find only people
        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter(modelEmit -> {
                            return modelEmit.getRealmModel() instanceof Person;
                        }).subscribe(modelEmit -> {
                    System.out.println( "nextPerson: " + modelEmit.getState() );
                })
        );


        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter( modelEmit -> modelEmit.getState()== ModelEmit.REMOVED )
                        .map(modelEmit -> modelEmit.getRealmModel() )
                        .ofType( Dog.class )
                        .subscribe(realmModel -> System.out.println( "onNextDogRemoved-> " + realmModel.toString() ))
        );


        Person person = realm.createObject(Person.class);
        Dog dog = realm.createObject( Dog.class );
        dog.deleteFromRealm();
        person.deleteFromRealm();
    }
}