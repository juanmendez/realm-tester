package info.juanmendez.mockrealm.utils;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.Set;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

/**
 * Created by Juan Mendez on 3/17/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelUtil {

    public static Class getClass( Object object ){

        Class clazz = object.getClass();

        if( object instanceof RealmObject || object instanceof RealmList ){
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
    public static String toString(Object realmModel){

        if( realmModel == null )
            return "";

        String jsonString = "";
        String q = "\"";
        String c = ",";
        String oP = "{";
        String cP = "}";
        String oB = "[";
        String cB = "]";


        if( realmModel instanceof AbstractList ){
            jsonString += oB;
            for( RealmModel m: (AbstractList<RealmModel>)realmModel ){
                jsonString += toString(m)+c;
            }

            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString+= cB;
        }
        else{
            Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
            jsonString += oP;
            Object currentObject;

            for (Field field: fieldSet) {

                currentObject = Whitebox.getInternalState(realmModel, field.getName() );

                if(AbstractList.class.isAssignableFrom(field.getType())){
                    jsonString+= q + field.getName() + q + ":" + toString(currentObject) + c;
                }
                else
                if(RealmModel.class.isAssignableFrom(field.getType())){
                    jsonString+= q + field.getName() + q + ":" + toString( (RealmModel) currentObject ) + c;
                }
                else
                if (currentObject != null)
                {
                    jsonString+= q + field.getName() + q + ":" + q +  currentObject.toString() + q + c;
                }
            }

            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString += cP;
        }


        return jsonString;
    }
}
