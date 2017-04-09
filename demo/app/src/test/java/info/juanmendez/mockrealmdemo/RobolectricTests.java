package info.juanmendez.mockrealmdemo;

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
import info.juanmendez.mockrealm.models.RealmAnnotation;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Juan Mendez on 2/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class,
                 RealmList.class, RealmCore.class, RealmObject.class, RealmDependencies.class })
public class RobolectricTests {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Realm realm;
    MainActivity activity;

    static{
        ShadowLog.stream = System.out;
    }

    @Before
    public void before() throws Exception {

        MockRealm.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        MockRealm.addAnnotations( RealmAnnotation.build(Dog.class)
                        .primaryField("id")
                        .indexedFields("name", "age", "birthdate", "nickname"),
                RealmAnnotation.build(Person.class)
                        .primaryField("id")
                        .indexedFields("name"));


        realm = Realm.getDefaultInstance();
        activity = Robolectric.setupActivity( MainActivity.class );
    }

    /**
     * RealmDependencies is added as part of @PrepareForTest in this way
     * realm configuration is mocked!
     */
    @Test
    public void shouldAssertWhatsOnMainActivity(){

        MockRealm.clearData();
        activity.shouldShowChangesFromRealmResultsWithAsyncTransactions();

        assertEquals( "The current message is ", "There are 3 dogs", activity.textView.getText() );
    }

    @Test
    public void shouldEnsureDistinctinRealmResults(){
        MockRealm.clearData();
        activity.shouldDoDistinctIn_realmResults();

        assertEquals( "There are 6 dogs ", "We found " + 5 + " with distinct names, and birthdays!", activity.textView.getText() );
    }

    @Test
    public void shouldSortPersonsByTheirFavoriteDogs(){
        MockRealm.clearData();
        activity.shouldSort();
    }
}