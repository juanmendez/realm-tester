package info.juanmendez.learn.powermock;

/**
 * Created by musta on 2/10/2017.
 */
public class StaticDog {

    private String name;

    private String secretlyTellMyName(){
        return "Winnie the Pooh";
    }

    public String tellThemMyName(){
        return secretlyTellMyName();
    }

    public static String sayStaticHello(){
        return "Hi from static doggie";
    }
}
