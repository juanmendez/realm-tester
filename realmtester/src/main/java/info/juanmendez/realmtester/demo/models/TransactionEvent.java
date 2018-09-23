package info.juanmendez.realmtester.demo.models;

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
