package info.juanmendez.mockrealm.decorators;

import io.realm.RealmConfiguration;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
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
        whenNew(RealmConfiguration.Builder.class).withNoArguments().thenReturn(mockedBuilder);

        when( mockedBuilder.build()).thenReturn( mockRealmConfig );
    }
}