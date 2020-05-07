/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 로그 데이터베이스 -
 */

package site.fermata.app.db;
import java.util.List;

import androidx.room.Dao;

import androidx.room.Insert;
import androidx.room.Query;
import site.fermata.app.Constants;

@Dao
public interface SignalLogDao {

    @Query("SELECT * FROM signallog")
    List<SignalLog> getAllSignal();

    @Query("UPDATE signallog SET timespan=:timespan , rssi=MAX(rssi,:newrssi) where uuid = :ID and timestamp= :timestamp")
    void updateTimeSpan(String ID,int timestamp , int timespan , int newrssi );



    @Query("SELECT DISTINCT uuid FROM signallog where uploaded = 0  and  timestamp <:ctime")
    String[] getUUIDRecord(int ctime);


    @Query("UPDATE signallog SET uploaded=1  where  timestamp< :timestamp")
    void flagUploaded(int timestamp );


    @Query("SELECT * FROM signallog WHERE uuid IN(:userIds)")
    SignalLog[] find(String[] userIds);

    @Insert
    void insert(SignalLog... signallog);

 /*   @Delete
    void delete(SignalLog... tempsignal);*/

    @Query("DELETE FROM signallog where timestamp < ( STRFTIME('%s', 'now')-"+ 60*60*24* Constants.CHECK_SPAN_DAY +")")
    void deleteOld();
}
