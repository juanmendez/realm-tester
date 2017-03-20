package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealmdemo.models.Cat;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Juan Mendez on 3/19/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
public class RealmModelTester {

    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
        realm = Realm.getDefaultInstance();
    }


    @Test
    public void shouldAddCatToRealm(){

        Cat cat = new Cat();

        realm.beginTransaction();
           realm.copyToRealm( cat );
        realm.commitTransaction();

        realm.where( Cat.class ).findAllAsync().addChangeListener( element -> {

            System.out.println( element );
        });

        realm.beginTransaction();
            RealmObject.deleteFromRealm( cat );
        realm.commitTransaction();
    }

}
