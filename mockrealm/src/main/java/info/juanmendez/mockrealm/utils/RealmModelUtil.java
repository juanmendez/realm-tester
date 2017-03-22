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

    private static final String Q = "\"";
    private static final String C = ",";
    private static final String Op = "{";
    private static final String Cp = "}";
    private static final String Ob = "[";
    private static final String Cb = "]";

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

        if( realmModel instanceof AbstractList ){

            if( !((AbstractList<RealmModel>)realmModel).isEmpty()  ){
                return Ob+Cb;
            }

            jsonString += Ob;
            for( RealmModel m: (AbstractList<RealmModel>)realmModel ){
                jsonString += toString(m)+C;
            }

            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString+= Cb;
        }
        else{
            Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
            jsonString += Op;
            Object currentObject;

            for (Field field: fieldSet) {

                currentObject = Whitebox.getInternalState(realmModel, field.getName() );

                if(AbstractList.class.isAssignableFrom(field.getType())){
                    jsonString+= Q + field.getName() + Q + ":" + toString(currentObject) + C;
                }
                else
                if(RealmModel.class.isAssignableFrom(field.getType())){
                    jsonString+= Q + field.getName() + Q + ":" + toString( (RealmModel) currentObject ) + C;
                }
                else
                if (currentObject != null)
                {
                    jsonString+= Q + field.getName() + Q + ":" + Q +  currentObject.toString() + Q + C;
                }
            }

            jsonString = jsonString.substring(0,jsonString.length()-1);
            jsonString += Cp;
        }


        return jsonString;
    }
}
