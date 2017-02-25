package info.juanmendez.mock.realm.dependencies;

import io.realm.RealmObject;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class MockUtils {
    
    public static Class getClass( Object realmModel ){

        Class clazz = realmModel.getClass();

        if( realmModel instanceof RealmObject){
            return clazz.getSuperclass();
        }

        return clazz;
    }
}
