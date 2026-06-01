package com.example.akuafodamfo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.akuafodamfo.R;
import com.example.akuafodamfo.services.LocationService;
import com.example.akuafodamfo.utils.VoiceHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private LocationService locationService;
    private VoiceHelper voiceHelper;
    private FirebaseFirestore db;
    private TextView tvWelcome, tvFarmInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        locationService = new LocationService(this);
        voiceHelper = new VoiceHelper(this);
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvFarmInfo = findViewById(R.id.tvFarmInfo);

        Button btnPest = findViewById(R.id.btnPestDetection);
        Button btnWeather = findViewById(R.id.btnWeather);
        Button btnMarket = findViewById(R.id.btnMarket);
        Button btnVoice = findViewById(R.id.btnVoiceCommand);

        loadUserData();
        checkLocationPermission();

        btnPest.setOnClickListener(v ->
                startActivity(new Intent(this, PestDetectionActivity.class)));

        btnWeather.setOnClickListener(v ->
                startActivity(new Intent(this, WeatherActivity.class)));

        btnMarket.setOnClickListener(v ->
                startActivity(new Intent(this, MarketActivity.class)));

        btnVoice.setOnClickListener(v ->
                voiceHelper.startListening("tw"));
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String location = documentSnapshot.getString("farmLocation");
                        String cropType = documentSnapshot.getString("cropType");

                        tvWelcome.setText("Welcome, " + name);
                        tvFarmInfo.setText("Farm: " + location + "\nCrop: " + cropType);
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationService.getCurrentLocation((lat, lng) -> {
            // Location obtained, can be used for weather/market data
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}