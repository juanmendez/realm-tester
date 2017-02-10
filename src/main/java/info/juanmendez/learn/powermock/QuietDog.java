package info.juanmendez.learn.powermock;

import info.juanmendez.learn.mockito.models.Pet;

/**
 * Created by musta on 2/9/2017.
 */
public class QuietDog{

    public final String finalHelloMethod() {
        return "QuietDog{FINAL}";
    }

    public String helloMethod() {
        return "QuietDog{}";
    }
}
