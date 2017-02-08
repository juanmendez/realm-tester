package info.juanmendez.learn.mockito.models;

/**
 * Created by musta on 2/7/2017.
 */
public interface Pet {
    int getAge();
    void setAge(int age);

    String getName();
    void setName( String name );

    String getType();
    void setType( String type );
}
