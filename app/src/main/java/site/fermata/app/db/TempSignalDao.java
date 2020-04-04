package site.fermata.app.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import site.fermata.app.Constants;


@Dao
public interface TempSignalDao {
    @Query("SELECT * FROM tempsignal")
    List<TempSignal> getAllSignal();

    @Insert
    void insert(TempSignal... tempsignal);

/*    @Delete
    void delete(TempSignal... tempsignal);*/

    @Query("DELETE FROM tempsignal where timestamp < ( STRFTIME('%s', 'now')-"+ 60*60*24* Constants.CHECK_SPAN_DAY +")")
    void deleteOld();
}
