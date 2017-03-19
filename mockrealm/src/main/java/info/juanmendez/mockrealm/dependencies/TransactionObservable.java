package info.juanmendez.mockrealm.dependencies;

import java.util.ArrayList;

import info.juanmendez.mockrealm.models.TransactionEvent;
import info.juanmendez.mockrealm.utils.SubscriptionsUtil;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by Juan Mendez on 3/17/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 */
public class TransactionObservable {
    private  SubscriptionsUtil<TransactionObservable, Object> subscriptionsUtil = new SubscriptionsUtil();
    private  PublishSubject<TransactionEvent> subject = PublishSubject.create();
    private  ArrayList<Object> stackTransactions = new ArrayList<>();

    public TransactionObservable(){
    }


    public  void startTransaction( Object keyTransaction, Subscription subscription ){

        subscriptionsUtil.add( this, keyTransaction, subscription );

        if( stackTransactions.isEmpty() ){
            stackTransactions.add( keyTransaction );
            nextTransaction();
        }else{
            stackTransactions.add( keyTransaction );
        }
    }

    /**
     * this is a transaction which happens in the main thread,
     * it is taken as a priority, and moved as first priority.
     * @param keyTransaction
     */
    public void startTransaction( Object keyTransaction ){
        stackTransactions.add(0, keyTransaction );
        nextTransaction();
    }


    /**
     * closes stackTransactions subscription, but only fires event and requests to start next transaction
     * if initiator is the first element at stackTransactions.
     * @param keyTransaction
     */
    public  void endTransaction( Object keyTransaction ){

        int keyIndex = stackTransactions.indexOf(keyTransaction);

        if( keyIndex >= 0 && !stackTransactions.isEmpty() ){

            stackTransactions.remove(keyIndex);

            if( keyIndex == 0 ){
                subject.onNext( new TransactionEvent(TransactionEvent.END_TRANSACTION, keyTransaction ));
            }

            subscriptionsUtil.remove( keyTransaction );

            if( keyIndex == 0 ){
                nextTransaction();
            }
        }
    }

    private  void nextTransaction(){

        if( !stackTransactions.isEmpty() ){
            subject.onNext( new TransactionEvent(TransactionEvent.START_TRANSACTION, stackTransactions.get(0) ));
        }
    }

    public Observable<TransactionEvent> asObservable(){
        return subject.asObservable();
    }
}