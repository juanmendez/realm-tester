package info.juanmendez.mockrealm.dependencies;

import info.juanmendez.mockrealm.models.ModelEmit;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Juan Mendez on 3/10/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmObservable {

    private static BehaviorSubject<ModelEmit> realmModelObserver = BehaviorSubject.create();
    private static CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Observable<ModelEmit> asObservable() {
        return realmModelObserver.asObservable();
    }

    public static void onNext(ModelEmit realmModelState){
        realmModelObserver.onNext( realmModelState);
    }

    public static void add(Subscription subscription ){
        compositeSubscription.add( subscription );
    }

    public static void remove( Subscription subscription ){
        compositeSubscription.remove( subscription );
    }

    public static void unsubscribe(){
        compositeSubscription.clear();
    }
}
