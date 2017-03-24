package info.juanmendez.mockrealm.decorators;

import info.juanmendez.mockrealm.models.RealmListStubbed;
import io.realm.RealmList;
import io.realm.RealmModel;

import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;


/**
 * Created by Juan Mendez on 2/25/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
public class RealmListDecorator {

    public static void prepare() throws Exception {

        spy(RealmList.class);

        whenNew( RealmList.class ).withArguments(anyVararg()).thenAnswer(invocation -> {

            RealmList<RealmModel> realmList = create();
            Object[] args = invocation.getArguments();

            for (Object arg:args) {
                realmList.add((RealmModel)arg);
            }

            return realmList;
        });
    }

    public static RealmList<RealmModel> create(){

        return new RealmListStubbed();
    }

}