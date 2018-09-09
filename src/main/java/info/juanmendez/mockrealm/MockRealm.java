package info.juanmendez.mockrealm;

import info.juanmendez.mockrealm.decorators.RealmConfigurationDecorator;
import info.juanmendez.mockrealm.decorators.RealmDecorator;
import info.juanmendez.mockrealm.decorators.RealmListDecorator;
import info.juanmendez.mockrealm.decorators.RealmModelDecorator;
import info.juanmendez.mockrealm.decorators.RealmObjectDecorator;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.RealmAnnotation;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class MockRealm {

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