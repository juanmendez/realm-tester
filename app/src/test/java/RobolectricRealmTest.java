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

import info.juanmendez.mock.realm.BuildConfig;
import info.juanmendez.mock.realm.MainActivity;
import info.juanmendez.mock.realm.MockRealm;
import info.juanmendez.mock.realm.R;
import info.juanmendez.mock.realm.dependencies.RealmStorage;
import info.juanmendez.mock.realm.factories.RealmFactory;
import info.juanmendez.mock.realm.models.Dog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;

import static org.junit.Assert.assertEquals;

/**
 * Created by Juan Mendez on 2/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class, RealmObject.class })
public class RobolectricRealmTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
        Realm realm = RealmFactory.create();
    }

    @Test
    public void shouldHaveLabel(){

        MainActivity activity = Robolectric.setupActivity( MainActivity.class );
        TextView textView = (TextView) activity.findViewById( R.id.textView);
        assertEquals( "main activity entered one dog", RealmStorage.getRealmMap().get(Dog.class).size(), 1);
    }
}
