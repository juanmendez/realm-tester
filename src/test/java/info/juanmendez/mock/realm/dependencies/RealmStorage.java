package info.juanmendez.mock.realm.dependencies;

import io.realm.RealmList;
import io.realm.RealmModel;
import java.util.HashMap;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmStorage {

    private static HashMap<Class, RealmList<RealmModel>> realmMap = new HashMap<>();
    private static HashMap<Class, RealmList<RealmModel> > queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmModel.*/
    public static HashMap<Class, RealmList<RealmModel>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<Class, RealmList<RealmModel>> getQueryMap() {
        return queryMap;
    }
}
