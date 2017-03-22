package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import info.juanmendez.mockrealm.MockRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Juan Mendez on 3/22/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
public class RealmTests {

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
    }

    @Test
    public void shouldBuilderPass(){

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        assertNotNull( "Builder exists", builder);
    }

    @Test
    public void shouldBuilderBuildPass(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        assertNotNull( "Builder exists", realmConfiguration);
    }

    @Test
    public void shouldBuildNamePass(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name("mocking-realm").build();
        assertNotNull( "Builder exists", realmConfiguration);
    }

    @Test
    public void shouldBeSameRealm(){
        assertNotNull("is the same?", Realm.getDefaultInstance());
    }
}