package info.juanmendez.mock.realm.factories;

import info.juanmendez.mock.realm.dependencies.Compare;
import info.juanmendez.mock.realm.dependencies.RealmStorage;
import io.realm.Case;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.exceptions.RealmException;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class QueryFactory {

    //collections queried keyed by immediate class

    public static RealmQuery create(Class clazz ){

        HashMap<Class, RealmList<RealmModel>> realmMap = RealmStorage.getRealmMap();
        HashMap<Class, RealmList<RealmModel>> queryMap = RealmStorage.getQueryMap();
        queryMap.put(clazz, realmMap.get(clazz) );

        RealmQuery realmQuery = mock(RealmQuery.class);
        when( realmQuery.toString() ).thenReturn( "Realm:" + clazz.getName() );
        Whitebox.setInternalState( realmQuery, "clazz", clazz);

        when( realmQuery.findAll() ).thenAnswer(invocationOnMock ->{
            return ResultsFactory.create( clazz );
        });

        when( realmQuery.findFirst()).thenAnswer(invocationOnMock -> {
            RealmList<RealmModel> realResults = queryMap.get(clazz);
            return realResults.get(0);
        });

        when( realmQuery.count() ).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {

                return ((Number)queryMap.get(clazz).size()).longValue();
            }
        });


        when( realmQuery.sum( anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {

                if( invocation.getArguments().length >= 1 ){
                    String fieldName = (String) invocation.getArguments()[0];
                    return queryMap.get(clazz).sum( fieldName );
                }

                return  null;
            }
        });

        when( realmQuery.average(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){
                    String fieldName = (String) invocation.getArguments()[0];
                    return queryMap.get(clazz).average(fieldName);
                }

                return  null;
            }
        });


        when( realmQuery.max(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){
                    String fieldName = (String) invocation.getArguments()[0];
                    return queryMap.get(clazz).max(fieldName);
                }
                return  null;
            }
        });

        when( realmQuery.min(anyString()) ).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                if( invocation.getArguments().length >= 1 ){
                    String fieldName = (String) invocation.getArguments()[0];
                    return queryMap.get(clazz).min(fieldName);
                }
                return  null;
            }
        });

        //TODO whens will also be moved over another class
        when( realmQuery.lessThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );
        when( realmQuery.lessThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.less ) );

        when( realmQuery.lessThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual ) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );
        when( realmQuery.lessThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.lessOrEqual) );

        when( realmQuery.greaterThan( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.more) );
        when( realmQuery.greaterThan( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.more) );

        when( realmQuery.greaterThanOrEqualTo( any(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );
        when( realmQuery.greaterThanOrEqualTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.moreOrEqual) );

        when( realmQuery.equalTo( anyString(), anyInt() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyByte()) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyDouble() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyFloat() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyLong() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), anyBoolean() ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );
        when( realmQuery.equalTo( anyString(), any(Date.class) ) ).thenAnswer( createComparison( realmQuery, Compare.equal ) );

        when( realmQuery.contains( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.contains ) );
        when( realmQuery.contains( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.contains ) );
        when( realmQuery.endsWith( anyString(), anyString() ) ).thenAnswer( createComparison( realmQuery, Compare.endsWith ) );
        when( realmQuery.endsWith( anyString(), anyString(), any(Case.class) ) ).thenAnswer( createComparison( realmQuery, Compare.endsWith ) );

        return realmQuery;
    }


    /**
     * This method filters using args.condition and updates queryMap<realmQuery.clazz, collection>
     * @param realmQuery queryMap.get( realmQuery.clazz ) gives key to get collection from queryMap
     * @param condition based on Compare.enums
     * @return the same args.realmQuery
     */
    private static Answer<RealmQuery> createComparison( RealmQuery realmQuery, String condition ){

        Class realmQueryClass = (Class) Whitebox.getInternalState( realmQuery, "clazz");
        
        return new Answer<RealmQuery>() {
            @Override
            public RealmQuery answer(InvocationOnMock invocationOnMock) throws Throwable {

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

                HashMap<Class, RealmList<RealmModel>> queryMap = RealmStorage.getQueryMap();
                RealmList<RealmModel> searchList = queryMap.get(realmQueryClass);

                QueryList queryList = new QueryList();
                RealmList<RealmModel> realmList = queryList.search( searchList, condition, invocationOnMock.getArguments() );
                queryMap.put( realmQueryClass, realmList);

                return realmQuery;
            }
        };
    }

    static class QueryList {

        String condition;
        ArrayList<String> types;
        Object[] arguments;
        Object needle;
        Class clazz;
        Case casing = Case.SENSITIVE;

        public RealmList<RealmModel> search( RealmList<RealmModel> haystack, String condition, Object[] arguments ){

            this.condition = condition;
            this.arguments = arguments;
            this.types = new ArrayList<>(Arrays.asList(((String) arguments[0]).split("\\.")));

            this.needle = arguments[1];
            this.clazz = needle.getClass();

            int argsLen = arguments.length;

           if( clazz == String.class && argsLen >= 3 ){
                casing = (Case) arguments[2];
            }

            RealmList<RealmModel> queriedList = new RealmList<>();

            for (RealmModel realmModel: haystack) {
                if( checkRealmObject( realmModel, 0 )){
                    queriedList.add( realmModel );
                }
            }

            return queriedList;
        }

        private boolean checkRealmObject(RealmModel realmModel, int level ){
            //RunTimeErrorException if search field is not found in realmQueryClass

            Object value;

            try {
                value = Whitebox.getInternalState( realmModel, types.get(level) );
            } catch (Exception e) {
                throw (new RealmException( realmModel.getClass().getName() + " doesn't have the attribute " + types.get(level) ) );
            }

            if( value != null ){

                if( level < types.size() - 1 && value instanceof RealmList  ){

                    RealmList<RealmModel> valueList = (RealmList<RealmModel>) value;

                    for ( RealmModel rm: valueList) {

                        if( checkRealmObject(rm, level+1)){
                            return true;
                        }
                    }

                    return false;
                }

                if( condition == Compare.equal ){

                    if( clazz == Date.class && ( ((Date)value) ).compareTo( (Date) needle) == 0 ){
                        return true;
                    }else if(needle.equals(value)){
                        return true;
                    }
                }
                else
                if( condition == Compare.less){

                    if( clazz == Date.class && ( ((Date)value) ).compareTo( (Date) needle) < 0 ){
                        return true;
                    }
                    else if( clazz == Byte.class && ((byte)value) < ((byte) needle)){
                        return true;
                    }
                    else if( clazz == Integer.class && ((int)value) < ((int) needle) ){
                        return true;
                    }
                    else if( clazz == Double.class && ((double)value) < ((double) needle) ){
                        return true;
                    }
                    else if( clazz == Long.class && ((long)value) < ((long) needle) ){
                        return true;
                    }
                    else if( clazz == Float.class && ((float)value) < ((float) needle) ){
                        return true;
                    }
                }
                else if( condition == Compare.lessOrEqual){

                    if( clazz == Date.class && ( ((Date)value) ).compareTo( (Date) needle) <= 0 ){
                        return true;
                    }
                    else if( clazz == Byte.class && ((byte)value) <= ((byte) needle)){
                        return true;
                    }
                    else if( clazz == Integer.class && ((int)value) <= ((int) needle) ){
                        return true;
                    }
                    else if( clazz == Double.class && ((double)value) <= ((double) needle) ){
                        return true;
                    }
                    else if( clazz == Long.class && ((long)value) <= ((long) needle) ){
                        return true;
                    }
                    else if( clazz == Float.class && ((float)value) <= ((float) needle) ){
                        return true;
                    }
                }
                else if( condition == Compare.more ){

                    if( clazz == Date.class && ( ((Date)value) ).compareTo( (Date) needle) > 0 ){
                        return true;
                    }
                    else if( clazz == Byte.class && ((byte)value) > ((byte) needle)){
                        return true;
                    }
                    else if( clazz == Integer.class && ((int)value) > ((int) needle) ){
                        return true;
                    }
                    else if( clazz == Double.class && ((double)value) > ((double) needle) ){
                        return true;
                    }
                    else if( clazz == Long.class && ((long)value) > ((long) needle) ){
                        return true;
                    }
                    else if( clazz == Float.class && ((float)value) > ((float) needle) ){
                        return true;
                    }
                }
                else if( condition == Compare.moreOrEqual ){

                    if( clazz == Date.class && ( ((Date)value) ).compareTo( (Date) needle) >= 0 ){
                        return true;
                    }
                    else if( clazz == Byte.class && ((byte)value) >= ((byte) needle)){
                        return true;
                    }
                    else if( clazz == Integer.class && ((int)value) >= ((int) needle) ){
                        return true;
                    }
                    else if( clazz == Double.class && ((double)value) >= ((double) needle) ){
                        return true;
                    }
                    else if( clazz == Long.class && ((long)value) >= ((long) needle) ){
                        return true;
                    }
                    else if( clazz == Float.class && ((float)value) >= ((float) needle) ){
                        return true;
                    }
                }
                else if( clazz == String.class && (condition == Compare.contains || condition == Compare.endsWith)  ){

                    if( condition == Compare.contains ){
                        if( casing == Case.SENSITIVE && ((String)value).contains((String ) needle)  ){
                            return true;
                        }
                        else
                        if(casing == Case.INSENSITIVE && (((String)value).toLowerCase()).contains(((String ) needle).toLowerCase())) {
                            return true;
                        }
                    }
                    else if( condition == Compare.endsWith ){
                        if( casing == Case.SENSITIVE && ((String)value).endsWith((String ) needle)  ){
                            return true;
                        }
                        else
                        if(casing == Case.INSENSITIVE && (((String)value).toLowerCase()).endsWith(((String ) needle).toLowerCase()))
                        {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }
}
