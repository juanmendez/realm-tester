package info.juanmendez.mockrealm.decorators;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.models.RealmEvent;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

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
            realmModel = RealmModelDecorator.mockRealmObject( realmModel );
        }

        RealmObjectDecorator.markAsValid( realmModel, valid );
        RealmObjectDecorator.markAsLoaded( realmModel, valid );
        return realmModel;
    }

    public static RealmModel mockRealmObject(RealmModel realmModel ){

        if( realmModel instanceof RealmObject ){
            realmModel = spy( realmModel );
        }

        handleDeleteEvents( realmModel);

        if( realmModel instanceof RealmObject ){
            RealmObjectDecorator.handleDeleteActions( (RealmObject) realmModel);
            RealmObjectDecorator.handleAsyncMethods( (RealmObject) realmModel);
        }

        return realmModel;
    }

    private static void handleDeleteEvents( RealmModel realmModel ){

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
}