package info.juanmendez.mock.realm.models;

import java.util.ArrayList;

import info.juanmendez.mock.realm.factories.ListFactory;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Created by @juanmendezinfo on 2/19/2017.
 */
public class QueryNest {

    private ArrayList<Query> queries = new ArrayList<>();

    private boolean asAnd = true;
    private ArrayList<RealmList<RealmModel>> groupResults = new ArrayList<>();
    private int groupLevel = 0;
    private Class clazz;

    public QueryNest(Class clazz){
        this.clazz = clazz;
    }

    public void onTopGroupBegin(RealmList<RealmModel> realmList ){
        groupResults.add( realmList );
        onBeginGroupClause();
    }

    public RealmList<RealmModel> getQueryList(){

        if( !asAnd){
            return groupResults.get(groupLevel-1);
        }

        return groupResults.get(groupLevel);
    }

    public void setQueryList( RealmList<RealmModel> queryList ){
        if( asAnd ){
            groupResults.set( groupLevel, queryList );
        }else{

            RealmList<RealmModel> currentGroupList = groupResults.get( groupLevel );

            for (RealmModel realmModel: queryList) {
              if( !currentGroupList.contains( realmModel)){
                  currentGroupList.add( realmModel );
              }
            }
        }

        //if the las query is based on OR(), then bounce back to AND()
        this.asAnd = true;
    }


    public void onOrClause() {
        this.asAnd = false;
    }

    public void onBeginGroupClause(){

        RealmList<RealmModel> previousGroupList = groupResults.get( groupLevel );

        RealmList<RealmModel> nextGroupList = ListFactory.create();
        for (RealmModel realmModel: previousGroupList) {
            nextGroupList.add( realmModel );
        }

        groupLevel++;
        groupResults.add( nextGroupList );
    }

    public void onCloseGroupClause(){

        RealmList<RealmModel> currentGroupList = groupResults.get( groupLevel );
        groupResults.remove( groupLevel );

        groupLevel--;

        if( groupLevel < 0 ){
            throw( new RealmException("There is an attempt to close more than the number of groups created" ));
        }

        groupResults.set( groupLevel, currentGroupList );
    }

    public void onTopGroupClose(){
        onCloseGroupClause();

        if( groupLevel > 0 ){
            throw( new RealmException("Required to close all groups. Current group level is " + groupLevel ));
        }
    }

    public Class getClazz() {
        return clazz;
    }

    public void appendQuery( Query query ){
        queries.add( query );
    }

    public ArrayList<Query> getQueries(){
        return queries;
    }

    @Override
    public QueryNest clone(){
        return new QueryNest( this.clazz );
    }
}
