package info.juanmendez.learn.realms;

import io.realm.RealmObject;

/**
 * Created by musta on 2/10/2017.
 */

public class RealmDog extends RealmObject{
    private int id;
    private String name;
    private int age;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
