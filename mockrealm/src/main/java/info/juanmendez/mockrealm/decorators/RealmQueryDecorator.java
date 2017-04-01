package info.juanmendez.mockrealm.decorators;

import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;

import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.models.Query;
import info.juanmendez.mockrealm.utils.QuerySort;
import info.juanmendez.mockrealm.utils.QueryTracker;
import io.realm.Case;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmQueryDecorator {

    //collections queried keyed by immediate class

    public static RealmQuery create( QueryTracker queryTracker){

        RealmQuery realmQuery = queryTracker.getRealmQuery();

        when( realmQuery.toString() ).thenReturn( "Realm:" + queryTracker.getClazz() );
        Whitebox.setInternalState( realmQuery, "clazz", queryTracker.getClazz());

        handleCollectionMethods(queryTracker);
        handleGroupingQueries(queryTracker);
        handleMathMethods(queryTracker);
        handleSearchMethods(queryTracker);
        handleSortingMethods(queryTracker);
        handleDistinct( queryTracker );

        return realmQuery;
    }

    private static void handleCollectionMethods(QueryTracker queryTracker) {
        RealmQuery realmQuery = queryTracker.getRealmQuery();

        when( realmQuery.findAll() ).thenAnswer(invocation ->{
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            return queryTracker.rewind();
        });

        when( realmQuery.findAllAsync() ).thenAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            return queryTracker.getRealmResults();
        });

        when( realmQuery.findFirst()).thenAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            RealmResults<RealmModel> realmResults = queryTracker.rewind();

            if( realmResults.isEmpty() )
                return null;
            else
                return realmResults.get(0);
        });

        when( realmQuery.findFirstAsync() ).thenAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            RealmObject realmObject = (RealmObject) RealmModelDecorator.create( queryTracker.getClazz(), false );
            RealmObjectDecorator.handleAsyncMethods( realmObject, queryTracker );
            return realmObject;
        });
    }

    private static void handleGroupingQueries(QueryTracker queryTracker) {

        RealmQuery realmQuery = queryTracker.getRealmQuery();
        when( realmQuery.or()).then( invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.or));
            return  realmQuery;
        });

        when( realmQuery.beginGroup()).then( invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.startGroup));
            return  realmQuery;
        });


        when( realmQuery.endGroup()).then( invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endGroup));
            return  realmQuery;
        });


        when( realmQuery.not() ).thenAnswer( invocation -> {
            queryTracker.appendQuery(Query.build().setCondition(Compare.not));
            return realmQuery;
        });
    }

    private static void handleMathMethods( QueryTracker queryTracker){

        RealmQuery realmQuery = queryTracker.getRealmQuery();
        
        when( realmQuery.count() ).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
                RealmResults<RealmModel> realmResults = queryTracker.rewind();
                return realmResults.size();
            }
        });

        when( realmQuery.sum( anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length >= 1 ){
                    queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
                    RealmResults<RealmModel> realmResults = queryTracker.rewind();

                    String fieldName = (String) invocation.getArguments()[0];
                    return realmResults.sum( fieldName );
                }

                return  null;
            }
        });

        when( realmQuery.average(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){
                    queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
                    RealmResults<RealmModel> realmResults = queryTracker.rewind();

                    String fieldName = (String) invocation.getArguments()[0];
                    return realmResults.average(fieldName);
                }

                return  null;
            }
        });


        when( realmQuery.max(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
                    RealmResults<RealmModel> realmResults = queryTracker.rewind();

                    String fieldName = (String) invocation.getArguments()[0];
                    return realmResults.max(fieldName);
                }
                return  null;
            }
        });

        when( realmQuery.min(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){

                    queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
                    RealmResults<RealmModel> realmResults = queryTracker.rewind();

                    String fieldName = (String) invocation.getArguments()[0];
                    return realmResults.min(fieldName);
                }
                return  null;
            }
        });

        when( realmQuery.isNull(anyString())).thenAnswer( invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.isNull).setArgs(invocation.getArguments()));
            return realmQuery;
        });

        when( realmQuery.isNotNull(anyString())).thenAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.not));
            queryTracker.appendQuery( Query.build().setCondition(Compare.isNull).setArgs(invocation.getArguments()));
            return realmQuery;
        });
    }

    private static void handleSearchMethods( QueryTracker queryTracker){
        RealmQuery realmQuery = queryTracker.getRealmQuery();

        when( realmQuery.lessThan( any(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );
        when( realmQuery.lessThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.less ) );

        when( realmQuery.lessThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual ) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.lessOrEqual) );

        when( realmQuery.greaterThan( any(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.more) );
        when( realmQuery.greaterThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.more) );

        when( realmQuery.greaterThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.moreOrEqual) );

        when( realmQuery.between( anyString(), anyInt(), anyInt()  ) ).thenAnswer( createComparison(queryTracker, Compare.between) );
        when( realmQuery.between( anyString(), any(Date.class), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.between) );
        when( realmQuery.between( anyString(), anyDouble(), anyDouble()  ) ).thenAnswer( createComparison(queryTracker, Compare.between) );
        when( realmQuery.between( anyString(), anyFloat(), anyFloat()  ) ).thenAnswer( createComparison(queryTracker, Compare.between) );
        when( realmQuery.between( anyString(), anyLong(), anyLong()  ) ).thenAnswer( createComparison(queryTracker, Compare.between) );
        when( realmQuery.between( anyString(), anyShort(), anyShort()  ) ).thenAnswer( createComparison(queryTracker, Compare.between) );


        when( realmQuery.equalTo( anyString(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.equal ) );


        when( realmQuery.notEqualTo( anyString(), anyInt() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyString() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );
        when( realmQuery.notEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison(queryTracker, Compare.equal, false ) );

        when( realmQuery.contains( anyString(), anyString() ) ).thenAnswer( createComparison(queryTracker, Compare.contains ) );
        when( realmQuery.contains( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison(queryTracker, Compare.contains ) );
        when( realmQuery.endsWith( anyString(), anyString() ) ).thenAnswer( createComparison(queryTracker, Compare.endsWith ) );
        when( realmQuery.endsWith( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison(queryTracker, Compare.endsWith ) );


        when( realmQuery.in( anyString(), any(Integer[].class))).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Byte[].class)) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Double[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Float[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Long[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(String[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(String[].class), any(Case.class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Boolean[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Short[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );
        when( realmQuery.in( anyString(), any(Date[].class) ) ).thenAnswer( createComparison(queryTracker, Compare.in ) );

        when( realmQuery.isEmpty(anyString())).thenAnswer(createComparison(queryTracker, Compare.isEmpty));
        when( realmQuery.isNotEmpty(anyString())).thenAnswer(createComparison(queryTracker, Compare.isEmpty, false));
    }

    private static void handleSortingMethods( QueryTracker queryTracker ){
        RealmQuery realmQuery = queryTracker.getRealmQuery();

        doAnswer(invocation -> {
            String field = (String) invocation.getArguments()[0];
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            queryTracker.appendQuery( Query.build()
                                            .setCondition(Compare.sort)
                                            .setArgs(new Object[]{new QuerySort.SortField(field, true)}) );

            return queryTracker.rewind();
        }).when( realmQuery ).findAllSorted( anyString() );


        doAnswer(invocation -> {
            String field = (String) invocation.getArguments()[0];
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));



            queryTracker.appendQuery( Query.build()
                                        .setCondition(Compare.sort)
                                        .setArgs(new Object[]{new QuerySort.SortField(field, true)}));

            return queryTracker.getRealmResults();
        }).when( realmQuery ).findAllSortedAsync( anyString() );


        doAnswer(invocation -> {
            String field = (String) invocation.getArguments()[0];
            Sort sort = (Sort) invocation.getArguments()[1];

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            queryTracker.appendQuery( Query.build().setCondition(Compare.sort).setArgs(new Object[]{new QuerySort.SortField(field, sort.getValue())}));
            return queryTracker.rewind();
        }).when( realmQuery ).findAllSorted( anyString(), any(Sort.class));

        doAnswer(invocation -> {
            String field = (String) invocation.getArguments()[0];
            Sort sort = (Sort) invocation.getArguments()[1];

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));
            queryTracker.appendQuery( Query.build().setCondition(Compare.sort).setArgs(new Object[]{new QuerySort.SortField(field, sort.getValue())}));
            return queryTracker.getRealmResults();
        }).when( realmQuery ).findAllSortedAsync( anyString(), any(Sort.class));

        doAnswer(invocation -> {

            ArrayList<QuerySort.SortField> sortFields = new ArrayList<QuerySort.SortField>();

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            //sorting goes in reverse order!
            sortFields.add( new QuerySort.SortField((String) invocation.getArguments()[2], ((Sort) invocation.getArguments()[3]).getValue() ));
            sortFields.add( new QuerySort.SortField((String) invocation.getArguments()[0], ((Sort) invocation.getArguments()[1]).getValue() ));

            for (QuerySort.SortField sortField:sortFields) {
                queryTracker.appendQuery( Query.build().setCondition(Compare.sort).setArgs(new Object[]{sortField}));
            }

            return queryTracker.rewind();
        }).when( realmQuery ).findAllSorted( anyString(), any(Sort.class), anyString(), any(Sort.class));


        doAnswer(invocation -> {

            ArrayList<QuerySort.SortField> sortFields = new ArrayList<QuerySort.SortField>();

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            //sorting goes in reverse order!
            sortFields.add( new QuerySort.SortField((String) invocation.getArguments()[2], ((Sort) invocation.getArguments()[3]).getValue() ));
            sortFields.add( new QuerySort.SortField((String) invocation.getArguments()[0], ((Sort) invocation.getArguments()[1]).getValue() ));

            for (QuerySort.SortField sortField:sortFields) {
                queryTracker.appendQuery( Query.build().setCondition(Compare.sort).setArgs(new Object[]{sortField}));
            }

            return queryTracker.getRealmResults();
        }).when( realmQuery ).findAllSortedAsync( anyString(), any(Sort.class), anyString(), any(Sort.class));


        doAnswer(invocation -> {

            String[] fields = (String[])invocation.getArguments()[0];
            Sort[] sorts = (Sort[])invocation.getArguments()[1];

            if( fields.length != sorts.length ){
                throw new RealmException("#mocking-realm: fields and sort arrays don't match" );
            }

            QuerySort.SortField sortField;
            ArrayList<QuerySort.SortField> sortFields = new ArrayList<QuerySort.SortField>();
            int top = fields.length-1;

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            //sorting goes in reverse order!
            for( int i = 0; i <= top; i++ ){

                sortField = new QuerySort.SortField( fields[top-i], sorts[top-i].getValue() );
                queryTracker.appendQuery( Query.build().setCondition(Compare.sort).setArgs(new Object[]{sortField}));
            }

            return queryTracker.rewind();
        }).when( realmQuery ).findAllSorted( any(String[].class), any(Sort[].class));


        doAnswer(invocation -> {

            String[] fields = (String[])invocation.getArguments()[0];
            Sort[] sorts = (Sort[])invocation.getArguments()[1];

            if( fields.length != sorts.length ){
                throw new RealmException("#mocking-realm: fields and sort arrays don't match" );
            }

            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            QuerySort.SortField sortField;
            ArrayList<QuerySort.SortField> sortFields = new ArrayList<QuerySort.SortField>();
            int top = fields.length-1;

            //sorting goes in reverse order!
            for( int i = 0; i <= top; i++ ){

                sortField = new QuerySort.SortField( fields[top-i], sorts[top-i].getValue() );

                queryTracker.appendQuery( Query.build()
                                                .setCondition(Compare.sort)
                                                .setArgs(new Object[]{sortField}));
            }

            return queryTracker.getRealmResults();
        }).when( realmQuery ).findAllSortedAsync( any(String[].class), any(Sort[].class));
    }

    private static void handleDistinct( QueryTracker queryTracker ){

        RealmQuery realmQuery = queryTracker.getRealmQuery();

        doAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            queryTracker.appendQuery( Query.build()
                    .setCondition(Compare.distinct)
                    .setArgs(invocation.getArguments()));

            return queryTracker.rewind();
        }).when(realmQuery).distinct(anyString());

        doAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));

            for( Object arg: invocation.getArguments() ){
                queryTracker.appendQuery(  Query.build()
                                                .setCondition(Compare.distinct)
                                                .setArgs(new Object[]{arg}));
            }
            return queryTracker.rewind();
        }).when(realmQuery).distinct(anyString(), anyVararg() );

        doAnswer(invocation -> {
            queryTracker.appendQuery( Query.build().setCondition(Compare.endTopGroup));



            queryTracker.appendQuery( Query.build()
                                        .setCondition(Compare.distinct)
                                        .setArgs(invocation.getArguments()));

            return queryTracker.getRealmResults();
        }).when(realmQuery).distinctAsync(anyString());
    }


    /**
     * This method filters using args.condition and updates queryMap<realmQuery.clazz, collection>
     * @param queryTracker queryMap.get( realmQuery.clazz ) gives key to get collection from queryMap
     * @param condition based on Compare.enums
     * @return the same args.realmQuery
     */

    public static Answer<RealmQuery> createComparison(QueryTracker queryTracker, String condition ){
        return  createComparison(queryTracker, condition, true );
    };

    public static Answer<RealmQuery> createComparison(QueryTracker queryTracker, String condition, Boolean assertive ){

        RealmQuery realmQuery = queryTracker.getRealmQuery();
        
        return invocationOnMock -> {

            int argsLen = invocationOnMock.getArguments().length;
            String type = "";

            if( argsLen >= 1 ){
                type = (String) invocationOnMock.getArguments()[0];

                if( type.isEmpty() )
                    return realmQuery;
            }
            else if( argsLen < 2 ){
                return realmQuery;
            }

            if( !assertive ){
                queryTracker.appendQuery(Query.build().setCondition(Compare.not));
            }

            queryTracker.appendQuery(Query.build()
                                    .setCondition(condition)
                                    .setField(type)
                                    .setArgs(invocationOnMock.getArguments()));

            return realmQuery;
        };
    }
}