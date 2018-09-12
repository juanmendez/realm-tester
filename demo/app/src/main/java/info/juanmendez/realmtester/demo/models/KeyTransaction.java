package info.juanmendez.realmtester.demo.models;

public class KeyTransaction {
    String name;

    public KeyTransaction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
