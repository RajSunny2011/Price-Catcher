package com.raj.pricecatcher.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Locator;

// import com.raj.pricecatcher.model.Item;
// import com.raj.pricecatcher.model.Website;
import com.raj.pricecatcher.model.AmazonItem;

public class PriceScraper {
    private Playwright playwright;
    private Browser browser;

    public PriceScraper() {
        // Initialize Playwright once when the application starts
        this.playwright = Playwright.create();
        
        this.browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true) 
        );
    }

    public double scrapeAmazonItem(AmazonItem item) {
        // Create a new page (tab) for this specific task
        Page page = browser.newPage();
        
        try {
            System.out.println("Navigating to: " + item.getURL());
            page.navigate(item.getURL().toString());

            Locator priceWhole = page.locator(".a-price-whole").first();
            Locator priceFraction = page.locator(".a-price-fraction").first();

            if (priceWhole.isVisible()) {
                String whole = priceWhole.innerText().replace(",", "").replace(".", "").trim();
                String fraction = "00";
                
                if (priceFraction.isVisible()) {
                    fraction = priceFraction.innerText().replace(".", "").trim();
                }
                
                return Double.parseDouble(whole + "." + fraction);
            }
            System.out.println("Could not find price element on Amazon page.");
        } catch (Exception e) {
            System.err.println("Error scraping " + item.getURL() + ": " + e.getMessage());
        }
        return -1;
    }

    // Call this when shutting down app
    public void close() {
        browser.close();
        playwright.close();
    }
}