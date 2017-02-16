package info.juanmendez.mock.realm.dependencies;

import io.realm.RealmObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmStorage {

    private static HashMap<Class, ArrayList<RealmObject>> realmMap = new HashMap<>();
    private static HashMap<Class, ArrayList<RealmObject> > queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmObject.*/
    public static HashMap<Class, ArrayList<RealmObject>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<Class, ArrayList<RealmObject>> getQueryMap() {
        return queryMap;
    }
}
