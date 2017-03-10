package info.juanmendez.mockrealm.factories;

import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;

import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.dependencies.RealmStorage;
import info.juanmendez.mockrealm.models.Query;
import info.juanmendez.mockrealm.models.QueryNest;
import io.realm.Case;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class QueryFactory {

    //collections queried keyed by immediate class

    public static RealmQuery create( QueryNest queryNest){

        RealmQuery realmQuery = mock(RealmQuery.class);

        when( realmQuery.toString() ).thenReturn( "Realm:" + queryNest.getClazz() );
        Whitebox.setInternalState( realmQuery, "clazz", queryNest.getClazz());

        handleCollectingMethods( realmQuery, queryNest );
        handleNestingMethods( realmQuery, queryNest );
        handleMathMethods( realmQuery, queryNest);
        handleSearchMethods(  realmQuery );


        return realmQuery;
    }

    private static void handleCollectingMethods(RealmQuery realmQuery, QueryNest queryNest) {

        when( realmQuery.findAll() ).thenAnswer(invocationOnMock ->{
            return generateList( queryNest );
        });

        when( realmQuery.findFirst()).thenAnswer(invocationOnMock -> {

            RealmResults<RealmModel> realmResults = generateList( queryNest );
            return realmResults.get(0);
        });
    }

    private static void handleNestingMethods(RealmQuery realmQuery, QueryNest queryNest) {

        when( realmQuery.or()).then( invocation -> {
            queryNest.appendQuery( new Query(Compare.or));
            return  realmQuery;
        });

        when( realmQuery.beginGroup()).then( invocation -> {
            queryNest.appendQuery( new Query(Compare.startGroup));
            return  realmQuery;
        });


        when( realmQuery.endGroup()).then( invocation -> {
            queryNest.appendQuery( new Query(Compare.endGroup));
            return  realmQuery;
        });
    }

    private static void handleMathMethods( RealmQuery realmQuery, QueryNest queryNest){

        when( realmQuery.count() ).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                RealmResults<RealmModel> realmResults = generateList( queryNest );
                return realmResults.size();
            }
        });

        when( realmQuery.sum( anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length >= 1 ){
                    RealmResults<RealmModel> realmResults = generateList( queryNest );

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
                    RealmResults<RealmModel> realmResults = generateList( queryNest );

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

                    RealmResults<RealmModel> realmResults = generateList( queryNest );

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

                    RealmResults<RealmModel> realmResults = generateList( queryNest );

                    String fieldName = (String) invocation.getArguments()[0];
                    return realmResults.min(fieldName);
                }
                return  null;
            }
        });
    }

    private static void handleSearchMethods( RealmQuery realmQuery ){
        when( realmQuery.lessThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );

        when( realmQuery.lessThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual ) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );

        when( realmQuery.greaterThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.more) );

        when( realmQuery.greaterThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );

        when( realmQuery.between( anyString(), anyInt(), anyInt()  ) ).thenAnswer( createComparison( realmQuery, Compare.between) );
        when( realmQuery.between( anyString(), any(Date.class), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.between) );
        when( realmQuery.between( anyString(), anyDouble(), anyDouble()  ) ).thenAnswer( createComparison( realmQuery, Compare.between) );
        when( realmQuery.between( anyString(), anyFloat(), anyFloat()  ) ).thenAnswer( createComparison( realmQuery, Compare.between) );
        when( realmQuery.between( anyString(), anyLong(), anyLong()  ) ).thenAnswer( createComparison( realmQuery, Compare.between) );
        when( realmQuery.between( anyString(), anyShort(), anyShort()  ) ).thenAnswer( createComparison( realmQuery, Compare.between) );


        when( realmQuery.equalTo( anyString(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );


        when( realmQuery.notEqualTo( anyString(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), anyShort() ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );
        when( realmQuery.notEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.not_equal ) );

        when( realmQuery.contains( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.contains ) );
        when( realmQuery.contains( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.contains ) );
        when( realmQuery.endsWith( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.endsWith ) );
        when( realmQuery.endsWith( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.endsWith ) );


        when( realmQuery.in( anyString(), any(Integer[].class))).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Byte[].class)) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Double[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Float[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Long[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(String[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(String[].class), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Boolean[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Short[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
        when( realmQuery.in( anyString(), any(Date[].class) ) ).thenAnswer( createComparison( realmQuery, Compare.in ) );
    }


    /**
     * This method filters using args.condition and updates queryMap<realmQuery.clazz, collection>
     * @param realmQuery queryMap.get( realmQuery.clazz ) gives key to get collection from queryMap
     * @param condition based on Compare.enums
     * @return the same args.realmQuery
     */

    private static Answer<RealmQuery> createComparison( RealmQuery realmQuery, String condition ){

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

            QueryNest queryNest = RealmStorage.getQueryMap().get( realmQuery );
            queryNest.appendQuery( new Query(condition, type, invocationOnMock.getArguments()));

            return realmQuery;
        };
    }

    private static RealmResults generateList(QueryNest queryNest ){
        ArrayList<Query> queries = queryNest.getQueries();
        RealmList<RealmModel> searchList;

        for ( Query query: queries ){

            if( !queryNest.isUsedByQuery( query ) ){

                searchList = new QuerySearch().search( query.getCondition(), query.getArgs(), queryNest.getQueryList()  );
                queryNest.setQueryList( searchList );
            }
        }

        return ResultsFactory.create( queryNest );
    }

}