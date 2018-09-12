package info.juanmendez.realmtester.demo;

import org.junit.Before;
import org.junit.Test;

import info.juanmendez.realmtester.RealmTester;
import info.juanmendez.realmtester.demo.models.RealmAnnotation;
import info.juanmendez.realmtester.test.RealmTesterBase;
import info.juanmendez.realmtester.demo.models.Dog;
import info.juanmendez.realmtester.demo.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Juan Mendez on 3/22/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmTestsBase extends RealmTesterBase {

    @Before
    public void before() throws Exception {
        RealmTester.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        RealmTester.addAnnotations(RealmAnnotation.build(Dog.class)
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