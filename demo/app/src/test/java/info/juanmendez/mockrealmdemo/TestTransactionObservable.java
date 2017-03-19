package info.juanmendez.mockrealmdemo;

import info.juanmendez.mockrealm.dependencies.TransactionObservable;
import info.juanmendez.mockrealm.models.TransactionEvent;

/**
 * Created by Juan Mendez on 3/18/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * This is a test made directly on mockrealm, but it was moved into this module
 */

public class TestTransactionObservable {

    TransactionObservable to = new TransactionObservable();

    TestTransactionObservable(){
        to.asObservable().subscribe(transactionEvent -> {
            System.out.println( transactionEvent.getState() + ", " + transactionEvent.getInitiator().toString() );
        });

        Transaction transaction;

        new Thread(() -> {
            Transaction t = new Transaction("_t1");
            executeWork( t, 500 );
        }).start();

        transaction = new Transaction("t2");
        to.startTransaction(transaction);
        to.endTransaction(transaction);

        transaction = new Transaction("t3");
        to.startTransaction(transaction);
        to.endTransaction(transaction);

        new Thread(() -> {
            Transaction t = new Transaction("_t4");
            executeWork( t, 600 );
        }).start();

        new Thread(() -> {
            Transaction t = new Transaction("_t5");
            executeWork( t, 3000 );
        }).start();
    }

    public void executeWork( Object initiator, long mils ){

        to.startTransaction(initiator,
                to.asObservable()
                        .filter(transactionEvent -> {
                            return transactionEvent.getState()== TransactionEvent.START_TRANSACTION && transactionEvent.getInitiator() == initiator;
                        })
                        .subscribe(o -> {

                            try {
                                Thread.sleep( mils );
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            System.out.println( "completed " + initiator.toString() );
                            to.endTransaction(initiator);
                        })
        );
    }

    class Transaction{
        String name;

        Transaction( String name ){
            this.name = name;
        }

        @Override
        public String toString(){
            return this.name;
        }
    }

    public static void main( String[] args ){
        new TestTransactionObservable();
    }
}
