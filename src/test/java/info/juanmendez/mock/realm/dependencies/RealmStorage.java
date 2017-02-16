package info.juanmendez.mock.realm.dependencies;

import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.HashMap;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmStorage {

    private static HashMap<Class, RealmList<RealmObject>> realmMap = new HashMap<>();
    private static HashMap<Class, RealmList<RealmObject> > queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmObject.*/
    public static HashMap<Class, RealmList<RealmObject>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<Class, RealmList<RealmObject>> getQueryMap() {
        return queryMap;
    }
}
