package com.raj.pricecatcher.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;

abstract public class Item implements Serializable {
    public URL url;
    public Website website;
    double thresholdPrice = -1.0;
    PriceHistory priceHistory = new PriceHistory();

    public Item(String urlString) throws Exception{
        this.url = new URI(urlString).toURL();
        if (this.url.getProtocol() == null || this.url.getHost() == null) {
            throw new Exception("Invalid URL: " + urlString);
        }
        this.website = Website.fromHost(this.url.getHost());
        if (this.website == Website.UNKNOWN) {
            throw new Exception("Unsupported website: " + this.url.getHost());
        }
    }

    public String getURL() {
        return url.toString();
    }
    public double getPrice(){
        return priceHistory.getCurentPrice();
    }
    public double[] getPriceHistory() {
        return priceHistory.getPrices();
    }
    void updatePriceHistory(double newPrice){
        priceHistory.addPrice(newPrice);
    }
    public double getThresholdPrice() {
        if (thresholdPrice < 0){
            return 0;
        }
        return thresholdPrice;
    }

    public void setThresholdPrice(double thresholdPrice) {
        this.thresholdPrice = thresholdPrice;
    }

    public abstract double fetchPrice() throws Exception;
}


