package ng.riby.androidtest.models;

import android.app.Application;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import ng.riby.androidtest.AppDatabase;

public class LocationViewModel extends AndroidViewModel{

    private LocationStoreDao mLocationDao;

    //public final LiveData<LocationStore> locations;
    public final LiveData<List<LocationStore>> mListLiveData;

    public LocationViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = AppDatabase.getDatabase(application);
        mLocationDao = database.locationStore();
        //locations =
        mListLiveData = mLocationDao.loadAllLocations();

    }


    public LiveData<List<LocationStore>> getAllData (){
        return  mListLiveData;
    }

    public LiveData<LocationStore> getLocations (String id){
        return  mLocationDao.getLocationStore(id);
    }

    public void insert(LocationStore locationStore){
        new InsertAsyncTask(mLocationDao).execute(locationStore);
    }

    public void delete(String id){
        new DeleteAsyncTask(mLocationDao).execute(id);
    }

    private static class InsertAsyncTask extends AsyncTask<LocationStore , Void ,Void>{

        LocationStoreDao mLocationDao;

        public InsertAsyncTask(LocationStoreDao mLocationDao){
            this.mLocationDao = mLocationDao;
        }

        @Override
        protected Void doInBackground(LocationStore... locationStores) {

            Log.d("backcheck", "doInBackground: "+locationStores[0].latitude);
            Log.d("backcheck", "doInBackground: "+locationStores[0].longitude);
            Log.d("backcheck", "doInBackground: "+locationStores[0].getId());

            mLocationDao.insert(locationStores[0].latitude , locationStores[0].longitude , locationStores[0].getId());
            return null;

        }
    }

    private static class DeleteAsyncTask extends AsyncTask<String, Void ,Void>{

        LocationStoreDao mLocationDao;

        public DeleteAsyncTask(LocationStoreDao mLocationDao){
            this.mLocationDao = mLocationDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mLocationDao.deleteByUserId(strings[0]);
            return null;
        }
    }
}
