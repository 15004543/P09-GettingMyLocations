package sg.edu.rp.c347.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    TextView tv;
    Button btnStart, btnStop, btnCheck;
    String folderLocation;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        btnStart = (Button) findViewById(R.id.startDetector);
        btnStop = (Button) findViewById(R.id.stopDetector);
        btnCheck = (Button) findViewById(R.id.checkRecords);

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09PS";
        File folder = new File(folderLocation);
        if (folder.exists() == false){
            boolean result = folder.mkdir();
            if (result == true){
                Log.i("File Read/Write", "Folder created");
            }
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File targetFile = new File(folderLocation, "Location.txt");

                if (targetFile.exists() == true){
                    String data = "";
                    try {
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while (line != null){
                            data += line + "\n";
                            line = br.readLine();
                        }
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        } else {
            mLocation = null;
            tv.setText("Permisson not granted to retrieve location info");
        }

        if(mLocation != null) {
            tv.setText("Last known location when this Activity started: \n" + "Latitude: " + mLocation.getLatitude() + "\nLongitude: " + mLocation.getLongitude());
        } else {
            tv.setText("Location not Detected");
        }
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

//    @Override
//    public void onLocationChanged(Location location){
//        File targetFile = new File(folderLocation, "Location.txt");
//
//        try {
//            FileWriter writer = new FileWriter(targetFile, true);
//            writer.write("Latitude: " + mLocation.getLatitude() + ", Longitude: " + mLocation.getLongitude() + "\n");
//            writer.flush();
//            writer.close();
//        } catch (Exception e) {
//            Log.i("Service", "Error");
//            e.printStackTrace();
//        }
//    }
}
