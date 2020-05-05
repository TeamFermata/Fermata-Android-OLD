package site.fermata.app.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TempSignal.class, SignalLog.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "appDatabase.db";
    private static volatile AppDatabase instance;
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };
    private static AppDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                DB_NAME)
               // fallbackToDestructiveMigration()
             //  .addMigrations(MIGRATION_3_4)
                .build();
    }

    public abstract SignalLogDao getSignalLogDao();
    public abstract TempSignalDao getTempSignalDao();
}

