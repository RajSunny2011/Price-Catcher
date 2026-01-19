package com.raj.pricecatcher.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;

import com.raj.pricecatcher.service.PriceScraper;

abstract public class Item implements Serializable {
    URL url;
    Website website;
    double thresholdPrice = -1.0;
    PriceHistory priceHistory = new PriceHistory();
    protected static PriceScraper sharedScraper;

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

    public URL getURL() {
        return url;
    }
    public Website getWebsite() {
        return website;
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

    protected static void ensureScraperInitialized() {
        if (sharedScraper == null) {
            System.out.println("Initializing Playwright Engine...");
            sharedScraper = new PriceScraper();
        }
    }

    public static void shutdownScraper() {
        if (sharedScraper != null) {
            System.out.println("Shutting down Playwright...");
            sharedScraper.close();
            sharedScraper = null;
        }
    }

    public abstract double fetchPrice() throws Exception;
}


