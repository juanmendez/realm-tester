package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.models.RealmAnnotation;
import info.juanmendez.mockrealm.test.MockRealmTester;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Juan Mendez on 3/22/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmTests extends MockRealmTester {

    @Before
    public void before() throws Exception {
        MockRealm.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        MockRealm.addAnnotations(RealmAnnotation.build(Dog.class)
                        .primaryField("id")
                        .indexedFields("name", "age", "birthdate", "nickname"),
                RealmAnnotation.build(Person.class)
                        .primaryField("id")
                        .indexedFields("name"));
    }

    @Test
    public void shouldBuilderPass() {

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        assertNotNull("Builder exists", builder);
    }

    @Test
    public void shouldBuilderBuildPass() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        assertNotNull("Builder exists", realmConfiguration);
    }

    @Test
    public void shouldBuildNamePass() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name("mocking-realm").build();
        assertNotNull("Builder exists", realmConfiguration);
    }

    @Test
    public void shouldBeSameRealm() {
        assertNotNull("is the same?", Realm.getDefaultInstance());
    }
}