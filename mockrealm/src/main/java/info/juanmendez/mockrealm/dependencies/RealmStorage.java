package info.juanmendez.mockrealm.dependencies;

import java.util.HashMap;

import info.juanmendez.mockrealm.models.ModelEmit;
import info.juanmendez.mockrealm.utils.QueryHolder;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmStorage {

    private static HashMap<Class, RealmList<RealmModel>> realmMap = new HashMap<>();
    private static HashMap<RealmResults<RealmModel>, QueryHolder> queryMap = new HashMap<>();

    /*keeps collections keyed by a sub-class of RealmModel.*/
    public static HashMap<Class, RealmList<RealmModel>> getRealmMap() {
        return realmMap;
    }

    /*collections queried keyed by immediate class*/
    public static HashMap<RealmResults<RealmModel>, QueryHolder> getQueryMap() {
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

        RealmObservable.unsubscribe();
        realmMap.clear();
        queryMap.clear();
    }
}