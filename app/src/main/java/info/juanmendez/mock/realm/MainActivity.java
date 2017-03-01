package info.juanmendez.mock.realm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

import info.juanmendez.mock.realm.models.Dog;
import info.juanmendez.mock.realm.models.Person;
import io.realm.Realm;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Realm.init(this);

        //RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        //Realm.setDefaultConfiguration( configuration );
        //Realm.deleteRealm( configuration );

        Realm realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

        Dog dog = realm.createObject( Dog.class );
        dog.setName("Max");
        dog.setAge(1);
        dog.setId(1);
        dog.setBirthdate( new Date() );

        Person person = realm.createObject( Person.class );
        person.setDogs( new RealmList<>(dog));

    }
}
