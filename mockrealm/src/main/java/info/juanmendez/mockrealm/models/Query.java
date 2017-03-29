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
    private Boolean asTrue = true;

    public static Query build(){
        return new Query();
    }

    public Query(){

    }

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
    
    public Query setField(String field) {
        this.field = field;
        return this;
    }

    public Query setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public Query setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Query setAsTrue(Boolean asTrue) {
        this.asTrue = asTrue;
        return this;
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

    public Boolean getAsTrue() {
        return asTrue;
    }
}