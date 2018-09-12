package info.juanmendez.realmtester.demo.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Person extends RealmObject {

    @PrimaryKey
    private int id;

    @Index
    private String name;

    private RealmList<Dog> dogs = new RealmList<>();
    private Dog favoriteDog;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(RealmList<Dog> dogs) {
        this.dogs = dogs;
    }

    public Dog getFavoriteDog() {
        return favoriteDog;
    }

    public void setFavoriteDog(Dog favoriteDog) {
        this.favoriteDog = favoriteDog;
    }
}
