/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 로그 클래스 -
 */

package site.fermata.app.db;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "signallog")
public class SignalLog {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;


    public String uuid;

    public int timestamp;

    public boolean uploaded;
    public  boolean on;
    public  int  rssi;
    public  int timespan;



    public SignalLog( String uuid , int rssi , boolean on, int timespan, int timestamp) {

        this.uuid = uuid;
        this.timestamp = timestamp  ;
        this.timespan= timespan;
        this.rssi= rssi;
        this.on=on;
        this.uploaded=false;

    }

}
