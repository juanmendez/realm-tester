package info.juanmendez.mockrealmdemo.models;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.RealmClass;

@RealmClass
public class Cat implements RealmModel {
    private int id;
    private String name;
    private int age;
    private Date birthdate;

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

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
}
