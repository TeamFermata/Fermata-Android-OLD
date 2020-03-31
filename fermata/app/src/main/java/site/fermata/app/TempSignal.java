package site.fermata.app;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tempsignal")
public class TempSignal {
    @PrimaryKey
    public String uuid;

    public int timestamp;
    public TempSignal( String name) {
        this.uuid = name;
        this.timestamp = (int)  (System.currentTimeMillis()/1000L )  ;
       }


}




