import info.juanmendez.learn.powermock.StaticDog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by musta on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(StaticDog.class)
public class Powermock_2_static_dog {

    StaticDog mockedDog;

    @Before
    public void before(){
        PowerMockito.mockStatic(StaticDog.class);
    }

    @Test
    public void fakeStaticReturnCall(){

        Mockito.when( StaticDog.sayStaticHello() ).thenReturn( "{I am your dog!}");
        StaticDog.sayStaticHello();
        PowerMockito.verifyStatic();
    }

    @Test
    public void modifyPrivateCallReturn() throws Exception {
        String fakeName= "meow meow";
        StaticDog dog = PowerMockito.spy( new StaticDog() );
        PowerMockito.doReturn( fakeName ).when( dog, "secretlyTellMyName" );
        dog.tellThemMyName();
        PowerMockito.verifyPrivate(dog, Mockito.times(1)).invoke("secretlyTellMyName");
    }
}
