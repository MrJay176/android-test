package ng.riby.androidtest.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import ng.riby.androidtest.models.LocationStore;
import ng.riby.androidtest.models.LocationViewModel;

public class TrackerService extends Service{

    private BroadcastReceiver mBroadcastReceiverStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //Use BroadCast To Stop Service
                stopForeground(true);
                stopSelf();

            } else {

                stopService(new Intent(getApplicationContext(), TrackerService.class));
                stopSelf();
            }

        }
    };

    private static final String TAG = TrackerService.class.getSimpleName();
    LocationViewModel mLocationViewModel;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.mychannelid.com";
            String channelnAME = "loading";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelnAME, NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true).setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE).build();

            startForeground(3, notification);

            requestLocationUpdates();

        } else {
            startForeground(1, new Notification());
            requestLocationUpdates();

        }

    }


    private void requestLocationUpdates() {
        Log.d("requestLocationUpdate", "requestLocationUpdates: Entered");
        try {

            Log.d("requestLocationUpdate", "requestLocationUpdates: First try passed");

            LocationRequest request = new LocationRequest();
            request.setInterval(500);
            request.setExpirationDuration(TimeUnit.MINUTES.toMillis(30));
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

            // request.setFastestInterval(5000);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {

                SettingsClient settingsClient = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
                Log.d("Permission granted", "requestLocationUpdates: yes ");
                // Request location updates and when an update is
                // received, store the location in Firebase
                client.requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d("Permission granted", "requestLocationUpdates: result gotten ");

                        Location location = locationResult.getLastLocation();
                        if (location != null) {

                            Log.d(TAG, "location update " + location);
                            Log.d(TAG, "location Latitude " + location.getLatitude());
                            Log.d(TAG, "location Longitude " + location.getLongitude());

                            LocationStore locations = new LocationStore();
                            locations.setLatitude(location.getLatitude());
                            locations.setLongitude(location.getLongitude());

                            Intent intent = new Intent("www.sent.com");
                            intent.putExtra("locations",locations);

                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(intent);



                        }

                        //sendLocationToRealm(location.getLatitude(),location.getLongitude());

                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);
                        Log.d("checlkkkkkk", "onLocationAvailability: " + locationAvailability.isLocationAvailable());

                        if (!locationAvailability.isLocationAvailable()) {

                        }

                    }
                }, null);
            } else {

                Log.d("Permission granted", "requestLocationUpdates: no ");

            }
        } catch (Exception e) {

            Log.d("requestLocationUpdate", "requestLocationUpdates: First try not passed");

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("TrackerOnStartCommand", "onStartCommand: Running Running");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mBroadcastReceiverStop,new IntentFilter("www.stop.com"));


        requestLocationUpdates();

        return START_STICKY;
    }

}
