package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.test.MockRealmTester;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Juan Mendez on 3/22/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmTests  extends MockRealmTester{

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