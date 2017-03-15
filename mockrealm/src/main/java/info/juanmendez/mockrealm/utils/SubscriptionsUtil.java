package info.juanmendez.mockrealm.utils;

import java.util.HashMap;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Juan Mendez on 3/15/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class CompositeSubscriptionUtil {
    private HashMap<Object, CompositeSubscription> holderSubscriptions = new HashMap<>();
    private HashMap<Object, Subscription> itemSubscription = new HashMap<>();

    public HashMap<Object, CompositeSubscription> getHolderSubscriptions() {
        return holderSubscriptions;
    }

    public HashMap<Object, Subscription> getItemSubscription() {
        return itemSubscription;
    }
}
