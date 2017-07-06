package info.juanmendez.mockrealm.decorators;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.RealmEvent;
import info.juanmendez.mockrealm.utils.RealmModelUtil;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelDecorator {

    public static void prepare(){
    }

    private static RealmModel createFromClass( Class clazz ){

        Constructor constructor = null;
        RealmModel realmModel = null;

        try {
            constructor = clazz.getConstructor();
            realmModel = (RealmModel) constructor.newInstance();
        }  catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return  realmModel;
    }

    public static RealmModel create(Class clazz, Boolean valid ) {
        RealmModel realmModel = createFromClass( clazz );

        if( realmModel instanceof RealmObject){
            realmModel = RealmModelDecorator.decorate( realmModel );
        }

        setValid( realmModel, valid );
        setLoaded( realmModel, valid );
        return realmModel;
    }

    public static RealmModel decorate(RealmModel realmModel ){

        Class clazz = RealmModelUtil.getClass( realmModel );

        //only decorate new realmModels
        if(RealmStorage.getRealmMap().get(clazz).contains( realmModel)){
            return realmModel;
        }

        if( realmModel instanceof RealmObject ){
            realmModel = spy( realmModel );
        }

        startDeleteObservers( realmModel);

        if( realmModel instanceof RealmObject ){
            RealmObjectDecorator.handleDeleteActions( (RealmObject) realmModel);
            RealmObjectDecorator.handleAsyncMethods( (RealmObject) realmModel);
        }

        return realmModel;
    }

    /**
     * Through RealmObservable be notified of changes, and see if any other realmModel deleted
     * is referenced by this realmModel and remove such reference.
     * @param realmModel
     */
    private static void startDeleteObservers(RealmModel realmModel ){

        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
        Class fieldClass;

        /**
         * There is one observable per each member observed either it's a realmModel or a realmResult
         */
        for (Field field: fieldSet) {

            fieldClass = field.getType();

            if( RealmModel.class.isAssignableFrom(fieldClass) ){

                RealmModel finalRealmModel = realmModel;
                RealmObservable.add( realmModel,

                        RealmObservable.asObservable()
                        .filter(realmEvent -> realmEvent.getState()== RealmEvent.MODEL_REMOVED)
                        .map(realmEvent -> realmEvent.getRealmModel())
                        .ofType(fieldClass)
                        .subscribe( o -> {
                            Object variable = Whitebox.getInternalState(finalRealmModel,
                                    field.getName());

                            if( variable != null && variable == o ){
                                Whitebox.setInternalState(finalRealmModel,
                                        field.getName(), (Object[]) null);
                            }
                        })
                );
            }
            else if( fieldClass == RealmList.class ){

                //RealmResults are not filtered

                RealmObservable.add( realmModel,

                        RealmObservable.asObservable()
                        .filter(realmEvent -> realmEvent.getState() == RealmEvent.MODEL_REMOVED)
                        .map(realmEvent -> realmEvent.getRealmModel())
                        .subscribe(o -> {
                            RealmList<RealmModel> realmList = (RealmList) Whitebox.getInternalState(realmModel, field.getName());

                            if( realmList != null ){
                                while( realmList.contains( o ) ){
                                    realmList.remove( o );
                                }
                            }
                        })
                );
            }
        }
    }

    /**
     * mark either realmModel or realmObject as valid or not
     * @param realmModel
     * @param flag
     */
    public static void setValid(RealmModel realmModel, Boolean flag ){

        if( realmModel instanceof RealmObject ){
            doReturn( flag ).when( (RealmObject) realmModel ).isValid();
        }
        else {

            try {
                doReturn( flag ).when( RealmObject.class, "isValid", realmModel );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * mark either realmModel or realmObject as loaded or not
     * @param realmModel
     * @param flag
     */
    public static void setLoaded(RealmModel realmModel, Boolean flag ){

        if( realmModel instanceof RealmObject ){
            doReturn( flag ).when( ((RealmObject) realmModel) ).isLoaded();
        }
        else {

            try {
                doReturn( flag ).when( RealmObject.class, "isLoaded", realmModel );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteRealmModel( RealmModel realmModel ){
        if( realmModel instanceof RealmObject){
            ((RealmObject) realmModel ).deleteFromRealm();
        }
        else{
            RealmObject.deleteFromRealm( realmModel );
        }
    }
}