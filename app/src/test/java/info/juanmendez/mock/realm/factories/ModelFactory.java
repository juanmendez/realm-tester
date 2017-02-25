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

import static org.powermock.api.mockito.PowerMockito.doAnswer;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class ModelFactory {


    public static RealmObject mockRealmObject(RealmObject realmModel ){
        RealmObject spied = PowerMockito.spy( realmModel );


        CompositeSubscription allSubscriptions = new CompositeSubscription();
        Set<Field> fieldSet =  Whitebox.getAllInstanceFields(spied);

        for (Field field: fieldSet) {

            if( RealmModel.class.isAssignableFrom( field.getType() )){
                Subscription subscription = RealmStorage.getDeleteObservable()
                        .ofType(field.getType())
                        .subscribe(o -> {

                            if( Whitebox.getInternalState( spied, field.getName() ) == o ){
                                Whitebox.setInternalState( spied, field.getName(), (Object[]) null);
                            }

                        });

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

                        RealmList list = (RealmList) Whitebox.getInternalState( spied, field.getName() );

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
