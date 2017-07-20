package sg.edu.rp.c347.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    boolean started;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    String folderLocation;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service", "Created");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09PS";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(started == false){
            started = true;
            Log.i("Service", "Started");
            mGoogleApiClient.connect();
        } else {
            Log.i("Service", "Still Running");
        }
        return Service.START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service", "Exited");
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle){
        Log.i("Service", "Connection Successful");
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Log.i("Service", "Connection Fail");
            mLocation = null;
        }
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

    }

    @Override
    public void onLocationChanged(Location location){
        File targetFile = new File(folderLocation, "Location.txt");

        try {
            FileWriter writer = new FileWriter(targetFile, true);
            writer.write("Latitude: " + mLocation.getLatitude() + ", Longitude: " + mLocation.getLongitude() + "\n");
//            writer.write("Test");
////            Log.i("Service", "" + mLocation.getLatitude());
////            Log.i("Service", "" + mLocation.getLongitude());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.i("Service", "Error");
            e.printStackTrace();
        }
    }
}
