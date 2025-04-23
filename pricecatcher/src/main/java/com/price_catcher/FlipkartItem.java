package com.price_catcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FlipkartItem extends CustomItem {

    public FlipkartItem(String urlString) throws Exception {
        super(urlString);
        if (!this.website.equals("www.flipkart.com")) {
            throw new Exception("Invalid URL: " + urlString);
        }
    }

    // Main method to fetch the price
    @Override
    double fetchPrice() throws Exception {
        String inputLine;
        double price = 0.0;
    
        try {
            // Open connection to the Flipkart product page URL
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder htmlContent = new StringBuilder();
                
                // Read the page content into a string
                while ((inputLine = reader.readLine()) != null) {
                    htmlContent.append(inputLine);
                }
                
                // Parse the full HTML content using Jsoup
                Document document = Jsoup.parse(htmlContent.toString());
                
                // Find the first script tag containing the JSON-LD data
                Element scriptElement = document.select("script[type=application/ld+json]").first();
                if (scriptElement != null) {
                    // Get the JSON string from the script tag
                    String jsonData = scriptElement.html().trim();
                    jsonData = jsonData.substring(jsonData.indexOf("{"), jsonData.lastIndexOf("}") + 1);
                    try {
                        // Parse the JSON string into a JSONObject
                        JSONObject jsonObject = new JSONObject(jsonData);
    
                        // Check if the "offers" object exists and contains a price
                        if (jsonObject.has("offers")) {
                            JSONObject offers = jsonObject.getJSONObject("offers");
                            if (offers.has("price")) {
                                // Extract the price
                                String priceString = offers.getBigInteger("price").toString();
                                price = Double.parseDouble(priceString);
                            }
                        }
                    } catch (JSONException | NumberFormatException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    System.out.println("No JSON-LD script tag found!");
                }
            } catch (IOException ioe) {
                System.out.println("IOException: " + ioe.getMessage());
                return -1.0; // Return -1 in case of I/O error
            }
        } catch (IOException e) {
            System.out.println("Error with I/O operations: " + e.getMessage());
            return -1.0; // Return -1 in case of I/O error
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return -1.0; // Return -1 for any unexpected errors
        }
    
        // If the price wasn't found, return -1
        if (price == 0.0) {
            System.out.println("Price not found in the HTML content.");
            return -1.0;
        }
    
        // Store the price and return it
        priceHistoryIndex = (priceHistoryIndex + 1) % priceHistory.length;
        priceHistory[priceHistoryIndex] = price;
        return price;
    }
}