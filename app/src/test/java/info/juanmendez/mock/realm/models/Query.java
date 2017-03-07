package info.juanmendez.mock.realm.models;

/**
 * Created by Juan Mendez on 3/6/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class Query {

    private String field;
    private String comparisson;
    private Object[] args;

    public Query(String comparisson) {
        this.comparisson = comparisson;
    }

    public Query(String comparisson, String field, Object[] args) {
        this.comparisson = comparisson;
        this.args = args;
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public String getComparisson() {
        return comparisson;
    }

    public Object[] getArgs() {
        return args;
    }
}
