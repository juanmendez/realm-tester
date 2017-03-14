package info.juanmendez.mockrealm.dependencies;

import java.util.HashMap;

import info.juanmendez.mockrealm.models.ModelEmit;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Juan Mendez on 3/10/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * Provides a subject which can be observed whenever realm is getting a realmModel added or removed
 * Elements emitted are of type ModelEmit. ModelEmit wraps the state and realmModel
 */

public class RealmObservable {

    private static BehaviorSubject<ModelEmit> realmModelObserver = BehaviorSubject.create();
    private static CompositeSubscription compositeSubscription = new CompositeSubscription();
    private static HashMap<Object, CompositeSubscription> mapSubscription = new HashMap<>();

    public static Observable<ModelEmit> asObservable() {
        return realmModelObserver.asObservable();
    }

    public static void onNext(ModelEmit realmModelState){
        realmModelObserver.onNext( realmModelState);
    }

    public static void add(Subscription subscription ){
        compositeSubscription.add( subscription );
    }

    public static void add( Object object, Subscription subscription ){

       if( !mapSubscription.containsKey( object ) ){
           mapSubscription.put( object, new CompositeSubscription() );
       }
       mapSubscription.get(object).add(subscription);
    }

    public static void remove( Subscription subscription ){
        compositeSubscription.remove( subscription );
    }

    public static void remove( Object object, Subscription subscription ){
        mapSubscription.get(object).remove(subscription);
    }

    public static void unsubscribe(){

        for (HashMap.Entry<Object, CompositeSubscription> entry : mapSubscription.entrySet()) {
            mapSubscription.get( entry.getKey() ).unsubscribe();
        }
        mapSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    public static void unsubcribe( Object object ){
        if( mapSubscription.containsKey( object )  ){
            mapSubscription.get( object ).unsubscribe();
            mapSubscription.remove( object );
        }
    }
}
