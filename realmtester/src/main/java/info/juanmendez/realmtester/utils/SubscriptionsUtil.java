package info.juanmendez.realmtester.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * A subject has several observers (subjectToObservers)
 * An observer is associated with one subject (observerToSubject)
 * The subject has a hold of a CompositeSubscription (subjectComposite)
 * Each observer added to the subjectComposite has a subscription map associated. (observerSubscriptions)
 */
public class SubscriptionsUtil<S, O> {

    private HashMap<S, ArrayList<O>> subjectToObservers = new HashMap<S, ArrayList<O>>();
    private HashMap<O, S> observerToSubject = new HashMap<O, S>();
    private HashMap<S, CompositeSubscription> subjectComposite = new HashMap<>();
    private HashMap<O, Subscription> observerSubscriptions = new HashMap<>();

    private CompositeSubscription getSubjectComposite(S subject) {

        if (!subjectComposite.containsKey(subject))
            subjectComposite.put(subject, new CompositeSubscription());

        return subjectComposite.get(subject);
    }

    //get all observers associated with a subject
    private ArrayList<O> getSubjectObservers(S subject) {

        if (!subjectToObservers.containsKey(subject)) {
            subjectToObservers.put(subject, new ArrayList<>());
        }

        return subjectToObservers.get(subject);
    }

    /**
     * add observers' subscription to subjectComposite, and also observerComposite
     * also associate the observer with its subscription (observerSubscriptions)
     *
     * @param subject
     * @param observer
     * @param subscription
     */
    public void add(S subject, O observer, Subscription subscription) {

        add(subject, subscription);
        observerSubscriptions.put(observer, subscription);
        getSubjectObservers(subject).add(observer);
        observerToSubject.put(observer, subject);
    }

    public void add(S subject, Subscription subscription) {

        CompositeSubscription compositeSubscription = getSubjectComposite(subject);
        compositeSubscription.add(subscription);
    }

    /**
     * when you remove the observer from observerComposite, also remove association at observersSubscription
     *
     * @param observer
     */
    public void remove(O observer) {

        if (observerToSubject.containsKey(observer) && observerSubscriptions.containsKey(observer)) {

            S subject = observerToSubject.get(observer);
            CompositeSubscription compositeSubscription = getSubjectComposite(subject);
            Subscription subscription = observerSubscriptions.get(observer);
            compositeSubscription.remove(subscription);

            observerSubscriptions.remove(observer);
            getSubjectObservers(subject).remove(observer);
            observerToSubject.remove(observer);
        }
    }


    public void remove(O observer, Subscription subscription) {

        S subject = observerToSubject.get(observer);
        CompositeSubscription compositeSubscription = getSubjectComposite(subject);
        compositeSubscription.remove(subscription);
    }

    public void removeAll(S subject) {
        CompositeSubscription compositeSubscription = getSubjectComposite(subject);
        ArrayList<O> observers = getSubjectObservers(subject);

        compositeSubscription.clear();
        subjectComposite.remove(subject);

        for (O observer : observers) {
            observerSubscriptions.remove(observer);
            observerToSubject.remove(observer);
        }

        observers.clear();
        subjectToObservers.remove(subject);
    }

    public void removeAll() {

        subjectToObservers.size();
        for (Iterator<Map.Entry<S, ArrayList<O>>> it = subjectToObservers.entrySet().iterator(); it.hasNext(); ) {
            removeAll(it.next().getKey());
        }
        subjectToObservers.size();
    }
}