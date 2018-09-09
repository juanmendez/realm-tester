package info.juanmendez.mockrealm.models;

public class Query {

    private String field;
    private String condition;
    private Object[] args;
    private Boolean asTrue = true;

    public static Query build() {
        return new Query();
    }

    private Query() {
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