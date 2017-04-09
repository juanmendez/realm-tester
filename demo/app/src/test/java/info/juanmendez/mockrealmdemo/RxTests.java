package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.dependencies.TransactionObservable;
import info.juanmendez.mockrealm.models.RealmAnnotation;
import info.juanmendez.mockrealm.models.TransactionEvent;
import info.juanmendez.mockrealm.test.MockRealmTester;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.KeyTransaction;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
public class RxTests extends MockRealmTester{
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        MockRealm.addAnnotations( RealmAnnotation.build(Dog.class)
                        .primaryField("id")
                        .indexedFields("name", "age", "birthdate", "nickname"),
                RealmAnnotation.build(Person.class)
                        .primaryField("id")
                        .indexedFields("name"));

        realm = Realm.getDefaultInstance();
    }

    /**
     */
    @Test(timeout = 6000)
    public void shouldBlockOtherTransactions(){
        MockRealm.clearData();
        TransactionObservable.asObservable().subscribe(transactionEvent -> {
            System.out.println( transactionEvent.getState() + ", " + transactionEvent.getTarget().toString() );
        });

        KeyTransaction keyTransaction;

        new Thread(() -> {
            KeyTransaction t = new KeyTransaction("_t1");
            executeWork( t, 50 );
        }).start();

        keyTransaction = new KeyTransaction("t2");
        TransactionObservable.startRequest(keyTransaction);
        TransactionObservable.endRequest(keyTransaction);

        keyTransaction = new KeyTransaction("t3");
        TransactionObservable.startRequest(keyTransaction);
        TransactionObservable.endRequest(keyTransaction);

        new Thread(() -> {
            KeyTransaction t = new KeyTransaction("_t4");
            executeWork( t, 60 );
        }).start();

        new Thread(() -> {
            KeyTransaction t = new KeyTransaction("_t5");
            executeWork( t, 30 );
        }).start();
    }

    public void executeWork( Object initiator, long mils ){

        TransactionObservable.startRequest(initiator,
                TransactionObservable.asObservable()
                        .filter(transactionEvent -> {
                            return transactionEvent.getState()== TransactionEvent.START_TRANSACTION && transactionEvent.getTarget() == initiator;
                        })
                        .subscribe(o -> {

                            try {
                                Thread.sleep( mils );
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            System.out.println( "completed " + initiator.toString() );
                            TransactionObservable.endRequest(initiator);
                        })
        );
    }

    @Test
    public void shouldBeRealmObjectAsObservable() {

        MockRealm.clearData();

        Dog asyncDog = realm.where(Dog.class).equalTo("age", 2).findFirstAsync();

        asyncDog.<Dog>asObservable().subscribe(thisDog -> {
            System.out.println("realmObject " + thisDog.toString());
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


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate(new Date(2015, 6, 10));
        realm.commitTransaction();


        realm.beginTransaction();
        dog = realm.where(Dog.class).equalTo("name", "Hernan Fernandez").findFirst();
        dog.deleteFromRealm();
        realm.commitTransaction();
    }

    @Test
    public void shouldBeRealmResultsAsObservable() {

        MockRealm.clearData();

        RealmResults<Dog> asyncDog = realm.where(Dog.class).equalTo("age", 2).findAllAsync();

        asyncDog.<RealmResults<Dog>>asObservable().subscribe(theseDogs -> {
            System.out.println("realmObject " + theseDogs);
        }, throwable -> {
            System.err.println( throwable.getMessage() );
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


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Aaron Hernandez");
        dog.setBirthdate(new Date(2015, 6, 10));
        realm.commitTransaction();


        realm.beginTransaction();
        dog = realm.where(Dog.class).equalTo("name", "Hernan Fernandez").findFirst();
        dog.deleteFromRealm();
        realm.commitTransaction();
    }
}