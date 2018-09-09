package info.juanmendez.mockrealm.models;

import io.realm.RealmModel;

/**
 * Created by Juan Mendez on 3/10/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 * <p>
 * It captures a change in realm
 */

public class RealmEvent {
    public static final String MODEL_ADDED = "RealmModelAdded";
    public static final String MODEL_REMOVED = "RealmModelRemoved";

    private String state;
    private RealmModel realmModel;

    public RealmEvent(String state) {
        this.state = state;
    }

    public RealmEvent(String state, RealmModel realmModel) {
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
