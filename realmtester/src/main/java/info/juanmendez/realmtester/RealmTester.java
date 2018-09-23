package info.juanmendez.realmtester;

import info.juanmendez.realmtester.decorators.RealmConfigurationDecorator;
import info.juanmendez.realmtester.decorators.RealmDecorator;
import info.juanmendez.realmtester.decorators.RealmListDecorator;
import info.juanmendez.realmtester.decorators.RealmModelDecorator;
import info.juanmendez.realmtester.decorators.RealmObjectDecorator;
import info.juanmendez.realmtester.dependencies.RealmStorage;
import info.juanmendez.realmtester.demo.models.RealmAnnotation;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

public class RealmTester {

    /**
     * This is a method required in order to start up
     * testing.
     *
     * @throws Exception
     */
    public static void prepare() throws Exception {
        mockStatic(RealmList.class);
        mockStatic(Realm.class);
        mockStatic(RealmConfiguration.class);
        mockStatic(RealmQuery.class);
        mockStatic(RealmResults.class);
        mockStatic(RealmObject.class);

        RealmListDecorator.prepare();
        RealmModelDecorator.prepare();
        RealmObjectDecorator.prepare();
        RealmDecorator.prepare();
        RealmConfigurationDecorator.prepare();
    }

    /**
     * Make sure to include each of your class annotation references through RealmAnnotation
     * before testing each type of realmModel in your project
     *
     * @param annotations
     */
    public static void addAnnotations(RealmAnnotation... annotations) {
        for (RealmAnnotation annotation : annotations) {
            RealmStorage.addAnnotations(annotation);
        }
    }

    /**
     * Call this method each time you want to clear your realm entries;
     * specially, when starting a new test.
     */
    public static void clearData() {
        RealmStorage.clear();
    }
}