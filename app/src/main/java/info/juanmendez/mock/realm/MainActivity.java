package info.juanmendez.mock.realm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Dog dog = realm.createObject( Dog.class );
                dog.setName("Max");
                dog.setAge(1);
                dog.setId(1);
                dog.setBirthdate( new Date() );
            }
        }, () -> {
            Log.i( "MainActivity", "Number of dogs: " + realm.where(Dog.class).count() );
        }, error -> {
            Log.e( "MainActivity", "There was an error completing transaction" + error.getMessage() );
        });

    }
}
