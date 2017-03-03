import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Date;

import info.juanmendez.mock.realm.BuildConfig;
import info.juanmendez.mock.realm.MainActivity;
import info.juanmendez.mock.realm.MockRealm;
import info.juanmendez.mock.realm.R;
import info.juanmendez.mock.realm.factories.RealmFactory;
import info.juanmendez.mock.realm.models.Dog;
import info.juanmendez.mock.realm.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by Juan Mendez on 2/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmCore.class, RealmObject.class })
public class RobolectricRealmTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public Realm realm;

    static{
        ShadowLog.stream = System.out;
    }

    @Before
    public void before() throws Exception {

        MockRealm.prepare();
        RealmFactory.setTransactionScheduler(Schedulers.computation());
        RealmFactory.setResponseScheduler(AndroidSchedulers.mainThread());
        realm = Realm.getDefaultInstance();
    }

    @Test
    public void shouldMockConfiguration() throws Exception {

        // yey, no UnsupportedOperationException here!
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        assertNotNull(builder.build());
    }

    @Test
    public void shouldCreateADogInMainActivity(){

        MainActivity activity = Robolectric.setupActivity( MainActivity.class );

        assertEquals( "same print", ((TextView)activity.findViewById(R.id.textView)).getText(), "Hello World!");
        System.out.println( "number of people here " + realm.where(Person.class).count() );
    }

    @Test
    public void shouldWorkWithSpy(){
        RealmList<RealmModel> list = new RealmList<>();
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Dog() );
        list.add( new Dog() );

        System.out.println( list.size() );
        for( RealmModel dog: list ){
            System.out.println( dog.toString() );
        }
    }

    @Test
    public void shouldWorkBetterThanMainActivity(){
        Dog dog = realm.createObject( Dog.class );
        dog.setName("Max");
        dog.setAge(1);
        dog.setId(1);
        dog.setBirthdate( new Date() );

        Person person = realm.createObject( Person.class );
        person.setDogs( new RealmList<>(dog));

        System.out.println( "number of people " + realm.where(Person.class).count() );
        System.out.println( "number of people " + realm.where(Person.class).count() );
        System.out.println( "number of people " + realm.where(Person.class).count() );
    }
}
