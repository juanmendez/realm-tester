package info.juanmendez.mockrealm.dependencies;

import java.util.HashMap;

import info.juanmendez.mockrealm.models.QueryNest;
import info.juanmendez.mockrealm.models.ModelEmit;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.exceptions.RealmException;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmStorage {

    private static HashMap<Class, RealmList<RealmModel>> realmMap = new HashMap<>();
    private static HashMap<RealmQuery, QueryNest> queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmModel.*/
    public static HashMap<Class, RealmList<RealmModel>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<RealmQuery, QueryNest> getQueryMap() {
        return queryMap;
    }

    public static void removeModel( RealmModel realmModel ){

        if( realmModel != null ){

            Class clazz = MockUtils.getClass(realmModel);

            if( RealmModel.class.isAssignableFrom(clazz) ){

                if( realmMap.get(clazz) != null && realmMap.get(clazz).contains( realmModel ) ){
                    realmMap.get( clazz ).remove( realmModel );
                    RealmObservable.onNext( new ModelEmit( ModelEmit.REMOVED, realmModel ) );
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
                    RealmObservable.onNext( new ModelEmit( ModelEmit.ADDED, realmModel ) );
                }else{
                    throw new RealmException( "Instance of " + clazz.getName() + " cannot be added more than once" );
                }
            }else{

                throw new RealmException( clazz.getName() + " is not an instance of RealmModel"  );
            }
        }
    }

    public static void clear(){
        realmMap.clear();
        queryMap.clear();
        RealmObservable.unsubscribe();
    }
}