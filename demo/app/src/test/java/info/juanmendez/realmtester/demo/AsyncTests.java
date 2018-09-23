package info.juanmendez.realmtester.demo;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import info.juanmendez.realmtester.RealmTester;
import info.juanmendez.realmtester.dependencies.RealmObservable;
import info.juanmendez.realmtester.demo.models.RealmAnnotation;
import info.juanmendez.realmtester.demo.models.RealmEvent;
import info.juanmendez.realmtester.test.RealmTesterBase;
import info.juanmendez.realmtester.demo.models.Dog;
import info.juanmendez.realmtester.demo.models.Person;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
public class AsyncTests extends RealmTesterBase {
    Realm realm;

    @Before
    public void before() throws Exception {
        RealmTester.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        RealmTester.addAnnotations(RealmAnnotation.build(Dog.class)
                        .primaryField("id")
                        .indexedFields("name", "age", "birthdate", "nickname"),
                RealmAnnotation.build(Person.class)
                        .primaryField("id")
                        .indexedFields("name"));

        realm = Realm.getDefaultInstance();
    }

    @Test
    public void shouldQuerySynchronousTransaction() {
        RealmTester.clearData();

        realm.executeTransaction(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate(new Date(2011, 6, 10));
        });

        assertEquals("Synchronous added first item", realm.where(Dog.class).findFirst().getName(), "Max");
    }

    @Test
    public void shouldQueryAsyncTransactionOnSuccessAndError() {

        RealmTester.clearData();

        realm.executeTransactionAsync(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate(new Date(2011, 6, 10));

        }, () -> {
            System.out.println("this dog made was succesfully saved!");
        });


        realm.executeTransactionAsync(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate(new Date(2011, 6, 10));
        });

        assertEquals("There are two items found after async transactions", realm.where(Dog.class).findAll().size(), 2);

        realm.executeTransactionAsync(realm1 -> {
            throw new RuntimeException("Making a big deal because there are no more dogs to add");
        }, () -> {
            System.out.println("transaction was succesful!");
        }, error -> {
            System.err.println("transaction didn't go well: " + error.getMessage());
        });
    }

    @Test
    public void shouldRealmObservableWork() {
        RealmTester.clearData();

        RealmObservable.add(
                RealmObservable.asObservable()
                        .subscribe(realmEvent -> System.out.println("onNext " + realmEvent.getState()))
        );


        //lets now find only people
        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter(realmEvent -> {
                            return realmEvent.getRealmModel() instanceof Person;
                        }).subscribe(realmEvent -> {
                    System.out.println("nextPerson: " + realmEvent.getState());
                })
        );


        RealmObservable.add(
                RealmObservable.asObservable()
                        .filter(realmEvent -> realmEvent.getState() == RealmEvent.MODEL_REMOVED)
                        .map(realmEvent -> realmEvent.getRealmModel())
                        .ofType(Dog.class)
                        .subscribe(realmModel -> System.out.println("onNextDogRemoved-> " + realmModel.toString()))
        );


        Person person = realm.createObject(Person.class);
        Dog dog = realm.createObject(Dog.class);
        dog.deleteFromRealm();
        person.deleteFromRealm();
    }

    @Test
    public void shouldDoAllAsync() {

        RealmTester.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2010, 6, 9));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2012, 2, 1));


        RealmResults realmResults = realm.where(Dog.class).findAllAsync();


        realmResults.addChangeListener(new RealmChangeListener<RealmResults>() {
            @Override
            public void onChange(RealmResults element) {
                assertEquals("there should be four dogs", element.size(), 4);
            }
        });
    }

    @Test
    public void shouldGetFirstAsync() {

        RealmTester.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2012, 2, 1));


        RealmObject realmObject = realm.where(Dog.class).equalTo("age", 2).findFirstAsync();

        final int[] calls = {0};
        realmObject.addChangeListener((RealmChangeListener<Dog>) element -> {
            calls[0]++;
        });

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate(new Date(2015, 6, 10));

        dog = realm.where(Dog.class).equalTo("name", "Hernan Fernandez").findFirst();
        dog.deleteFromRealm();
        realm.commitTransaction();

        realmObject.removeChangeListeners();
        assertEquals("changeListener invoked twice", calls[0], 2);
    }

    @Test
    public void shouldChangeListenersWork() {
        RealmTester.clearData();

        RealmResults<Dog> results = realm.where(Dog.class).findAllAsync();
        assertNotNull("realmObject exists", results);

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        Dog dog;
        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2012, 2, 1));
        realm.commitTransaction();

        results.removeChangeListeners();

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate(new Date(2015, 6, 10));
        realm.commitTransaction();

        assertEquals("changeListener invoked twice", calls[0], 2);
    }

    @Test
    public void shouldChangeListenerWorkWithExecuteTransactions() {
        RealmTester.clearData();

        RealmResults<Dog> results = realm.where(Dog.class).findAllAsync();
        assertNotNull("realmObject exists", results);

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        realm.executeTransaction(realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Hernan Fernandez");
            dog.setBirthdate(new Date(2015, 6, 10));

            dog = realm1.createObject(Dog.class);
            dog.setAge(5);
            dog.setName("Pedro Flores");
            dog.setBirthdate(new Date(2012, 2, 1));
        });

        realm.executeTransaction(realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Aaron Hernandez");
            dog.setBirthdate(new Date(2015, 6, 10));

            realm1.where(Dog.class).findFirst().deleteFromRealm();
        });

        assertEquals("changeListener invoked twice", calls[0], 3);
    }

    @Test
    public void shouldChangeListenerWorkWithExecuteTransactions2() {
        RealmTester.clearData();

        RealmResults<Dog> results = realm.where(Dog.class).findAllAsync();
        assertNotNull("realmObject exists", results);

        final int[] calls = {0};
        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            calls[0]++;
        });

        realm.executeTransactionAsync(realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Hernan Fernandez");
            dog.setBirthdate(new Date(2015, 6, 10));

            dog = realm1.createObject(Dog.class);
            dog.setAge(5);
            dog.setName("Pedro Flores");
            dog.setBirthdate(new Date(2012, 2, 1));
        });

        realm.executeTransactionAsync(realm1 -> {

            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Aaron Hernandez");
            dog.setBirthdate(new Date(2015, 6, 10));

            realm1.where(Dog.class).findFirst().deleteFromRealm();
        });

        assertEquals("changeListener invoked twice", calls[0], 3);
    }
}