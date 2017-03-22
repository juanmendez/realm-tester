package info.juanmendez.mockrealmdemo;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Juan Mendez on 3/22/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmDependencies {

    public  static void createConfig(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("realmconfiguration.realm")
                .build();

        Realm.deleteRealm(realmConfiguration); // Clean slate
        Realm.setDefaultConfiguration(realmConfiguration); // Make this Realm the default
    }
}
