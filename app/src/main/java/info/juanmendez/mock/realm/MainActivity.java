package info.juanmendez.mock.realm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Date;

import info.juanmendez.mock.realm.models.Dog;
import info.juanmendez.mock.realm.models.Person;
import io.realm.Realm;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Realm.init(this);

        //RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        //Realm.setDefaultConfiguration( configuration );
        //Realm.deleteRealm( configuration );


        Realm realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Dog dog = realm.createObject( Dog.class );
                dog.setName("Max");
                dog.setAge(1);
                dog.setId(1);
                dog.setBirthdate( new Date() );

                Person person = realm.createObject( Person.class );
                person.setDogs( new RealmList<>(dog));

                dog.deleteFromRealm();
            }
        }, () -> {
            Log.i( "MainActivity", "Number of dogs: " + realm.where(Person.class).findFirst() );
        }, error -> {
            Log.e( "MainActivity", "There was an error completing transaction" + error.getMessage() );
        });
    }
}
