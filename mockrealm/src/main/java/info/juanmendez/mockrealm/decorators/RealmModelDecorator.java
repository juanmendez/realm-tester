package info.juanmendez.mockrealm.decorators;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Set;

import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.ModelEmit;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import rx.Subscription;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelDecorator {

    public static void prepare() throws Exception {

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                RealmModel realmModel = (RealmModel) invocation.getArguments()[0];
                RealmStorage.removeModel( realmModel );
                return null;
            }
        }).when( RealmObject.class, "deleteFromRealm", any( RealmModel.class ) );
    }

    public static RealmModel mockRealmObject(RealmModel realmModel ){

        if( realmModel instanceof RealmObject ){
            realmModel = spy( realmModel );
        }

        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
        Class fieldClass;
        Subscription subscription;

        /**
         * There is one observable per each member observed either it's a realmModel or a realmResult
         */
        for (Field field: fieldSet) {

            fieldClass = field.getType();

            if( RealmModel.class.isAssignableFrom(fieldClass) ){

                RealmModel finalRealmModel = realmModel;
                RealmObservable.add( realmModel,

                        RealmObservable.asObservable()
                        .filter(modelEmit -> modelEmit.getState()==ModelEmit.REMOVED )
                        .map(modelEmit -> modelEmit.getRealmModel())
                        .ofType(fieldClass)
                        .subscribe( o -> {
                            Object variable = Whitebox.getInternalState(finalRealmModel,
                                    field.getName());

                            if( variable != null && variable == o ){
                                Whitebox.setInternalState(finalRealmModel,
                                        field.getName(),
                                        (Object[]) null);
                            }
                        })
                    );
            }
            else if( fieldClass == RealmList.class ){

                //RealmResults are not filtered
                RealmModel finalRealmModel1 = realmModel;

                RealmObservable.add( realmModel, RealmObservable.asObservable()
                        .filter(modelEmit -> modelEmit.getState() == ModelEmit.REMOVED)
                        .map(modelEmit -> modelEmit.getRealmModel())
                        .subscribe(o -> {
                            RealmList<RealmModel> realmList = (RealmList) Whitebox.getInternalState(finalRealmModel1, field.getName());

                            if( realmList != null ){

                                while( realmList.contains( o ) ){
                                    realmList.remove( o );
                                }
                            }
                        })
                );
            }
        }

        if( realmModel instanceof RealmObject ){

            RealmModel modelDeleted = realmModel;

            //when deleting then also make all subscriptions be unsubscribed
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    RealmObservable.unsubcribe( modelDeleted );

                    Set<Field> fieldSet =  Whitebox.getAllInstanceFields(modelDeleted);

                    for (Field field: fieldSet) {

                        if( field.getType() == RealmList.class ){

                            RealmList list = (RealmList) Whitebox.getInternalState(modelDeleted, field.getName());

                            if( list != null )
                                list.clear();
                        }
                    }

                    RealmStorage.removeModel(modelDeleted);
                    return null;
                }
            }).when( (RealmObject) realmModel ).deleteFromRealm();
        }

        return realmModel;
    }
}