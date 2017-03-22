package info.juanmendez.mockrealmdemo;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;

/**
 * Created by Juan Mendez on 2/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmCore.class, RealmObject.class })
public class RobolectricTests {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public Realm realm;

    static{
        ShadowLog.stream = System.out;
    }

    @Before
    public void before() throws Exception {

        MockRealm.prepare();
        realm = Realm.getDefaultInstance();
    }

    @Test
    public void shouldAssertWhatsOnMainActivity(){

        RealmStorage.clear();
        MainActivity activity = Robolectric.setupActivity( MainActivity.class );
        activity.shouldShowChangesFromRealmResultsWithAsyncTransactions();

        Assert.assertEquals( "The current message is ", "There are 3 dogs", activity.textView.getText() );
    }
}