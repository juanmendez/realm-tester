package info.juanmendez.mockrealm.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.realm.RealmModel;

/**
 * Created by Juan Mendez on 3/14/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelUtils {

    public static RealmModel fromClass( Class clazz ) {
        Constructor constructor = null;
        RealmModel realmModel = null;

        try {
            constructor = clazz.getConstructor();
            realmModel = (RealmModel) constructor.newInstance();
        }  catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return realmModel;
    }
}
