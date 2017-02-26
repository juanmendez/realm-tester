package info.juanmendez.mock.realm.factories;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
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

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class ModelFactory {

    //TODO: temporary location for static methods of Realmobject
    public static void create() throws Exception {

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when( RealmObject.class, "deleteFromRealm", any( RealmModel.class ) );
    }

    public static RealmObject mockRealmObject(RealmObject realmModel ){
        RealmObject spied = PowerMockito.spy( realmModel );

        CompositeSubscription allSubscriptions = new CompositeSubscription();
        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(spied);
        Class fieldClass;
        Subscription subscription;

        /**
         * There is one observable per each member observed either it's a realmModel or a realmResult
         */
        for (Field field: fieldSet) {

            fieldClass = field.getType();

            if( RealmModel.class.isAssignableFrom(fieldClass) ){

                //RealmModels are filtered by its inmediate class
                subscription = RealmStorage.getDeleteObservable()
                        .ofType(fieldClass)
                        .subscribe( o -> {

                            Object variable = Whitebox.getInternalState( spied, field.getName());

                            if( variable != null && variable == o ){
                                Whitebox.setInternalState( spied, field.getName(), (Object[]) null);
                            }
                        });

                allSubscriptions.add( subscription );
            }
            else if( fieldClass == RealmList.class ){

                //RealmResults are not filtered
                subscription = RealmStorage.getDeleteObservable()
                        .subscribe(o -> {

                            RealmList<RealmModel> realmList = (RealmList) Whitebox.getInternalState( spied, field.getName());

                            if( realmList != null ){

                                while( realmList.contains( o ) ){
                                    realmList.remove( o );
                                }
                            }

                        });//end subscription

                allSubscriptions.add( subscription );
            }
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                allSubscriptions.clear();

                Set<Field> fieldSet =  Whitebox.getAllInstanceFields(spied);

                for (Field field: fieldSet) {

                    if( field.getType() == RealmList.class ){

                        RealmList list = (RealmList) Whitebox.getInternalState( spied, field.getName());

                        if( list != null )
                            list.clear();
                    }
                }

                RealmStorage.removeModel( spied );
                return null;
            }
        }).when( spied ).deleteFromRealm();

        return spied;
    }
}
