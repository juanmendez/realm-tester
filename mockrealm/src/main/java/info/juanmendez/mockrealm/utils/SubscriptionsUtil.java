package info.juanmendez.mockrealm.utils;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Juan Mendez on 3/15/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * A wrapper has several items (wrapperToItems)
 * An item is associated with one wrapper (itemToWrapper)
 * The wrapper has a hold of a CompositeSubscription (wrapperComposite)
 * Each item added to the wrapperComposite has a subscription map associated. (itemSubscriptions)
 */

public class SubscriptionsUtil<W,I> {

    private HashMap<W, ArrayList<I>> wrapperToItems = new HashMap<W, ArrayList<I>>();
    private HashMap<I,W> itemToWrapper = new HashMap<I, W>();
    private HashMap<W, CompositeSubscription> wrapperComposite = new HashMap<>();
    private HashMap<I, Subscription> itemSubscriptions = new HashMap<>();

    private CompositeSubscription getWrapperComposite(W wrapper ){

        if( !wrapperComposite.containsKey( wrapper ))
            wrapperComposite.put( wrapper, new CompositeSubscription());

        return wrapperComposite.get( wrapper );
    }

    //get all items associated with a wrapper
    private ArrayList<I> getWrapperItems(W wrapper ){

        if( !wrapperToItems.containsKey( wrapper) ){
            wrapperToItems.put( wrapper, new ArrayList<I>());
        }

        return wrapperToItems.get( wrapper );
    }

    /**
     * add items' subscription to wrapperComposite, and also itemComposite
     * also associate the item with its subscription (itemSubscriptions)
     * @param wrapper
     * @param item
     * @param subscription
     */
    public void add(W wrapper, I item, Subscription subscription ){

        CompositeSubscription compositeSubscription = getWrapperComposite( wrapper );

        compositeSubscription.add( subscription );
        itemSubscriptions.put( item, subscription );
        getWrapperItems(wrapper).add( item );
        itemToWrapper.put(item, wrapper);
    }

    /**
     * when you remove the item from itemComposite, also remove association at itemsSubscription
     * @param item
     */
    public void remove( I item ){

        if( itemToWrapper.containsKey( item ) && itemSubscriptions.containsKey( item ) ){

            W wrapper = itemToWrapper.get(item);
            CompositeSubscription compositeSubscription = getWrapperComposite( wrapper );
            Subscription subscription = itemSubscriptions.get( item );
            compositeSubscription.remove( subscription );

            itemSubscriptions.remove(item);
            getWrapperItems(wrapper).remove(item);
            itemToWrapper.remove(item);
        }
    }

    public void removeAll( W wrapper ){
        CompositeSubscription compositeSubscription = getWrapperComposite( wrapper );
        ArrayList<I> items = getWrapperItems(wrapper);

        compositeSubscription.clear();
        wrapperComposite.remove( wrapper );

        for (I item: items) {
            itemSubscriptions.remove(item);
            itemToWrapper.remove(item);
        }

        items.clear();
        wrapperToItems.remove( wrapper );
    }
}