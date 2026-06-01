package com.example.akuafodamfo.models;



public class Pest {
    private String name;
    private String scientificName;
    private String description;
    private String treatment;
    private String imageUrl;
    private float confidence;

    public Pest() {
    }

    public Pest(String name, String scientificName, String description,
                String treatment, String imageUrl, float confidence) {
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.treatment = treatment;
        this.imageUrl = imageUrl;
        this.confidence = confidence;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public String getTreatment() {
        return treatment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public float getConfidence() {
        return confidence;
    }
}
