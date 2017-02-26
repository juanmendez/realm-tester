package info.juanmendez.mock.realm.dependencies;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
public class MockUtils {

    public static Class getClass( Object object ){

        Class clazz = object.getClass();

        if( object instanceof RealmObject || object instanceof RealmList ){
            return clazz.getSuperclass();
        }

        return clazz;
    }
}