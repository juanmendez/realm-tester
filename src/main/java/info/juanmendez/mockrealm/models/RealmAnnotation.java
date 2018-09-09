package info.juanmendez.mockrealm.models;

import java.util.ArrayList;

/**
 * Created by Juan Mendez on 4/8/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 * <p>
 * Because Realm annotations are of type RetentionPolicy.CLASS, we cannot
 * use reflection, so this is a simple way to start declaring your reamModel classes
 * before you start mocking.
 */
public class RealmAnnotation {
    Class clazz;
    String _primaryField;
    ArrayList<String> _indexedFields = new ArrayList<>();
    ArrayList<String> _ignoredFields = new ArrayList<>();

    public static RealmAnnotation build(Class clazz) {
        RealmAnnotation realmAnnotation = new RealmAnnotation();
        realmAnnotation.clazz = clazz;
        return realmAnnotation;
    }

    public RealmAnnotation primaryField(String field) {
        _primaryField = field;
        return this;
    }

    public RealmAnnotation indexedFields(String... fields) {

        _indexedFields.clear();

        for (String field : fields) {
            _indexedFields.add(field);
        }
        return this;
    }


    public RealmAnnotation ignoredFields(String... fields) {

        _ignoredFields.clear();

        for (String field : fields) {
            _ignoredFields.add(field);
        }
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getPrimaryField() {
        return _primaryField;
    }

    public ArrayList<String> geIndexedFields() {
        return _indexedFields;
    }

    public ArrayList<String> getIgnoredFields() {
        return _ignoredFields;
    }
}
