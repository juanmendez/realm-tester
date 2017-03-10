package info.juanmendez.mockrealm;

import info.juanmendez.mockrealm.factories.ConfigurationFactory;
import info.juanmendez.mockrealm.factories.ListFactory;
import info.juanmendez.mockrealm.factories.ModelFactory;
import info.juanmendez.mockrealm.factories.RealmFactory;
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
        mockStatic( RealmList.class );
        mockStatic( Realm.class );
        mockStatic( RealmConfiguration.class);
        mockStatic( RealmQuery.class );
        mockStatic( RealmResults.class );
        mockStatic( RealmObject.class );

        ListFactory.prepare();
        ModelFactory.prepare();
        RealmFactory.prepare();
        ConfigurationFactory.prepare();
    }
}