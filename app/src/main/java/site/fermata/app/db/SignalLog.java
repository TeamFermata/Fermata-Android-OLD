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
