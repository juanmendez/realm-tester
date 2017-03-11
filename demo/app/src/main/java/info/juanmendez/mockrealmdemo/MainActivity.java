package info.juanmendez.mockrealmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Date;

import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //configurations throw errors.
        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

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
}
