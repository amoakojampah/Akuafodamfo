package com.example.akuafodamfo.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.akuafodamfo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity {
    private TextView tvTemperature, tvHumidity, tvRainfall, tvRecommendation;
    private ImageView ivWeatherIcon;

    public void refreshWeather(View view) {
        new FetchWeatherTask().execute();
    }

    private int getWeatherIconResource(String condition) {
        switch (condition.toLowerCase()) {
            case "rain":
                return R.drawable.rainday;
            case "clouds":
                return R.drawable.cloudy;
            case "clear":
                return R.drawable.haze;
            default:
                return R.drawable.day;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);  // Changed from R.activity_weather to R.layout.activity_weather

        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvRainfall = findViewById(R.id.tvRainfall);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);  // Added ImageView initialization

        new FetchWeatherTask().execute();
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String apiKey = "YOUR_OPENWEATHER_API_KEY";
            String city = "Accra"; // Or get from GPS
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                    "&appid=" + apiKey + "&units=metric";

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject main = json.getJSONObject("main");

                    String weatherCondition = json.getJSONArray("weather").getJSONObject(0).getString("main");
                    int weatherIconRes = getWeatherIconResource(weatherCondition);
                    ivWeatherIcon.setImageResource(weatherIconRes);

                    double temp = main.getDouble("temp");
                    double humidity = main.getDouble("humidity");

                    tvTemperature.setText("Temperature: " + temp + "°C");
                    tvHumidity.setText("Humidity: " + humidity + "%");

                    // Simple recommendation based on weather
                    if (temp > 30 && humidity < 40) {
                        tvRecommendation.setText("Recommendation: Water crops in the evening");
                    } else if (json.has("rain")) {
                        tvRainfall.setText("Rain expected today");
                        tvRecommendation.setText("Recommendation: Delay fertilizer application");
                    } else {
                        tvRecommendation.setText("Good day for farming activities");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}