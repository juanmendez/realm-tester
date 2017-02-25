package info.juanmendez.mock.realm.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by musta on 2/19/2017.
 */
public class Person extends RealmObject {
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
