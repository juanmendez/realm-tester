package info.juanmendez.mockrealmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;

import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    Realm realm;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Configurations must be part of a non-Android component class.
         * And such dependency must be declared at @PrepareForTest eg. RobolectricTests
         */
        RealmDependencies.createConfig(this);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        shouldSort();
    }

    void shouldQueryChain(){
        realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(thisRealm -> {
            Dog dog = thisRealm.createObject( Dog.class );
            dog.setName("Max");
            dog.setAge(1);
            dog.setId(1);
            dog.setBirthdate( new Date() );

            Person person = thisRealm.createObject( Person.class );
            person.setDogs( new RealmList<>(dog));
        }, () -> {
            Log.i( "MainActivity", "Number of people: " + realm.where(Person.class).count() );
        }, error -> {
            Log.e( "MainActivity", "There was an error completing transaction" + error.getMessage() );
        });
    }

    void shouldShowChangesFromRealmResultsWithAsyncTransactions(){

        realm = Realm.getDefaultInstance();
        RealmResults<Dog> results = realm.where( Dog.class ).findAllAsync();

        results.addChangeListener((RealmChangeListener<RealmResults<Dog>>) dogs -> {
            textView.setText( "There are " + dogs.size() + " dogs" );
        });

        realm.executeTransactionAsync( realm1 -> {

            System.out.println( "executeTransactionAsync thread" + Thread.currentThread().getName() );
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

            System.out.println( "executeTransactionAsync thread" + Thread.currentThread().getName() );
            Dog dog;
            dog = realm1.createObject(Dog.class);
            dog.setAge(2);
            dog.setName("Aaron Hernandez");
            dog.setBirthdate( new Date(2015, 6, 10));
        });
    }

    public void shouldDoDistinctIn_realmResults(){

        //lets do this first with realmList
        realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        Dog dog;
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate( new Date(2016, 6, 9));


        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate( new Date(2016, 6, 9));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate( new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Hernan");
        dog.setBirthdate( new Date(2012, 2, 1));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate( new Date(2015, 2, 1));

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate( new Date(2014, 2, 1));

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate( new Date(2005, 2, 1));

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate( new Date(2007, 2, 1));
        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class).findAll().distinct("name").distinct("age");
        textView.setText("We found " + dogs.size() + " with distinct names, and birthdays!" );
    }

    public void shouldSort(){
        realm = Realm.getDefaultInstance();


        Dog dog;
        Person person;

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate( new Date(2016, 6, 9));
        Dog idalgo = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate( new Date(2016, 6, 9));
        Dog fido = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate( new Date(2015, 6, 10));
        Dog hernan  = dog;


        dog = realm.createObject(Dog.class);
        Dog nully  = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate( new Date(2015, 2, 1));
        Dog chibi = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate( new Date(2014, 2, 1));
        Dog andromeda = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate( new Date(2005, 2, 1));
        Dog baxter = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate( new Date(2007, 2, 1));
        Dog beethoven = dog;
        realm.commitTransaction();

        realm.beginTransaction();
        person = realm.createObject( Person.class );
        person.setName("Chiu-Ki");
        person.setFavoriteDog( nully );

        person = realm.createObject( Person.class );
        person.setName("Karl");
        person.setFavoriteDog( andromeda );
        person.setDogs(new RealmList<>( beethoven, baxter, hernan, nully ));


        person = realm.createObject( Person.class );
        person.setName("Jimmy");
        person.setFavoriteDog( baxter );
        person.setDogs(new RealmList<>( chibi, andromeda, fido, baxter ));

        person = realm.createObject( Person.class );
        person.setName("Donn");
        person.setFavoriteDog( fido );
        person.setDogs(new RealmList<>( idalgo, baxter, andromeda, nully, chibi  ));

        person = realm.createObject( Person.class );
        person.setName("Mark");
        person.setFavoriteDog( chibi );
        person.setDogs(new RealmList<>( chibi, nully, andromeda, baxter  ));



        realm.commitTransaction();



        RealmResults<Person> unsorted = realm.where( Person.class ).findAll();
        RealmResults<Person> sorted = realm.where( Person.class ).findAllSorted( new String[]{"favoriteDog.name", "favoriteDog.age"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
        for( RealmModel p: sorted ){
            Log.i( "MainActivity", ((Person)p).getName() );
            Log.i( "MainActivity", "Favorite dog: " + ((Person)p).getFavoriteDog().getName() + ", age: " + ((Person)p).getFavoriteDog().getAge() );

        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        if( realm != null )
            realm.close();
    }
}