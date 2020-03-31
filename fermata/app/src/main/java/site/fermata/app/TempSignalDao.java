package site.fermata.app;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


@Dao
public interface TempSignalDao {
    @Query("SELECT * FROM tempsignal")
    List<TempSignal> getAllRepos();

    @Insert
    void insert(TempSignal... repos);

    @Delete
    void delete(TempSignal... repos);

    @Query("DELETE FROM tempsignal where timestamp < ( STRFTIME('%s', 'now')-"+ 60*60*24*Constants.CHECK_SPAN_DAY +")")
    void deleteOld();
}
