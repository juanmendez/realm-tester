package info.juanmendez.mock.realm.models;

import io.realm.*;

import java.util.ArrayList;

/**
 * Created by musta on 2/19/2017.
 */
public class QueryWatcher {


    private boolean asAnd = true;
    private ArrayList<RealmList<RealmModel>> groupResults = new ArrayList<>();
    private int groupLevel = 0;

    public QueryWatcher(){

    }

    public void onWhereClause( RealmList<RealmModel> realmList ){

        groupLevel = 0;
        groupResults.clear();
        groupResults.add( realmList );
        onStartGroupClause();
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

        //default is always true!
        this.asAnd = true;
    }


    public void onOrClause() {
        this.asAnd = false;
    }

    public void onStartGroupClause(){

        RealmList<RealmModel> previousGroupList = groupResults.get( groupLevel );

        RealmList<RealmModel> nextGroupList = new RealmList<>();
        for (RealmModel realmModel: previousGroupList) {
            nextGroupList.add( realmModel );
        }

        groupLevel++;
        groupResults.add( nextGroupList );
    }

    public void onEndGroupClause(){

        RealmList<RealmModel> currentGroupList = groupResults.get( groupLevel );
        groupResults.remove( groupLevel );

        groupResults.set( groupLevel--, currentGroupList );
    }

    public void onFindAllClause(){
        onEndGroupClause();
    }

    public void onFindFirstClause(){
        onEndGroupClause();
    }
}
