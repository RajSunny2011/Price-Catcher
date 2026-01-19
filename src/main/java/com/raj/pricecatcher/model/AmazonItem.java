package com.raj.pricecatcher.model;

public class AmazonItem extends Item {
    public AmazonItem(String urlString) throws Exception {
        super(urlString);
        if (!this.website.equals(Website.AMAZON)) {
            throw new Exception("Invalid URL: " + urlString);
        }
    }

    @Override
    public double fetchPrice() throws Exception {
        ensureScraperInitialized();
        double price = sharedScraper.scrapeAmazonItem(this);
        
        this.updatePriceHistory(price);
        return price;
    }
}
