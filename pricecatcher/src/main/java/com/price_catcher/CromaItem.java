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

public class CromaItem extends CustomItem {

    public CromaItem(String urlString) throws Exception {
        super(urlString);
        if (!this.website.equals("www.croma.com")) {
            throw new Exception("Invalid URL: " + urlString);
        }
    }

    // Main method to fetch the price
    @Override
    double fetchPrice() throws Exception {
        String inputLine;
        double price = 0.0;

        try {
            // Open connection to the Croma product page URL
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder htmlContent = new StringBuilder();
                
                // Read the page content into a string
                while ((inputLine = reader.readLine()) != null) {
                    htmlContent.append(inputLine);
                }
                
                // Parse the full HTML content
                Document document = Jsoup.parse(htmlContent.toString());
                
                // Find the script tag containing the JSON data
                Element scriptElement = document.select("script[type=application/ld+json]").first();
                if (scriptElement != null) {
                    // Get the JSON string from the script tag
                    String jsonData = scriptElement.html().trim();
                    try {
                        // Parse the JSON string into a JSONObject
                        JSONObject jsonObject = new JSONObject(jsonData);
                        
                        // Extract the "offers" object
                        JSONObject offers = jsonObject.getJSONObject("offers");

                        // Extract the price (assuming the price field is directly inside "offers")
                        if (offers.has("price")) {
                            String priceString = offers.getString("price").replace(",", "").trim();
                            price = Double.parseDouble(priceString);
                        }
                    } catch (NumberFormatException | JSONException e) {
                        System.out.println("Error parsing JSON with org.json: " + e.getMessage());
                    }
                }
            } catch (IOException ioe) {
                System.out.println("IOException: " + ioe);
                return -1.0;
            }
        } catch (IOException e) {
            System.out.println("Error with I/O operations: " + e);
            return -1.0;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e);
            return -1.0;
        }

        // If price was not found, return -1
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
