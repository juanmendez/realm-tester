package info.juanmendez.mockrealm.decorators;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.Callable;

import info.juanmendez.mockrealm.dependencies.RealmObservable;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.dependencies.TransactionObservable;
import info.juanmendez.mockrealm.models.RealmEvent;
import info.juanmendez.mockrealm.models.TransactionEvent;
import info.juanmendez.mockrealm.utils.QueryHolder;
import info.juanmendez.mockrealm.utils.RealmModelUtil;
import info.juanmendez.mockrealm.utils.SubscriptionsUtil;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by Juan Mendez on 2/24/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class RealmModelDecorator {

    private static SubscriptionsUtil<RealmObject, RealmChangeListener> subscriptionsUtil = new SubscriptionsUtil<>();

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
            markAsValid( (RealmObject) realmModel, valid );

        }

        return realmModel;
    }

    public static RealmModel mockRealmObject(RealmModel realmModel ){

        if( realmModel instanceof RealmObject ){
            realmModel = spy( realmModel );
        }

        handleDeleteEvents( realmModel);

        if( realmModel instanceof RealmObject ){
            handleDeleteActions( (RealmObject) realmModel);
            handleAsyncMethods( (RealmObject) realmModel);
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

    private static void handleDeleteActions( RealmObject realmObject ){

        //when deleting then also make all subscriptions be unsubscribed
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                RealmObservable.unsubcribe( realmObject );

                Set<Field> fieldSet =  Whitebox.getAllInstanceFields(realmObject);

                for (Field field: fieldSet) {

                    if( field.getType() == RealmList.class ){

                        RealmList list = (RealmList) Whitebox.getInternalState(realmObject, field.getName());

                        if( list != null )
                            list.clear();
                    }
                }

                RealmObservable.unsubcribe( realmObject );
                RealmStorage.removeModel( realmObject );

                //after deleting item, lets make it invalid
                doReturn(false).when( realmObject ).isValid();
                return null;
            }
        }).when( (RealmObject) realmObject ).deleteFromRealm();
    }

    /**
     * avoid doing anything in case realmObject is part of a realmResult
     * @param realmObject
     */
    private static void handleAsyncMethods( RealmObject realmObject ){

        doAnswer(invocation -> null).when( realmObject ).addChangeListener( any(RealmChangeListener.class));
        doAnswer(invocation -> null).when( realmObject).removeChangeListener(any(RealmChangeListener.class));
        doAnswer(invocation -> null).when( realmObject).removeChangeListeners();
        doAnswer( invocation -> null).when( realmObject ).asObservable();
    }

    public static void handleAsyncMethods(RealmObject realmObject, QueryHolder queryHolder ){

        doAnswer(invocation -> {

            //execute query once associated
            RealmChangeListener listener = (RealmChangeListener) invocation.getArguments()[0];
            Observable.fromCallable(new Callable<RealmResults<RealmModel>>() {
                @Override
                public RealmResults<RealmModel> call() throws Exception {

                    return queryHolder.rewind();
                }
            }).subscribeOn(RealmDecorator.getTransactionScheduler())
                    .observeOn( RealmDecorator.getResponseScheduler() )
                    .subscribe(realmResults -> {

                        if( !realmResults.isEmpty())
                            listener.onChange( realmResults.get(0) );
                        else
                            listener.onChange( null );

            });


            final String[] json = new String[2];

            //whenever there is a transaction ending, we compare previous result with current one.
            //we transform both results as json objects and just do a check if strings are not the same
            subscriptionsUtil.add( realmObject,
                                   listener,
                    TransactionObservable.asObservable()
                            .subscribe( transactionEvent -> {

                                if( transactionEvent.getState() == TransactionEvent.END_TRANSACTION ){

                                    String initialJson = "", currrentJson = "";

                                    RealmResults<RealmModel> realmResults = queryHolder.getRealmResults();

                                    if( !realmResults.isEmpty() ){
                                        initialJson = RealmModelUtil.toString( realmResults.get(0) );
                                    }

                                    realmResults = queryHolder.rewind();

                                    if( !realmResults.isEmpty() ){
                                        currrentJson = RealmModelUtil.toString( realmResults.get(0) );
                                    }

                                    if( !initialJson.equals( currrentJson )){

                                        if( !realmResults.isEmpty() )
                                            listener.onChange( realmResults.get(0) );
                                        else
                                            listener.onChange( null );
                                    }
                                }
                            })
            );

            return null;
        }).when( realmObject ).addChangeListener(any(RealmChangeListener.class));


        doAnswer(invocation -> {
            RealmChangeListener listener = (RealmChangeListener) invocation.getArguments()[0];
            subscriptionsUtil.remove(listener);
            return null;
        }).when( realmObject ).removeChangeListener( any(RealmChangeListener.class));


        doAnswer(invocation -> {
            subscriptionsUtil.removeAll( realmObject );
            return null;
        }).when( realmObject ).removeChangeListeners();


        doAnswer(invocation -> {
            PublishSubject subject = PublishSubject.create();

            //first time make a call!
            Observable.fromCallable(new Callable<RealmModel>() {
                @Override
                public RealmModel call() throws Exception {

                    RealmResults<RealmModel> realmResults = queryHolder.rewind();

                    if( !realmResults.isEmpty())
                        return realmResults.get(0);
                    else
                        return null;
                }
            }).subscribeOn(RealmDecorator.getTransactionScheduler())
                    .observeOn( RealmDecorator.getResponseScheduler() )
                    .subscribe(realmObject1 -> {
                        subject.onNext( realmObject1);
                    });

            TransactionObservable.asObservable().subscribe(transactionEvent -> {

                if( transactionEvent.getState() == TransactionEvent.END_TRANSACTION ){
                    String initialJson = "", currrentJson = "";

                    RealmResults<RealmModel> realmResults = queryHolder.getRealmResults();

                    if( !realmResults.isEmpty() ){
                        initialJson = RealmModelUtil.toString( realmResults.get(0) );
                    }

                    realmResults = queryHolder.rewind();

                    if( !realmResults.isEmpty() ){
                        currrentJson = RealmModelUtil.toString( realmResults.get(0) );
                    }

                    if( !initialJson.equals( currrentJson )){

                        if( !realmResults.isEmpty() )
                            subject.onNext( realmResults.get(0) );
                        else
                            subject.onNext( null );
                    }
                }
            });

            return subject;
        }).when( realmObject ).asObservable();

    }


    public static void markAsValid( RealmObject realmObject, Boolean valid ){
        doReturn( valid ).when( realmObject ).isValid();
    }
}