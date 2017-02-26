package info.juanmendez.mock.realm;

import info.juanmendez.mock.realm.factories.ConfigurationFactory;
import info.juanmendez.mock.realm.factories.ListFactory;
import info.juanmendez.mock.realm.factories.ModelFactory;
import info.juanmendez.mock.realm.factories.RealmFactory;
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

    public static void prepare() throws Exception {
        mockStatic( Realm.class );
        mockStatic( RealmConfiguration.class);
        mockStatic( RealmQuery.class );
        mockStatic( RealmResults.class );
        mockStatic( RealmObject.class );
        mockStatic( RealmList.class );

        ModelFactory.create();
        ListFactory.create();
        RealmFactory.create();
        ConfigurationFactory.create();
    }
}