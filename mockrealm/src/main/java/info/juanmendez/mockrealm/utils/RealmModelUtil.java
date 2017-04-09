package info.juanmendez.mockrealm.utils;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Set;

import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.RealmListStubbed;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

import static org.mockito.Mockito.mockingDetails;

/**
 * Created by Juan Mendez on 3/17/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelUtil {

    private static final String Q = "\"";
    private static final String C = ",";
    private static final String Op = "{";
    private static final String Cp = "}";
    private static final String Ob = "[";
    private static final String Cb = "]";

    public static Class getClass( Object object ){

        Class clazz = object.getClass();

        if( (object instanceof RealmObject && mockingDetails(object).isSpy()) || object instanceof RealmListStubbed ){
            return clazz.getSuperclass();
        }

        return clazz;
    }

    /**
     *  This is a cheap way to save the state of an object. Unfortunately, @Ignore variables cannot be
     * tracked due to their nature of their retention policy which is not of Runtime policy type.
     * http://stackoverflow.com/questions/4453159/how-to-get-annotations-of-a-member-variable
     * @param realmModel object to check variables and values
     * @return a json string
     */
    public static String getState(Object realmModel){

        if( realmModel == null )
            return "";

        String jsonString = "";

        if( realmModel instanceof AbstractList ){

            AbstractList<RealmModel> abstractList = (AbstractList<RealmModel>)realmModel;

            if( abstractList.isEmpty() ){
                return Ob+Cb;
            }

            jsonString += Ob;
            try{
                for( RealmModel m: abstractList ){
                    jsonString += getState(m)+C;
                }
            }
            catch( Exception e ){
                System.err.println( e.getMessage() );
            }


            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString+= Cb;
        }
        else{
            Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
            jsonString += Op;
            Object currentObject;

            for (Field field: fieldSet) {

                if( !RealmAnnotationUtil.isIgnored(field) ){
                    currentObject = Whitebox.getInternalState(realmModel, field.getName() );

                    if(AbstractList.class.isAssignableFrom(field.getType())){
                        jsonString+= Q + field.getName() + Q + ":" + getState(currentObject) + C;
                    }
                    else
                    if(RealmModel.class.isAssignableFrom(field.getType())){
                        jsonString+= Q + field.getName() + Q + ":" + getState( (RealmModel) currentObject ) + C;
                    }
                    else
                    if (currentObject != null)
                    {
                        jsonString+= Q + field.getName() + Q + ":" + Q +  currentObject.toString() + Q + C;
                    }
                }
            }

            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString += Cp;
        }


        return jsonString;
    }


    /**
     * Make originalRealmModel have the same attribute values from copyRealmModel
     * @param originalRealmModel
     * @param copyRealmModel
     */
    public static RealmModel extend( RealmModel originalRealmModel, RealmModel copyRealmModel ){

        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(copyRealmModel);
        Object currentObject;
        AbstractList copyList, originalList;

        for (Field field: fieldSet) {

            currentObject = Whitebox.getInternalState(copyRealmModel, field.getName() );

            if( currentObject instanceof AbstractList ){
                copyList = (AbstractList) currentObject;
                originalList = (AbstractList) Whitebox.getInternalState(originalRealmModel, field.getName() );
                originalList.clear();
                originalList.addAll( copyList );
            }else{
                Whitebox.setInternalState( originalRealmModel, field.getName(), currentObject );
            }
        }

        return originalRealmModel;
    }

    public static RealmModel tryToUpdate( RealmModel newRealmModel ){

        HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
        Class clazz = RealmModelUtil.getClass(newRealmModel);

        Object newKey, storedKey;
        RealmList<RealmModel> realmList = realmMap.get(clazz);

        if( !realmList.contains( newRealmModel )){

            newKey = RealmAnnotationUtil.findPrimaryKey( newRealmModel );

            if( newKey != null ){

                for( RealmModel realmModel: realmList ){
                    storedKey = RealmAnnotationUtil.findPrimaryKey( realmModel );

                    if( storedKey != null && storedKey.equals( newKey ) ){
                        return RealmModelUtil.extend( realmModel, newRealmModel );
                    }
                }
            }
        }

        return null;
    }
}