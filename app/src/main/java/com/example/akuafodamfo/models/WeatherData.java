package com.example.akuafodamfo.models;



public class WeatherData {
    private double temperature;
    private double humidity;
    private double rainfall;
    private String condition;
    private String recommendation;

    public WeatherData() {
    }

    public WeatherData(double temperature, double humidity,
                       double rainfall, String condition) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.rainfall = rainfall;
        this.condition = condition;
        generateRecommendation();
    }

    private void generateRecommendation() {
        if (rainfall > 5) {
            recommendation = "Delay fertilizer application due to expected heavy rain";
        } else if (temperature > 30 && humidity < 40) {
            recommendation = "Water crops in the evening to prevent dehydration";
        } else {
            recommendation = "Good conditions for farming activities";
        }
    }

    // Getters
    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getRainfall() {
        return rainfall;
    }

    public String getCondition() {
        return condition;
    }

    public String getRecommendation() {
        return recommendation;
    }
}