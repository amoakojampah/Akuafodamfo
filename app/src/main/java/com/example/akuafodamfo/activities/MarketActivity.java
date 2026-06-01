package com.example.akuafodamfo.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.akuafodamfo.R;
import com.example.akuafodamfo.adapters.MarketPriceAdapter;
import com.example.akuafodamfo.models.MarketPrice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarketActivity extends AppCompatActivity {
    private Spinner spinnerCropType;
    private RecyclerView rvMarketPrices;
    private TextView tvLastUpdated;
    private Button btnRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MarketPriceAdapter adapter;
    private List<MarketPrice> marketPrices = new ArrayList<>();
    private List<MarketPrice> filteredPrices = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        initializeViews();
        setupSpinner();
        setupRecyclerView();
        setupClickListeners();
        loadMarketData();
    }

    private void initializeViews() {
        spinnerCropType = findViewById(R.id.spinnerCropType);
        rvMarketPrices = findViewById(R.id.rvMarketPrices);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        btnRefresh = findViewById(R.id.btnRefresh);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSpinner() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.crop_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCropType.setAdapter(adapter);

            spinnerCropType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCrop = position == 0 ? null : parent.getItemAtPosition(position).toString();
                    filterByCropType(selectedCrop);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    filterByCropType(null);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up crop filter", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new MarketPriceAdapter(filteredPrices);
        rvMarketPrices.setLayoutManager(new LinearLayoutManager(this));
        rvMarketPrices.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> {
            btnRefresh.setEnabled(false);
            loadMarketData();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMarketData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadMarketData() {
        // Show loading state
        tvLastUpdated.setText(R.string.loading_prices);

        // Simulate network request delay
        rvMarketPrices.postDelayed(() -> {
            try {
                // Clear existing data
                marketPrices.clear();

                // Add mock data - in real app, this would come from API/database
                marketPrices.add(new MarketPrice("Maize", "Kumasi", 250.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Maize", "Accra", 280.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Cassava", "Kumasi", 150.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Cassava", "Tamale", 120.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Rice", "Kumasi", 350.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Rice", "Accra", 380.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Yam", "Kumasi", 200.0, "GHC", new Date()));
                marketPrices.add(new MarketPrice("Yam", "Tamale", 180.0, "GHC", new Date()));

                // Apply current filter
                String selectedCrop = spinnerCropType.getSelectedItemPosition() == 0 ?
                        null : spinnerCropType.getSelectedItem().toString();
                filterByCropType(selectedCrop);

                // Update last updated time
                tvLastUpdated.setText(getString(R.string.last_updated, dateFormat.format(new Date())));

            } catch (Exception e) {
                Toast.makeText(this, "Error loading market data", Toast.LENGTH_SHORT).show();
                tvLastUpdated.setText(R.string.loading_error);
            } finally {
                btnRefresh.setEnabled(true);
            }
        }, 1500); // 1.5 second delay for simulation
    }

    private void filterByCropType(String cropType) {
        filteredPrices.clear();

        if (cropType == null || cropType.equals(getString(R.string.all_crops))) {
            filteredPrices.addAll(marketPrices);
        } else {
            for (MarketPrice price : marketPrices) {
                if (price.getCropType().equalsIgnoreCase(cropType)) {
                    filteredPrices.add(price);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredPrices.isEmpty()) {
            Toast.makeText(this, "No prices found for " + cropType, Toast.LENGTH_SHORT).show();
        }
    }
}