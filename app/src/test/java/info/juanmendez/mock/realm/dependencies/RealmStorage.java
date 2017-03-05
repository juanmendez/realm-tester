package info.juanmendez.mock.realm.dependencies;

import java.util.HashMap;

import info.juanmendez.mock.realm.models.Query;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;
import rx.subjects.PublishSubject;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 *
 * TODO: for quick observing realmModels being removed, some functionality remained here but should be
 * sealed in its own class
 */
public class RealmStorage {

    private static PublishSubject<RealmModel> deleteSubject = PublishSubject.create();


    private static HashMap<Class, RealmList<RealmModel>> realmMap = new HashMap<>();
    private static HashMap<Class, Query> queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmModel.*/
    public static HashMap<Class, RealmList<RealmModel>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<Class, Query> getQueryMap() {
        return queryMap;
    }

    public static PublishSubject<RealmModel> getDeleteObservable() {
        return deleteSubject;
    }

    public static void removeModel( RealmModel realmModel ){

        if( realmModel != null ){

            Class clazz = MockUtils.getClass(realmModel);

            if( RealmModel.class.isAssignableFrom(clazz) ){

                if( realmMap.get(clazz) != null && realmMap.get(clazz).contains( realmModel ) ){
                    deleteSubject.onNext( realmModel );
                    realmMap.get( clazz ).remove( realmModel );
                }else{
                    throw new RealmException( "Instance of " + clazz.getName() + " cannot be deleted as it's not part of the realm database" );
                }

            }else{

                throw new RealmException( clazz.getName() + " is not an instance of RealmModel"  );
            }
        }
    }

    public static void addModel( RealmModel realmModel ){

        if( realmModel != null ){

            Class clazz = MockUtils.getClass(realmModel);

            if( RealmModel.class.isAssignableFrom(clazz) ){

                if( !realmMap.get(clazz).contains( realmModel ) ){
                    realmMap.get(MockUtils.getClass(realmModel) ).add( realmModel );
                }else{
                    throw new RealmException( "Instance of " + clazz.getName() + " cannot be added more than once" );
                }
            }else{

                throw new RealmException( clazz.getName() + " is not an instance of RealmModel"  );
            }
        }
    }
}