import info.juanmendez.learn.powermock.QuietDog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by musta on 2/8/2017.
 */

@RunWith(PowerMockRunner.class)
public class Powermock_1_introduction {

    @Mock
    QuietDog mockedDog;

    @Test
    public void testMockito() throws Exception {

        when(mockedDog.helloMethod()).thenReturn("Hello Baeldung!");
        String welcome = mockedDog.helloMethod();
        Mockito.verify(mockedDog).helloMethod();
        assertEquals("Hello Baeldung!", welcome);
    }

    /**
     * https://github.com/powermock/powermock/wiki/MockitoUsage#a-full-example-of-spying
     */
    @Test
    public void testSpionage() throws Exception {
        String fakedBark = "{}";
        QuietDog powerMockedDog = PowerMockito.mock( QuietDog.class );
        PowerMockito.when( powerMockedDog.finalHelloMethod() ).thenReturn( fakedBark );
        assertEquals("same dog?", powerMockedDog.finalHelloMethod(), fakedBark);

    }

    /**
     * https://github.com/powermock/powermock/wiki/MockitoUsage#how-to-mock-construction-of-new-objects
     * @throws Exception
     */
    @Test
    public void testNewInstance() throws Exception {

        whenNew(QuietDog.class).withNoArguments().thenReturn(mockedDog);
        when( mockedDog.toString() ).thenReturn("QuietDog{FAKED_RETURN!}");

        QuietDog realDog = new QuietDog();
        assertEquals("same dog?", realDog, mockedDog);
    }


}
