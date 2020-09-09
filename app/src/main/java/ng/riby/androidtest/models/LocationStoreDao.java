package ng.riby.androidtest.models;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface LocationStoreDao {

    @Query("select * from locations_two")
    LiveData<List<LocationStore>> loadAllLocations();

    @Query("select * from locations_two where id=:id")
    LiveData<LocationStore> getLocationStore(String id);

    @Query("update locations_two set " +
            "latitude = :latitude, longitude = :longitude where id = :id")
    void insert(double latitude , double longitude , String id);

    @Query("delete from locations_two where id=:id")
    void deleteByUserId(String id);

}
