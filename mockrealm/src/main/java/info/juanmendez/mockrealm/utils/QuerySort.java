package info.juanmendez.mockrealm.utils;

import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import info.juanmendez.mockrealm.decorators.RealmListDecorator;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Created by Juan Mendez on 3/27/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * This class takes care of sorting realmLists. The realmList can be in any level.
 * For example persons.dogs.age, srots each dogs realmList, only.
 * In case it's just persons.favoriteDog, then persons realmList is the one sorted
 */

public class QuerySort {

    ArrayList<String> types;
    private int desc = 1;



    public RealmList<RealmModel> perform( Object[] args, RealmList<RealmModel> realmList ){
        return perform( (SortField)args[0], realmList );
    }

    /**
     * takes only one filed to sort!
     * @param sortField (must have field to sort, and either desc/asc order)
     * @param realmList list to sort
     */
    public RealmList<RealmModel> perform( SortField sortField, RealmList<RealmModel> realmList ){
        this.types = new ArrayList<>(Arrays.asList(((String) sortField.field).split("\\.")));
        this.desc = sortField.desc?1:-1;

        RealmList<RealmModel> listToSort = RealmListDecorator.create();
        listToSort.addAll(realmList);

        searchInList( listToSort, 0 );
        return listToSort;
    }

    private Object searchInList(RealmList<RealmModel> realmList, int level ){

        if( realmList != null && !realmList.isEmpty()){
            ModelKey modelKey;
            ArrayList<ModelKey> modelKeys = new ArrayList<>();


            for (RealmModel realmModel : realmList) {
                modelKeys.add( new ModelKey( searchInModel(realmModel, level), realmModel ) );
            }

            Collections.sort(modelKeys, new GenericComparator() );

            Iterator itr= modelKeys.iterator();
            realmList.clear();

            while(itr.hasNext()){
                modelKey = (ModelKey) itr.next();
                realmList.add( modelKey.realmModel );
            }
        }

        return null;
    }

    /**
     * find the current value based on the array types. if it's the final element from such array
     * then it returns that value, otherwises it checks if the current value is a realmModel or realmList,
     * and then does another iteration.
     * @param realmModel
     * @param level
     * @return
     */
    private Object searchInModel(RealmModel realmModel, int level) {

        Object o;

        try {
            o = Whitebox.getInternalState(realmModel, types.get(level));
        } catch (Exception e) {
            throw (new RealmException(RealmModelUtil.getClass(realmModel).getName() + " doesn't have the attribute " + types.get(level)));
        }

        if (o != null) {

            if (level < types.size() - 1) {

                if (o instanceof RealmList) {
                    throw new RealmException("#mocking-realm: 'RealmList' field '" + types.get(level) + "' is not a supported link field here." );
                    //could have sorted another level: return searchInList( (RealmList<RealmModel>) o, level + 1 );
                } else if (o instanceof RealmModel) {
                    return searchInModel((RealmModel) o, level + 1);
                }

                throw (new RealmException(types.get(level) + " is of neither type RealmList, or RealmModel"));
            }
        }

        return o;
    }

    /**
     * we sort by having an instance of each realmModel and its value wrapped in a ValueKey
     */
    class ModelKey {
        Object key;
        RealmModel realmModel;

        public ModelKey(Object key, RealmModel realmModel) {
            this.key = key;
            this.realmModel = realmModel;
        }
    }

    /**
     * This is default Comparator. It welcomes ModelKeys, and based on their
     * keys, it is able to sort.
     */
    class GenericComparator implements Comparator<ModelKey>{

        /**
         * ValueKey's value is an object, so within compare we do comparisson
         * based on their original class.
         * @param q1
         * @param q2
         * @return
         */
        @Override
        public int compare(ModelKey q1, ModelKey q2) {
            Object k1 = q1.key;
            Object k2 = q2.key;
            int returnValue = 0;

            if( k1!=null && k2!=null){
                Class clazz = k1.getClass();

                if (clazz == Date.class) {
                    returnValue = (((Date) k1)).compareTo((Date) k2);
                } else if( clazz == String.class ){
                    returnValue = (((String) k1)).compareTo((String) k2);
                } else if (clazz == Integer.class ) {
                    int b1 = (int)k1;
                    int b2 = (int)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }
                } else if (clazz == Double.class ) {
                    double b1 = (double)k1;
                    double b2 = (double)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }
                } else if (clazz == Long.class ) {
                    long b1 = (long)k1;
                    long b2 = (long)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }

                } else if (clazz == Float.class ) {
                    float b1 = (float)k1;
                    float b2 = (float)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }
                } else if (clazz == Short.class ) {
                    short b1 = (short)k1;
                    short b2 = (short)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }
                }else if (clazz == Byte.class ) {
                    byte b1 = (byte)k1;
                    byte b2 = (byte)k2;

                    if( b1 > b2 ){
                        returnValue = 1;
                    }else if( b1 == b2 ){
                        returnValue = 0;
                    }else {
                        returnValue = -1;
                    }
                }
            }else{

                if( k1 == null && k2 == null )
                    returnValue = 0;
                else
                if( k1 == null  )
                    returnValue = -1;
                else
                if( k2 == null )
                    returnValue = 1;
            }

            return returnValue * desc;
        }
    }

    public static class SortField{
        private String field;
        private Boolean desc;

        public SortField(String field, Boolean desc) {
            this.field = field;
            this.desc = desc;
        }

        public String getField() {
            return field;
        }
    }
}