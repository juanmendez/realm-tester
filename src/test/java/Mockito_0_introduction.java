import info.juanmendez.learn.mockito.models.PetImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by musta on 2/7/2017.
 */
public class Mockito_0_introduction {

    /**shorthand for mock creations*/
    @Mock
    private PetImpl pet;

    @Captor
    private ArgumentCaptor<String> captor;

    List mockedList;

    @Before
    public void before(){

        mockedList = mock( List.class );

        //Important configuation to inject mock creations
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mockito_demo(){

        mockedList.add("one");
        mockedList.add("two");
        mockedList.clear();

        verify( mockedList ).add("one");
        verify( mockedList ).clear();
    }

    @Test
    public void mockMyPet(){

        /**
         java.lang.AssertionError: Age is 1
         Expected :0
         Actual   :1
         */
        assertEquals( "Age is 1", pet.getAge(), 1);
    }

    @Test
    public void mockMyPet_fix(){
        when(pet.getAge()).thenReturn(1);
        assertEquals( "Age is 1", pet.getAge(), 1);
    }

    /*
    this time lets do partial implementation with spy
    spy can also be included with @spy
     */
    @Test
    public void spy_on_pet(){
        PetImpl pet = spy( new PetImpl() );

        //works!
        assertEquals( "Age is 1", pet.getAge(), 1);

        //lets manipulate, we want age to be 2 instead of 1.
        //rather than when().thenReturn() we do doReturn().when().methodCalled()
        doReturn( 2 ).when( pet ).getAge();
        assertEquals( "Age is 2", pet.getAge(), 2);
    }

    @Test
    public void mockPetWithInterface(){
        /**
         java.lang.AssertionError: Name as expected
         Expected :null
         Actual   :Amelia
         */
        pet.setName( "Amelia");
        assertEquals( "Name as expected", pet.getName(), "Amelia");
    }


    /**
     * take a snapshot of your argument passed.
     */
    @Test
    public void captorTest(){

        pet.setName( "Amelia");
        verify( pet ).setName( captor.capture() );

        assertEquals( "Amelia", captor.getValue() );
    }

    @Test
    public void then_Answer(){
        when( pet.updateAnswer(any(), any())).thenAnswer( answerThem());

        assertEquals("return must be", "answer1 x answer2", pet.updateAnswer( "answer1", "answer2"));
    }

    private Answer<String> answerThem(){
        return new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                String string1 = (String) invocationOnMock.getArguments()[0];
                String string2 = (String) invocationOnMock.getArguments()[1];

                return string1 + " x " + string2;
            }
        };
    }
}
