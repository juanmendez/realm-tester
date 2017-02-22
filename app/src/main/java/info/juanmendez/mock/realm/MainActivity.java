package info.juanmendez.mock.realm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

import info.juanmendez.mock.realm.models.Dog;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm.init(getApplicationContext());
        Realm realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

        Dog dog = realm.createObject( Dog.class );
        dog.setName("Max");
        dog.setAge(1);
        dog.setId(1);
        dog.setBirthdate( new Date() );
    }
}
