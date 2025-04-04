package com.price_catcher;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;

abstract public class CustomItem implements Serializable {
    public URL url;
    public String website;
    double[] priceHistory = new double[10];
    int priceHistoryIndex = 0;
    double thresholdPrice = 0.0;
    public CustomItem(String urlString) throws Exception{
        this.url = new URI(urlString).toURL();
        if (this.url.getProtocol() == null || this.url.getHost() == null) {
            throw new Exception("Invalid URL: " + urlString);
        }
        this.website = url.getHost();
    }
    public String getURL() {
        return url.toString();
    }
    public double getPrice(){
        return priceHistory[priceHistoryIndex];
    }
    public double[] getPriceHistory() {
        return priceHistory;
    }
    public void setThresholdPrice(double thresholdPrice) {
        if (thresholdPrice < 0){
            this.thresholdPrice = 0;
            return;
        }
        this.thresholdPrice = thresholdPrice;
    }
    public double getThresholdPrice() {
        return thresholdPrice;
    }
    abstract double fetchPrice() throws Exception;
}
