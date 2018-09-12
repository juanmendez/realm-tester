package info.juanmendez.realmtester.demo;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmDependencies {

    public static void createConfig(Context context) {
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("realmconfiguration.realm")
                .build();

        Realm.deleteRealm(realmConfiguration); // Clean slate
        Realm.setDefaultConfiguration(realmConfiguration); // Make this Realm the default
    }
}
