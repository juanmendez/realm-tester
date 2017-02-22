package info.juanmendez.mock.realm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class MockRealm {

    public static void prepare() throws Exception {
            makeClassesStatic();
            //config();
    }

    private static void makeClassesStatic() throws Exception {
        mockStatic( RealmCore.class );
        mockStatic( Realm.class );
        mockStatic( RealmConfiguration.class);
        mockStatic( RealmQuery.class );
        mockStatic( RealmResults.class );
        mockStatic( RealmObject.class );
    }

    private static void config() throws Exception {

        final RealmConfiguration mockRealmConfig = mock(RealmConfiguration.class);

        // TODO: Better solution would be just mock the RealmConfiguration.Builder class. But it seems there is some
        // problems for powermock to mock it (static inner class). We just mock the RealmCore.loadLibrary(Context) which
        // will be called by RealmConfiguration.Builder's constructor.
        doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(any(Context.class));
    }
}