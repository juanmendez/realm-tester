package info.juanmendez.mockrealmdemo.models;

/**
 * Created by Juan Mendez on 3/21/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class KeyTransaction {
    String name;

    public KeyTransaction(String name ){
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }
}
