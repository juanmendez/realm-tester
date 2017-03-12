package info.juanmendez.mockrealm;

import info.juanmendez.mockrealm.decorators.RealmConfigurationDecorator;
import info.juanmendez.mockrealm.decorators.RealmDecorator;
import info.juanmendez.mockrealm.decorators.RealmListDecorator;
import info.juanmendez.mockrealm.decorators.RealmModelDecorator;
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

        RealmListDecorator.prepare();
        RealmModelDecorator.prepare();
        RealmDecorator.prepare();
        RealmConfigurationDecorator.prepare();
    }
}