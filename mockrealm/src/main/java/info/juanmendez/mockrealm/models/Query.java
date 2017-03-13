package info.juanmendez.mockrealm.models;

/**
 * Created by Juan Mendez on 3/6/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class Query {

    private String field;
    private String condition;
    private Object[] args;

    public Query(String condition) {
        this.condition = condition;
    }

    public Query( String condition, Object[] args ){
        this( condition );
        this.args = args;
    }

    public Query(String condition, String field, Object[] args) {
        this( condition, args );
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public String getCondition() {
        return condition;
    }

    public Object[] getArgs() {
        return args;
    }
}