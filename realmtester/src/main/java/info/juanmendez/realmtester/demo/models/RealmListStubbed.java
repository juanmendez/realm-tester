package info.juanmendez.realmtester.demo.models;

import org.mockito.internal.util.reflection.Whitebox;

import info.juanmendez.realmtester.decorators.RealmModelDecorator;
import io.realm.RealmList;
import io.realm.RealmModel;

public class RealmListStubbed<T extends RealmModel> extends RealmList<T> {

    @Override
    public boolean deleteFirstFromRealm() {
        if (!isEmpty()) {
            RealmModel realmModel = get(0);
            RealmModelDecorator.deleteRealmModel(realmModel);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteLastFromRealm() {

        if (!isEmpty()) {
            RealmModel realmModel = get(size() - 1);
            RealmModelDecorator.deleteRealmModel(realmModel);
            return true;
        }

        return false;
    }

    @Override
    public void deleteFromRealm(int location) {
        if (!isEmpty()) {
            RealmModel realmModel = get(size() - 1);

            RealmModelDecorator.deleteRealmModel(realmModel);
        }
    }

    @Override
    public Number min(String fieldName) {

        Number value, minValue = null;

        for (Object item : this) {

            value = (Number) Whitebox.getInternalState(item, fieldName);

            if (minValue == null)
                minValue = value;
            else if (value.floatValue() < minValue.floatValue()) {
                minValue = value;
            }
        }

        return minValue;
    }

    /**
     * if realmModel is empty or null, then returns null
     *
     * @param fieldName
     * @return
     */
    @Override
    public Number max(String fieldName) {

        Number value, maxValue = null;

        for (Object item : this) {

            value = (Number) Whitebox.getInternalState(item, fieldName);

            if (maxValue == null)
                maxValue = value;
            else if (value.floatValue() > maxValue.floatValue()) {
                maxValue = value;
            }
        }

        return maxValue;
    }

    /**
     * if realmModel is empty or null, then returns null
     *
     * @param fieldName
     * @return
     */
    @Override
    public Number sum(String fieldName) {

        double sumValue = 0;

        for (Object item : this) {

            sumValue += ((Number) Whitebox.getInternalState(item, fieldName)).floatValue();
        }

        return sumValue;
    }

    /**
     * if realmModel is empty or null, then returns null
     *
     * @param fieldName
     * @return
     */
    @Override
    public double average(String fieldName) {

        float sumValue = 0;

        for (Object item : this) {

            sumValue += ((Number) Whitebox.getInternalState(item, fieldName)).floatValue();
        }

        return (sumValue / size());
    }

    @Override
    public boolean deleteAllFromRealm() {
        for (Object realmModel : this) {
            RealmModelDecorator.deleteRealmModel((RealmModel) realmModel);
        }

        return true;
    }
}