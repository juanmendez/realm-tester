package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.dependencies.TransactionObservable;
import info.juanmendez.mockrealm.models.TransactionEvent;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.mockito.*", "android.*"})
@PrepareForTest({ RealmConfiguration.class, Realm.class, RealmQuery.class, RealmResults.class, RealmList.class, RealmObject.class })
public class AsyncMockRealmTest
{
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();
    }

    /**
     */
    @Test(timeout = 6000)
    public void shouldBlockOtherTransactions(){

        TransactionObservable.asObservable().subscribe(transactionEvent -> {
            System.out.println( transactionEvent.getState() + ", " + transactionEvent.getTarget().toString() );
        });

        Transaction transaction;

        new Thread(() -> {
            Transaction t = new Transaction("_t1");
            executeWork( t, 50 );
        }).start();

        transaction = new Transaction("t2");
        TransactionObservable.startRequest(transaction);
        TransactionObservable.endRequest(transaction);

        transaction = new Transaction("t3");
        TransactionObservable.startRequest(transaction);
        TransactionObservable.endRequest(transaction);

        new Thread(() -> {
            Transaction t = new Transaction("_t4");
            executeWork( t, 60 );
        }).start();

        new Thread(() -> {
            Transaction t = new Transaction("_t5");
            executeWork( t, 30 );
        }).start();
    }

    public void executeWork( Object initiator, long mils ){

        TransactionObservable.startRequest(initiator,
                TransactionObservable.asObservable()
                        .filter(transactionEvent -> {
                            return transactionEvent.getState()== TransactionEvent.START_TRANSACTION && transactionEvent.getTarget() == initiator;
                        })
                        .subscribe(o -> {

                            try {
                                Thread.sleep( mils );
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            System.out.println( "completed " + initiator.toString() );
                            TransactionObservable.endRequest(initiator);
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
}