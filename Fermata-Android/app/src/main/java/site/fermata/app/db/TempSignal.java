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




