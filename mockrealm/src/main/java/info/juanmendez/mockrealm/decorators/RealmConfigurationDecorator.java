package info.juanmendez.mockrealm.decorators;

import java.io.File;

import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.rx.RxObservableFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by Juan Mendez on 2/23/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmConfigurationDecorator {


    public static void prepare() throws Exception {

        RealmConfiguration mockRealmConfig = mock(RealmConfiguration.class);

        // make a mockery of our inner class
        RealmConfiguration.Builder mockedBuilder = mock(RealmConfiguration.Builder.class);

        // magically return the mock when a new instance is required
        whenNew(RealmConfiguration.Builder.class).withNoArguments().thenAnswer(invocation -> mockedBuilder);
        whenNew(RealmConfiguration.Builder.class).withAnyArguments().thenAnswer(invocation -> mockedBuilder);
        doAnswer(invocation -> mockRealmConfig ).when(mockedBuilder ).build();

        //what to do with builder configs
        doReturn(mockedBuilder).when( mockedBuilder ).name(anyString());
        doReturn(mockedBuilder).when( mockedBuilder ).directory(any(File.class));
        doReturn(mockedBuilder).when( mockedBuilder ).encryptionKey(any());
        doReturn(mockedBuilder).when( mockedBuilder ).schemaVersion(anyLong());
        doReturn(mockedBuilder).when( mockedBuilder ).migration(any(RealmMigration.class));
        doReturn(mockedBuilder).when( mockedBuilder ).deleteRealmIfMigrationNeeded();
        doReturn(mockedBuilder).when( mockedBuilder ).inMemory();
        doReturn(mockedBuilder).when( mockedBuilder ).modules(any(), anyVararg());
        doReturn(mockedBuilder).when( mockedBuilder ).rxFactory(any(RxObservableFactory.class));
        doReturn(mockedBuilder).when( mockedBuilder ).assetFile(anyString());
    }
}