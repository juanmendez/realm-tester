package info.juanmendez.mockrealm.utils;

import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import info.juanmendez.mockrealm.decorators.RealmListDecorator;
import info.juanmendez.mockrealm.models.Query;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Created by Juan Mendez on 3/27/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 * <p>
 * This class takes care of making a realmList have distinct values
 */

public class QueryDistinct {

    ArrayList<String> types;

    public RealmList<RealmModel> perform(Query query, RealmList<RealmModel> realmList) {
        return perform((String) query.getArgs()[0], realmList);
    }

    /**
     * takes only one filed to sort!
     *
     * @param field     (must have field to sort, and either desc/asc order)
     * @param realmList list to sort
     */
    public RealmList<RealmModel> perform(String field, RealmList<RealmModel> realmList) {
        this.types = new ArrayList<>(Arrays.asList(((String) field).split("\\.")));

        RealmList<RealmModel> distinctList = RealmListDecorator.create();

        if (realmList != null && !realmList.isEmpty()) {

            Object value;
            ArrayList distinctValues = new ArrayList<>();

            for (RealmModel realmModel : realmList) {
                value = searchInModel(realmModel, 0);

                if (!distinctValues.contains(value)) {
                    distinctValues.add(value);
                    distinctList.add(realmModel);
                }
            }
        }

        return distinctList;
    }

    /**
     * find the current value based on the array types. if it's the final element from such array
     * then it returns that value, otherwises it checks if the current value is a realmModel or realmList,
     * and then does another iteration.
     *
     * @param model
     * @param level
     * @return
     */
    private Object searchInModel(Object model, int level) {

        Object o;

        try {
            o = Whitebox.getInternalState(model, types.get(level));
        } catch (Exception e) {
            throw (new RealmException(RealmModelUtil.getClass(model).getName() + " doesn't have the attribute " + types.get(level)));
        }

        if (o != null) {

            if (level < types.size() - 1) {

                if (o instanceof RealmList) {
                    throw new RealmException("#mocking-realm: 'RealmList' field '" + types.get(level) + "' is not a supported link field here.");
                } else if (o instanceof RealmModel) {
                    throw new RealmException("#mocking-realm: 'RealmObject' field '" + types.get(level) + "' is not a supported link field here.");
                } else {
                    return searchInModel(o, level + 1);
                }
            }
        }

        if (o instanceof Date) {
            o = ((Date) o).getTime();
        }

        return o;
    }
}