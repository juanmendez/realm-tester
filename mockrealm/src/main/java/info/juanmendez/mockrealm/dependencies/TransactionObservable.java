package info.juanmendez.mockrealm.dependencies;

import java.util.ArrayList;

import info.juanmendez.mockrealm.models.TransactionEvent;
import info.juanmendez.mockrealm.utils.SubscriptionsUtil;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class TransactionObservable {
    private static TransactionObservable instance;
    private static SubscriptionsUtil<TransactionObservable, Object> subscriptionsUtil = new SubscriptionsUtil();
    private static PublishSubject<TransactionEvent> subject = PublishSubject.create();

    private static ArrayList<Object> stackTransactions = new ArrayList<>();

    public static void startRequest(Object keyTransaction, Subscription subscription) {

        if (instance == null) {
            instance = new TransactionObservable();
        }

        subscriptionsUtil.add(instance, keyTransaction, subscription);

        if (stackTransactions.isEmpty()) {
            stackTransactions.add(keyTransaction);
            next();
        } else {
            stackTransactions.add(keyTransaction);
        }
    }

    /**
     * this is a transaction which happens in the main thread,
     * it is taken as a priority, and moved as first transaction
     *
     * @param keyTransaction
     */
    public static void startRequest(Object keyTransaction) {
        stackTransactions.add(0, keyTransaction);
        next();
    }


    /**
     * closes stackTransactions subscription, but only fires event and requests to start next transaction
     * if initiator is the first element at stackTransactions.
     *
     * @param keyTransaction
     */
    public static void endRequest(Object keyTransaction) {

        int keyIndex = stackTransactions.indexOf(keyTransaction);

        //notify when transaction ends only if it's the first on the list
        if (keyIndex >= 0 && !stackTransactions.isEmpty()) {

            stackTransactions.remove(keyIndex);

            if (keyIndex == 0) {
                subject.onNext(new TransactionEvent(TransactionEvent.END_TRANSACTION, keyTransaction));
            }

            subscriptionsUtil.remove(keyTransaction);

            if (keyIndex == 0) {
                next();
            }
        }
    }

    /**
     * canceling transaction simply means removing it from stackTransactions
     * only if it's not the current transaction which means is the first in the list.
     *
     * @param keyTransaction
     */
    public static void cancel(Object keyTransaction) {
        if (stackTransactions.indexOf(keyTransaction) > 0) {
            stackTransactions.remove(keyTransaction);
            subscriptionsUtil.remove(keyTransaction);
        }
    }

    /**
     * check if transaction has been canceled
     *
     * @param keyTransaction
     * @return true if is not in stackTransactions
     */
    public static Boolean isCanceled(Object keyTransaction) {
        return stackTransactions.indexOf(keyTransaction) < 0;
    }

    private static void next() {

        if (!stackTransactions.isEmpty()) {
            subject.onNext(new TransactionEvent(TransactionEvent.START_TRANSACTION, stackTransactions.get(0)));
        }
    }

    public static Observable<TransactionEvent> asObservable() {
        return subject.asObservable();
    }


    public static void removeSubscriptions() {
        subscriptionsUtil.removeAll();

        //required in case of RealmStorage.clear()
        subject = PublishSubject.create();
    }

    /**
     * An instance of KeyTransaction pairs with each transaction. As a key it then notifies the next transaction, and
     * is used again to request ending transaction, or canceling.
     */
    public static class KeyTransaction {
        String name;

        public static KeyTransaction create(String name) {
            return new KeyTransaction(name);
        }

        public KeyTransaction(String name) {
            this.name = name;
        }
    }
}