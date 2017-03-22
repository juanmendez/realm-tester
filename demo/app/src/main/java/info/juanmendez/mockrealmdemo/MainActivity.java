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
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    Realm realm;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //configurations throw errors.
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
    }

    void shouldQueryChain(){
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

    @Override
    protected void onPause(){
        super.onPause();
        realm.close();
    }
}
