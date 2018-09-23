package info.juanmendez.realmtester.decorators;

import info.juanmendez.realmtester.demo.models.RealmListStubbed;
import io.realm.RealmList;
import io.realm.RealmModel;

import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

public class RealmListDecorator<T extends RealmModel> {

    public static void prepare() throws Exception {

        spy(RealmList.class);

        whenNew(RealmList.class).withArguments(anyVararg()).thenAnswer(invocation -> {

            RealmList<RealmModel> realmList = create();
            Object[] args = invocation.getArguments();

            for (Object arg : args) {
                realmList.add((RealmModel) arg);
            }

            return realmList;
        });
    }

    public static RealmList<RealmModel> create() {
        return new RealmListStubbed<>();
    }
}