/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 임시 신호 클래스 -
 */

package site.fermata.app.db;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tempsignal")
public class TempSignal {
    @PrimaryKey
    @NonNull
    public String uuid;
    public int timestamp;

    public TempSignal( String uuid ) {
        this.uuid = uuid;
        this.timestamp = (int)  (System.currentTimeMillis()/1000L )  ;
       }


}




