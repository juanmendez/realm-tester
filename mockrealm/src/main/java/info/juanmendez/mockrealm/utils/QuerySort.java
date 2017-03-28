package info.juanmendez.mockrealm.utils;

import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Created by Juan Mendez on 3/27/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 *
 * making sure our realmList is sorted by the given criteria
 */

public class QuerySort {

    ArrayList<String> types;


    public void perform( Object[] arguments, RealmList<RealmModel> realmList ){
        this.types = new ArrayList<>(Arrays.asList(((String) arguments[0]).split("\\.")));
        searchInList( realmList, 0 );
    }

    private Object searchInList(RealmList<RealmModel> realmList, int level ){

        ArrayList<QueryMap> queryMaps = new ArrayList<>();
        for (RealmModel realmModel : realmList) {
            queryMaps.add( new QueryMap( searchInModel(realmModel, level), realmModel ) );
        }

        Collections.sort(queryMaps, new GenericComparator(true) );

        Iterator itr=queryMaps.iterator();
        realmList.clear();

        while(itr.hasNext()){
            QueryMap queryMap = (QueryMap) itr.next();
            realmList.add( queryMap.realmModel );
        }

        return null;
    }

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
                    return searchInList( (RealmList<RealmModel>) o, level + 1 );

                } else if (o instanceof RealmModel) {
                    return searchInModel((RealmModel) o, level + 1);
                }

                throw (new RealmException(types.get(level) + " is of neither type RealmList, or RealmModel"));
            }
        }

        return o;
    }

    class QueryMap{
        Object key;
        RealmModel realmModel;

        public QueryMap(Object key, RealmModel realmModel) {
            this.key = key;
            this.realmModel = realmModel;
        }
    }

    class GenericComparator implements Comparator<QueryMap>{

        private int desc = 1;

        public GenericComparator(Boolean isDesc) {
            if( !isDesc )
                desc = -1;
        }

        @Override
        public int compare(QueryMap q1, QueryMap q2) {
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
                    returnValue = 1;
                else
                if( k2 == null )
                    returnValue = -1;
            }

            return returnValue * desc;
        }
    }
}