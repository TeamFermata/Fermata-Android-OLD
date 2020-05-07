/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 임시 시그널 데이터베이스 -
 */

package site.fermata.app.db;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import site.fermata.app.Constants;

@Dao
public interface TempSignalDao {
    @Query("SELECT uuid FROM tempsignal")
    List<String> getAllSignal();

    @Insert
    void insert(TempSignal... tempsignal);

/*    @Delete
    void delete(TempSignal... tempsignal);*/

    @Query("DELETE FROM tempsignal where timestamp < ( STRFTIME('%s', 'now')-"+ 60*60*24* Constants.CHECK_SPAN_DAY +")")
    void deleteOld();
}
