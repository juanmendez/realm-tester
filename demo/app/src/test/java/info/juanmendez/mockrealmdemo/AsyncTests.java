package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.RealmEvent;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
public class AsyncTests
{
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
        realm = Realm.getDefaultInstance();
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
    public void shouldRealmObservableWork(){
        RealmStorage.clear();

        RealmObservable.add(
                RealmObservable.asObservable()
                        .subscribe(realmEvent -> System.out.println( "onNext " + realmEvent.getState() ))
        );


        //lets now find only people
        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter(realmEvent -> {
                            return realmEvent.getRealmModel() instanceof Person;
                        }).subscribe(realmEvent -> {
                    System.out.println( "nextPerson: " + realmEvent.getState() );
                })
        );


        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter(realmEvent -> realmEvent.getState()== RealmEvent.MODEL_REMOVED)
                        .map(realmEvent -> realmEvent.getRealmModel() )
                        .ofType( Dog.class )
                        .subscribe(realmModel -> System.out.println( "onNextDogRemoved-> " + realmModel.toString() ))
        );


        Person person = realm.createObject(Person.class);
        Dog dog = realm.createObject( Dog.class );
        dog.deleteFromRealm();
        person.deleteFromRealm();
    }

    @Test
    public void shouldDoAllAsync(){

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


        RealmResults realmResults = realm.where( Dog.class ).findAllAsync();


        realmResults.addChangeListener(new RealmChangeListener<RealmResults>() {
            @Override
            public void onChange(RealmResults element) {
                assertEquals("there should be four dogs", element.size(), 4 );
            }
        });
    }

    @Test
    public void shouldGetFirstAsync(){

        RealmStorage.clear();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate( new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate( new Date(2012, 2, 1));


        RealmObject realmObject = realm.where( Dog.class ).equalTo("age", 2 ).findFirstAsync();

        final int[] calls = {0};
        realmObject.addChangeListener((RealmChangeListener<Dog>) element -> {
            calls[0]++;
        });

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate( new Date(2015, 6, 10));

        dog = realm.where( Dog.class ).equalTo("name", "Hernan Fernandez").findFirst();
        dog.deleteFromRealm();
        realm.commitTransaction();

        realmObject.removeChangeListeners();
        assertEquals( "changeListener invoked twice", calls[0], 2);
    }

    @Test
    public void shouldChangeListenersWork(){
        RealmStorage.clear();

        RealmResults<Dog> results = realm.where( Dog.class ).findAllAsync();
        assertNotNull( "realmObject exists", results );

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        Dog dog;
        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate( new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate( new Date(2012, 2, 1));
        realm.commitTransaction();

        results.removeChangeListeners();

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate( new Date(2015, 6, 10));
        realm.commitTransaction();

        assertEquals( "changeListener invoked twice", calls[0], 2);
    }

    @Test
    public void shouldShowChangesFromRealmResultsWithSyncTransactions(){
        RealmStorage.clear();

        RealmResults<Dog> results = realm.where( Dog.class ).findAllAsync();
        assertNotNull( "realmObject exists", results );

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        realm.executeTransaction( realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Hernan Fernandez");
            dog.setBirthdate( new Date(2015, 6, 10));

            dog = realm1.createObject(Dog.class);
            dog.setAge(5);
            dog.setName("Pedro Flores");
            dog.setBirthdate( new Date(2012, 2, 1));
        });

        realm.executeTransaction( realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Aaron Hernandez");
            dog.setBirthdate( new Date(2015, 6, 10));

            realm1.where(Dog.class).findFirst().deleteFromRealm();
        });

        assertEquals( "changeListener invoked twice", calls[0], 3);
    }

    @Test
    public void shouldShowChangesFromRealmResultsWithAsyncTransactions(){
        RealmStorage.clear();

        RealmResults<Dog> results = realm.where( Dog.class ).findAllAsync();
        assertNotNull( "realmObject exists", results );

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        realm.executeTransactionAsync( realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Hernan Fernandez");
            dog.setBirthdate( new Date(2015, 6, 10));

            dog = realm1.createObject(Dog.class);
            dog.setAge(5);
            dog.setName("Pedro Flores");
            dog.setBirthdate( new Date(2012, 2, 1));
        });

        realm.executeTransactionAsync( realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Aaron Hernandez");
            dog.setBirthdate( new Date(2015, 6, 10));

            realm1.where(Dog.class).findFirst().deleteFromRealm();
        });

        assertEquals( "changeListener invoked twice", calls[0], 3);
    }
}