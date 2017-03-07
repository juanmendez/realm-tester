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

import info.juanmendez.mock.realm.BuildConfig;
import info.juanmendez.mock.realm.MainActivity;
import info.juanmendez.mock.realm.MockRealm;
import info.juanmendez.mock.realm.R;
import info.juanmendez.mock.realm.factories.RealmFactory;
import info.juanmendez.mock.realm.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Juan Mendez on 2/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21 )
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
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
    public void shouldCreateADogInMainActivity(){

        MainActivity activity = Robolectric.setupActivity( MainActivity.class );

        assertEquals( "same print", ((TextView)activity.findViewById(R.id.textView)).getText(), "Hello World!");
        System.out.println( "number of people here " + realm.where(Person.class).count() );
        assertEquals( "There is one person found", realm.where( Person.class ).count(), 1 );
    }
}