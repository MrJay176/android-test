package ng.riby.androidtest;
import android.content.Context;



import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import ng.riby.androidtest.models.LocationStore;
import ng.riby.androidtest.models.LocationStoreDao;

@Database(entities = LocationStore.class , version = 2)
public abstract class AppDatabase extends RoomDatabase{

    public static volatile AppDatabase INSTANCE;
    public abstract LocationStoreDao locationStore();

     public static AppDatabase getDatabase(Context context){
        if(INSTANCE == null){
            synchronized(AppDatabase.class){
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"location_database_two")
                        .build();
            }

        }
        return INSTANCE;
    }


    @Override
    public void clearAllTables() {

    }

    public static void destroyInstance(){
        INSTANCE = null;
    }

}
