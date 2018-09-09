package info.juanmendez.mockrealm.models;

/**
 * Created by Juan Mendez on 3/17/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 * <p>
 * In order to start and end transactions, we need to keep track of what's been added and removed..
 */

public class TransactionEvent {

    public static final String START_TRANSACTION = "RealmStartTransaction";
    public static final String END_TRANSACTION = "RealmEndTransaction";


    private String state;
    private Object target;

    public TransactionEvent(String state, Object target) {
        this.state = state;
        this.target = target;
    }

    public String getState() {
        return state;
    }

    public Object getTarget() {
        return target;
    }
}
