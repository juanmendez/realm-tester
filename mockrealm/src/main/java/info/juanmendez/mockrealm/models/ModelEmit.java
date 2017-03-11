package info.juanmendez.mockrealm.models;

import io.realm.RealmModel;

/**
 * Created by Juan Mendez on 3/10/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * Depicts the state of a realmModel if it's been added or removed
 */

public class ModelEmit {
    public static final String ADDED = "added";
    public static final String REMOVED = "removed";

    private String state;
    private RealmModel realmModel;

    public ModelEmit(String state, RealmModel realmModel) {
        this.state = state;
        this.realmModel = realmModel;
    }

    public String getState() {
        return state;
    }

    public RealmModel getRealmModel() {
        return realmModel;
    }
}
