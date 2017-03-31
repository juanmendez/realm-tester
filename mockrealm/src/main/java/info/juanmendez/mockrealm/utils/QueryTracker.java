package info.juanmendez.mockrealm.utils;

import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;

import info.juanmendez.mockrealm.decorators.RealmListDecorator;
import info.juanmendez.mockrealm.decorators.RealmResultsDecorator;
import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.models.Query;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by @juanmendezinfo on 2/19/2017.
 */
public class QueryTracker {

    private ArrayList<Query> queries = new ArrayList<>();

    private ArrayList<Boolean> queryAndList = new ArrayList<>();
    private ArrayList<Boolean> queryYesList = new ArrayList<>();
    private ArrayList<RealmList<RealmModel>> groupResults = new ArrayList<>();
    private int groupLevel = 0;
    private Class clazz;
    private RealmQuery realmQuery;
    private RealmResults realmResults;

    /**
     * cloned object preserves realmList, check clone method
     */
    private RealmList<RealmModel> parentRealmList;

    public QueryTracker(Class clazz){
        this.clazz = clazz;
        realmQuery = mock(RealmQuery.class);
        setUpRealmResults();
    }

    private void setUpRealmResults(){
        //beforehand we are going to take care of realmResults
        realmResults = PowerMockito.mock( RealmResults.class );

        //level 0, we are going to start a realmList
        groupResults.add( RealmListDecorator.create() );
        RealmResultsDecorator.create( this );
    }

    private void onTopGroupBegin(RealmList<RealmModel> realmList ){

        //we update the current list instead of assigning one.
        groupResults.get(groupLevel).clear();
        groupResults.get(groupLevel).addAll( realmList );
        queryAndList.add( true );
        queryYesList.add( true );

        onBeginGroupClause();
    }

    public void setQueryList( RealmList<RealmModel> queryList ){
        if( queryAndList.get( groupLevel ) ){
            groupResults.get( groupLevel ).clear();
            groupResults.get( groupLevel ).addAll( queryList );
        }else{

            RealmList<RealmModel> currentGroupList = groupResults.get( groupLevel );

            for (RealmModel realmModel: queryList) {
              if( !currentGroupList.contains( realmModel)){
                  currentGroupList.add( realmModel );
              }
            }
        }

        //if the las query is based on OR(), then bounce back to AND()
        queryAndList.set( groupLevel, true );
    }

    private void onOrClause() {
        queryAndList.set( groupLevel, false );
    }

    private void onNotClause(){
        queryYesList.set( groupLevel, false );
    }

    private void onBeginGroupClause(){

        RealmList<RealmModel> previousGroupList = groupResults.get( groupLevel );

        RealmList<RealmModel> nextGroupList = RealmListDecorator.create();
        nextGroupList.addAll( previousGroupList );

        groupLevel++;
        groupResults.add( nextGroupList );
        queryAndList.add( true );
        queryYesList.add( true );
    }

    private void onCloseGroupClause(){

        RealmList<RealmModel> currentGroupList = groupResults.get( groupLevel );
        groupResults.remove( groupLevel );
        queryAndList.remove( groupLevel );
        queryYesList.remove( groupLevel );

        groupLevel--;

        if( groupLevel < 0 ){
            throw( new RealmException("There is an attempt to close more than the number of groups created" ));
        }

        groupResults.get(groupLevel).clear();
        groupResults.get(groupLevel).addAll( currentGroupList );
    }

    private void onTopGroupClose(){
        onCloseGroupClause();

        if( groupLevel > 0 ){
            throw( new RealmException("Required to close all groups. Current group level is " + groupLevel ));
        }
    }

    private Boolean executeGroupQuery(Query query ){

        Boolean used = false;

        switch ( query.getCondition() ){

            case Compare.startTopGroup:
                onTopGroupBegin( (RealmList<RealmModel>) query.getArgs()[0] );
                used = true;
                break;
            case Compare.startGroup:
                onBeginGroupClause();
                used = true;
                break;
            case Compare.or:
                onOrClause();
                used = true;
                break;
            case Compare.not:
                onNotClause();
                used = true;
                break;
            case Compare.endGroup:
                onCloseGroupClause();
                used = true;
                break;
            case Compare.endTopGroup:
                onTopGroupClose();
                used = true;
                break;
        }

        return used;
    }

    public Class getClazz() {
        return clazz;
    }

    public void appendQuery( Query query ){
        queries.add( query );
    }

    public RealmList<RealmModel> getQueryList(){

        if( !queryAndList.isEmpty() && !queryAndList.get( groupLevel )){
            return groupResults.get(groupLevel-1);
        }

        return groupResults.get(groupLevel);
    }

    public ArrayList<Query> getQueries(){
        return queries;
    }

    @Override
    public QueryTracker clone(){
        QueryTracker cloned = new QueryTracker( this.clazz );

        if( parentRealmList != null ){
            cloned.parentRealmList = parentRealmList;
        }else{
            cloned.parentRealmList = getQueryList();
        }

        return cloned;
    }

    public RealmQuery getRealmQuery() {
        return realmQuery;
    }

    public RealmResults getRealmResults() {
        return realmResults;
    }

    public RealmList<RealmModel> getParentRealmList() {
        return parentRealmList;
    }

    public RealmResults<RealmModel> rewind(){
        ArrayList<Query> queries = getQueries();
        RealmList<RealmModel> searchList;

        for ( Query query: queries ){

            if( !executeGroupQuery( query ) ){

                if( groupLevel >=1 ){
                    query.setAsTrue( queryYesList.get(groupLevel) && queryYesList.get(groupLevel-1) );
                }

                if( query.getCondition() == Compare.sort ){
                    searchList = new QuerySort().perform( query, getQueryList() );
                    setQueryList( searchList );

                } else if( query.getCondition() == Compare.distinct ){
                    searchList = new QueryDistinct().perform( query, getQueryList() );
                    setQueryList( searchList );
                } else{
                    searchList = new QuerySearch().search( query, getQueryList()  );
                    setQueryList( searchList );
                }

                queryYesList.set( groupLevel, true );
            }
        }

        return realmResults;
    }
}