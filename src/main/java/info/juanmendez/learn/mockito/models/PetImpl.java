package info.juanmendez.learn.mockito.models;

/**
 * Created by musta on 2/7/2017.
 */
public class PetImpl implements Pet {

    //default is 1, but would the mocked pet give back the same value?
    private int age=1;
    private String name;
    private String type;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}