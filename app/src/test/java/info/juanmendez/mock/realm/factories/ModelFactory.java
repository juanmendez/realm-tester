package info.juanmendez.mock.realm.factories;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Set;

import info.juanmendez.mock.realm.dependencies.RealmStorage;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class ModelFactory {

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

        CompositeSubscription allSubscriptions = new CompositeSubscription();
        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmModel);
        Class fieldClass;
        Subscription subscription;

        /**
         * There is one observable per each member observed either it's a realmModel or a realmResult
         */
        for (Field field: fieldSet) {

            fieldClass = field.getType();

            if( RealmModel.class.isAssignableFrom(fieldClass) ){

                //RealmModels are filtered by its inmediate class
                RealmModel finalRealmModel = realmModel;
                subscription = RealmStorage.getDeleteObservable()
                        .ofType(fieldClass)
                        .subscribe( o -> {

                            Object variable = Whitebox.getInternalState(finalRealmModel, field.getName());

                            if( variable != null && variable == o ){
                                Whitebox.setInternalState(finalRealmModel, field.getName(), (Object[]) null);
                            }
                        });

                allSubscriptions.add( subscription );
            }
            else if( fieldClass == RealmList.class ){

                //RealmResults are not filtered
                RealmModel finalRealmModel1 = realmModel;
                subscription = RealmStorage.getDeleteObservable()
                        .subscribe(o -> {

                            RealmList<RealmModel> realmList = (RealmList) Whitebox.getInternalState(finalRealmModel1, field.getName());

                            if( realmList != null ){

                                while( realmList.contains( o ) ){
                                    realmList.remove( o );
                                }
                            }

                        });//end subscription

                allSubscriptions.add( subscription );
            }
        }

        if( realmModel instanceof RealmObject ){

            RealmModel finalRealmModel2 = realmModel;

            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    allSubscriptions.clear();

                    Set<Field> fieldSet =  Whitebox.getAllInstanceFields(finalRealmModel2);

                    for (Field field: fieldSet) {

                        if( field.getType() == RealmList.class ){

                            RealmList list = (RealmList) Whitebox.getInternalState(finalRealmModel2, field.getName());

                            if( list != null )
                                list.clear();
                        }
                    }

                    RealmStorage.removeModel(finalRealmModel2);
                    return null;
                }
            }).when( (RealmObject) realmModel ).deleteFromRealm();
        }

        return realmModel;
    }
}