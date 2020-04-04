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


    @Insert
    void insert(SignalLog... signallog);

 /*   @Delete
    void delete(SignalLog... tempsignal);*/

    @Query("DELETE FROM signallog where timestamp < ( STRFTIME('%s', 'now')-"+ 60*60*24* Constants.CHECK_SPAN_DAY +")")
    void deleteOld();
}
