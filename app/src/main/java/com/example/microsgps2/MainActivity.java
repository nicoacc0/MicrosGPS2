package com.example.microsgps2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    EditText lat, lon, dir;
    Button obtener, salida;
    ProgressBar progressBar;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        lat = findViewById(R.id.edtlatitud);
        lon = findViewById(R.id.edtlongitud);

        obtener = findViewById(R.id.btnObtenerCoordenda);
        salida = findViewById(R.id.btnSalir);

        progressBar = findViewById(R.id.progressBar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void ObtenerCoordendasActual(View view) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            getCoordenada();
        }
    }

    private void getCoordenada() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        int latestLocationIndex = locationResult.getLocations().size() - 1;
                        double latitud = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        lat.setText(String.valueOf(latitud));
                        lon.setText(String.valueOf(longitude));

                        // Guarda las coordenadas en Firebase en tiempo real
                        //db = FirebaseDatabase.getInstance();
                        //reference = db.getReference("coordenadas");
                        //reference.child("micro1").child("lat").setValue(latitud);
                        //reference.child("micro1").child("lng").setValue(longitude);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }, Looper.myLooper());

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
    }


    public void Exit(View view) {
        stopLocationUpdates();
        this.finish();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
    }
}
