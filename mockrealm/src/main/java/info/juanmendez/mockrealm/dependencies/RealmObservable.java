package info.juanmendez.mockrealm.dependencies;

import info.juanmendez.mockrealm.models.RealmEvent;
import info.juanmendez.mockrealm.utils.SubscriptionsUtil;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

/**
 * Created by Juan Mendez on 3/10/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * Provides a subject which can be observed whenever realm is getting a realmModel added or removed
 * Elements emitted are of type RealmEvent. RealmEvent wraps the state and realmModel
 */

public class RealmObservable {

    private static BehaviorSubject<RealmEvent> realmModelObserver = BehaviorSubject.create();
    private static TransactionObservable to = new TransactionObservable();
    private  static SubscriptionsUtil<TransactionObservable, Object> subscriptionsUtil = new SubscriptionsUtil();


    public static Observable<RealmEvent> asObservable() {
        return realmModelObserver.asObservable();
    }

    public static void onNext(RealmEvent realmModelState){
        realmModelObserver.onNext( realmModelState);
    }

    public static void add(Subscription subscription ){
        subscriptionsUtil.add(to, subscription );
    }

    public static void add( Object observer, Subscription subscription ){
        subscriptionsUtil.add(to, observer, subscription );
    }

    public static void remove( Subscription subscription ){
        subscriptionsUtil.remove(to, subscription );
    }

    public static void remove( Object observer, Subscription subscription ){
        subscriptionsUtil.remove(observer, subscription);
    }

    public static void unsubscribe(){
        subscriptionsUtil.removeAll(to);
    }

    public static void unsubcribe( Object observer ){
        subscriptionsUtil.remove( observer );
    }
}
