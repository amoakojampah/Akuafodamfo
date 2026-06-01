package com.example.akuafodamfo.models;

import java.util.Date;

public class MarketPrice {
    private String cropType;
    private String marketLocation;
    private double price;
    private String currency;
    private Date updatedAt;

    public MarketPrice(String cropType, String marketLocation, double price, String currency, Date updatedAt) {
        this.cropType = cropType;
        this.marketLocation = marketLocation;
        this.price = price;
        this.currency = currency;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getCropType() { return cropType; }
    public String getMarketLocation() { return marketLocation; }
    public double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public Date getUpdatedAt() { return updatedAt; }
}