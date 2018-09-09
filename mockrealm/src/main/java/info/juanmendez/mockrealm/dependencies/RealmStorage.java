package info.juanmendez.mockrealm.dependencies;

import java.util.HashMap;

import info.juanmendez.mockrealm.decorators.RealmObjectDecorator;
import info.juanmendez.mockrealm.decorators.RealmResultsDecorator;
import info.juanmendez.mockrealm.models.RealmAnnotation;
import info.juanmendez.mockrealm.models.RealmEvent;
import info.juanmendez.mockrealm.utils.RealmModelUtil;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

public class RealmStorage {

    private static HashMap<Class, RealmList<RealmModel>> realmMap = new HashMap<>();
    private static HashMap<Class, RealmAnnotation> annotationMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmModel.*/
    public static HashMap<Class, RealmList<RealmModel>> getRealmMap() {
        return realmMap;
    }

    public static void removeModel(RealmModel realmModel) {

        if (realmModel != null) {

            Class clazz = RealmModelUtil.getClass(realmModel);

            if (RealmModel.class.isAssignableFrom(clazz)) {

                if (realmMap.get(clazz) != null && realmMap.get(clazz).contains(realmModel)) {
                    realmMap.get(clazz).remove(realmModel);
                    RealmObservable.onNext(new RealmEvent(RealmEvent.MODEL_REMOVED, realmModel));
                } else {
                    throw new RealmException("Instance of " + clazz.getName() + " cannot be deleted as it's not part of the realm database");
                }

            } else {

                throw new RealmException(clazz.getName() + " is not an instance of RealmModel");
            }
        }
    }

    public static void addModel(RealmModel realmModel) {

        if (realmModel != null) {

            Class clazz = RealmModelUtil.getClass(realmModel);

            if (RealmModel.class.isAssignableFrom(clazz)) {

                if (!realmMap.get(clazz).contains(realmModel)) {
                    realmMap.get(RealmModelUtil.getClass(realmModel)).add(realmModel);
                    RealmObservable.onNext(new RealmEvent(RealmEvent.MODEL_ADDED, realmModel));
                } else {
                    System.out.println("#mocking-realm: Instance of " + clazz.getName() + " cannot be added more than once");
                }
            } else {

                throw new RealmException(clazz.getName() + " is not an instance of RealmModel");
            }
        }
    }

    public static void clear() {
        RealmObservable.removeSubscriptions();
        TransactionObservable.removeSubscriptions();
        RealmObjectDecorator.removeSubscriptions();
        RealmResultsDecorator.removeSubscriptions();
        realmMap.clear();
    }

    public static void addAnnotations(RealmAnnotation realmAnnotation) {
        annotationMap.put(realmAnnotation.getClazz(), realmAnnotation);
    }

    public static HashMap<Class, RealmAnnotation> getAnnotationMap() {
        return annotationMap;
    }
}