package info.juanmendez.mock.realm;

import android.content.Context;
import io.realm.*;
import io.realm.internal.RealmCore;
import io.realm.log.RealmLog;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class MockRealm {

    public static void prepare(){
            makeClassesStatic();
            config();
    }

    private static void makeClassesStatic(){
        mockStatic( Realm.class );
        mockStatic( RealmConfiguration.class );
        mockStatic( RealmQuery.class );
        mockStatic( RealmResults.class );
        mockStatic( RealmCore.class );
        mockStatic( RealmLog.class );
        mockStatic( RealmAsyncTask.class );
        mockStatic( Realm.Transaction.class );
    }

    private static void config(){

        RealmConfiguration realmConfiguration = PowerMockito.mock(RealmConfiguration.class);
        PowerMockito.doNothing().when( RealmCore.class );
        RealmCore.loadLibrary( any( Context.class) );
    }
}