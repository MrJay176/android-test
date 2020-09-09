package ng.riby.androidtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import ng.riby.androidtest.models.LocationStore;
import ng.riby.androidtest.models.LocationViewModel;
import ng.riby.androidtest.services.TrackerService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LocationViewModel mLocationViewModel;
    boolean onePassed = false;

    private ProgressDialog mProgressDialog;

    private Button mStartButton, mStopButton;
    private TextView mTextViewDisplayOne,mTextViewDisplayTwo ,mTextViewThree , mTextViewMove;

    public static final int PERMISSION_COUNT = 2;
    public static final int REQUEST_PERMISSIONS = 1234;
    public static final String[] PERMISSION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };



    private BroadcastReceiver mBroadcastSent = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {

            LocationStore locations = intent.getParcelableExtra("locations");

            if(onePassed){
                locations.setId("2");
                mLocationViewModel.insert(locations);
                Log.d("two", "onReceive: "+locations.latitude);
                Log.d("two", "onReceive: "+locations.longitude);
                try{
                locationStoress.set(1,locations);
                }catch (IndexOutOfBoundsException got){
                    locationStoress.add(locations);
                }
            }else {
                locations.setId("1");
                locationStoress.clear();
                locationStoress.add(locations);
                DismissDialog();
                mTextViewDisplayOne.setText("Your Current Location is Latitude = "+locations.getLatitude()
                        +" Longitude = "+locations.getLongitude());
                onePassed = true;
            }
        }
    };

    List<LocationStore> locationStoress = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsDenied(){
        for(int i = 0 ; i<PERMISSION_COUNT;i++){
            if(Objects.requireNonNull(getApplicationContext()).checkSelfPermission(PERMISSION[i])!=
                    PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_PERMISSIONS&&grantResults.length>0){
            if(arePermissionsDenied()){
                ((ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE))
                        .clearApplicationUserData();
                Toast.makeText(this, "Turn On Location", Toast.LENGTH_LONG).show();
            }else {
                onResume();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&arePermissionsDenied()){
            requestPermissions(PERMISSION,REQUEST_PERMISSIONS);
            return;
        }

        mLocationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting your current Location");
        mProgressDialog.setCancelable(false);

        mTextViewMove = findViewById(R.id.text_move);
        mTextViewMove.setVisibility(View.GONE);

        mStartButton = findViewById(R.id.start);
        mStartButton.setOnClickListener(this);

        mStopButton = findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mStopButton.setVisibility(View.GONE);
        mTextViewDisplayOne =findViewById(R.id.text_display_one);
        mTextViewDisplayOne.setOnClickListener(this);

        mTextViewDisplayTwo =findViewById(R.id.text_display_two);
        mTextViewDisplayTwo.setOnClickListener(this);

        mTextViewThree = findViewById(R.id.text_display_three);
        mTextViewThree.setOnClickListener(this);

        CheckIfLocationIsEnabled();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mBroadcastSent,new IntentFilter("www.sent.com"));



    }

    //====Method To Check If Location is enabled==============================
    private boolean CheckIfLocationIsEnabled() {
        boolean IsLocationEnabled = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        IsLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!IsLocationEnabled) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Turn On GPS , Location Needed to track movement ").setPositiveButton("Open GPS settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();

        }
        return IsLocationEnabled;
    }
    private void useTrackerSerice(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("Main", "useTrackerSerice: Tracker First");
            startForegroundService(new Intent(getApplicationContext(), TrackerService.class));
        } else {
            Log.d("Main", "useTrackerSerice: Tracker Second");
            startService(new Intent(getApplicationContext(), TrackerService.class));
        }


    }


    private void stopTracking(){
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("www.stop.com"));
        if(mLocationViewModel!=null && mLocationViewModel.getAllData().hasObservers()){
            mLocationViewModel.getAllData().removeObservers(this);
        }
        calculateDistance(locationStoress);

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastSent);

    }

    private void saveData(final LocationStore location){

        mLocationViewModel.getLocations("2").observe(this, new Observer<LocationStore>() {
            @Override
            public void onChanged(LocationStore locationStore) {

                Log.d("CheckTwo", "onChanged:hey "+locationStore.getLatitude());

                locationStoress.add(locationStore);
            }
        });

    }


    @SuppressLint("SetTextI18n")
    private void calculateDistance(List<LocationStore> locationStore){
        final Location location = new Location("");
        location.setLatitude(locationStore.get(0).getLatitude());
        location.setLongitude(locationStore.get(0).getLongitude());

        mLocationViewModel.getLocations("2").observe(this, new Observer<LocationStore>() {
            @Override
            public void onChanged(LocationStore locationStore) {

                try {
                    Log.d("CheckTwo", "onChanged:hey " + locationStore.getLatitude());
                    //locationStoress.add(locationStore);
                    Location locationTwo = new Location("");
                    locationTwo.setLatitude(locationStore.getLatitude());
                    locationTwo.setLongitude(locationStore.getLongitude());

                    float distance = location.distanceTo(locationTwo);

                    mTextViewDisplayTwo.setText("You stopped moving at Latitude = " + locationStore.getLatitude()
                            + " Longitude = " + locationStore.getLongitude());
                    mTextViewThree.setText("distant moved = " + distance + " meters");

                }catch (NullPointerException got){
                    //locationStoress.add(locationStore);

                    Location locationTwo = new Location("");
                    locationTwo.setLatitude(locationStoress.get(1).latitude);
                    locationTwo.setLongitude(locationStoress.get(1).longitude);

                    float distance = location.distanceTo(locationTwo);

                    mTextViewDisplayTwo.setText("You stopped moving at Latitude = " + locationTwo.getLatitude()
                            + " Longitude = " + locationTwo.getLongitude());
                    mTextViewThree.setText("distant moved = " + distance + " meters");


                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.start:
                useTrackerSerice();
                mProgressDialog.show();
                mStopButton.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.GONE);
                break;
            case R.id.stop:
                stopTracking();
                break;

        }
    }

    public void DismissDialog(){
        mProgressDialog.setMessage("Calculating For precision , Please Wait for 10 second before moving");
        try{

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        mProgressDialog.dismiss();
                        mTextViewMove.setVisibility(View.VISIBLE);
                    }catch (IllegalArgumentException got){
                        dissmiss();
                    }
                }
            },10000);
        }catch (IllegalArgumentException got){
            mProgressDialog.dismiss();
            mTextViewMove.setVisibility(View.VISIBLE);
        }

    }

    private void dissmiss(){

        mProgressDialog.dismiss();
        mTextViewMove.setVisibility(View.VISIBLE);
    }
}