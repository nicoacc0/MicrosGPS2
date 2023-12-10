package com.example.microsgps2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    public static final int REQUEST_CODE = 1;

    Button obtener, salida, btn_cerrar;

    FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    private LocationCallback locationCallback;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Spinner spinnerCiudadOrigen = rootView.findViewById(R.id.spinnerCiudadOrigen);
        Spinner spinnerCiudadDestino = rootView.findViewById(R.id.spinnerCiudadDestino);
        Spinner spinnerRecorrido = rootView.findViewById(R.id.spinnerRecorrido);

        ArrayAdapter<CharSequence> adapterLeft = ArrayAdapter.createFromResource(requireContext(), R.array.LocalidadesOrigen, R.layout.spinner_item_left);
        adapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCiudadOrigen.setAdapter(adapterLeft);
        spinnerCiudadOrigen.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapterRight = ArrayAdapter.createFromResource(requireContext(), R.array.LocalidadesDestino, R.layout.spinner_item_right);
        adapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCiudadDestino.setAdapter(adapterRight);
        spinnerCiudadDestino.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapterRecorrido = ArrayAdapter.createFromResource(requireContext(), R.array.Recorridos, R.layout.spinner_item_recorrido);
        adapterRecorrido.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecorrido.setAdapter(adapterRecorrido);
        spinnerRecorrido.setOnItemSelectedListener(this);



        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        obtener = rootView.findViewById(R.id.btnObtenerCoordenda);
        salida = rootView.findViewById(R.id.btnSalir);
        btn_cerrar = rootView.findViewById(R.id.btn_cerrar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String id = currentUser.getUid();
                mAuth.signOut();
                stopLocationUpdates();
                stopService();
                reference.child("UsuarioActivo").child(id).removeValue();
                requireActivity().finish();
                startActivity(new Intent(requireContext(), AuthActivity.class));
                stopLocationUpdates();
            }
        });

        salida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String id = currentUser.getUid();
                stopLocationUpdates();
                stopService();
                reference.child("UsuarioActivo").child(id).removeValue();
                stopLocationUpdates();
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitud = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String id = currentUser.getUid();
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", id);
                        map.put("Latitud", latitud);
                        map.put("Longitud", longitude);
                        map.put("CiudadOrigen", ciudadOrigenSeleccionada);
                        map.put("CiudadDestino", ciudadDestinoSeleccionada);
                        map.put("Recorrido", recorridoSeleccionado);

                        reference.child("UsuarioActivo").child(id).setValue(map);
                    }
                }
            }
        };

        obtener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ObtenerCoordendasActual(view);
            }
        });

        return rootView;
    }

    public void ObtenerCoordendasActual(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            startLocationForegroundService();
            Toast.makeText(getContext(),
                    "RECORRIDO INICIADO",
                    Toast.LENGTH_SHORT).show();

        }
    }

    private void startLocationForegroundService() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent serviceIntent = new Intent(requireContext(), LocationForegroundService.class);
                requireActivity().startService(serviceIntent);

                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
        } catch (Exception ex) {
            Log.e("HomeFragment", "Error starting service: " + ex.getMessage());
        }
    }

    private void stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(locationCallback);
    }

    private void stopService() {
        stopLocationUpdates();
        requireActivity().stopService(new Intent(requireContext(), LocationForegroundService.class));
    }
    private boolean userSelected = false;
    private String ciudadOrigenSeleccionada;
    private String ciudadDestinoSeleccionada;
    private String recorridoSeleccionado;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!userSelected) {
            userSelected = true;
        } else {
            String selectedOption = parent.getItemAtPosition(position).toString();

            if (parent.getId() == R.id.spinnerCiudadOrigen) {
                ciudadOrigenSeleccionada = selectedOption;
            } else if (parent.getId() == R.id.spinnerCiudadDestino) {
                ciudadDestinoSeleccionada = selectedOption;
            } else if (parent.getId() == R.id.spinnerRecorrido) {
                recorridoSeleccionado = selectedOption;
            }
        }
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
