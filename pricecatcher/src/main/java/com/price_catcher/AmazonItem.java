package com.price_catcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AmazonItem extends CustomItem {
    public AmazonItem(String urlString) throws Exception {
        super(urlString);
        if (!this.website.equals("www.amazon.in")) {
            throw new Exception("Invalid URL: " + urlString);
        }
    }

    @Override
    double fetchPrice() throws Exception {
        // URL of the Amazon product page
        String inputLine;
        double price = 0.0;

        try {
            // Open connection to the Amazon product page URL
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                Document document;
                boolean priceFound = false;

                // Read the page line by line
                while ((inputLine = reader.readLine()) != null) {
                    // Parse the current line
                    document = Jsoup.parse(inputLine);

                    // Look for the price parts (whole and fraction)
                    Element priceElement = document.select("span.a-price-whole").first();
                    Element priceFractionElement = document.select("span.a-price-decimal").first();
                    
                    if (priceElement != null && priceFractionElement != null) {
                        System.out.println("Price Element: " + priceElement);
                        System.out.println("Price Fraction Element: " + priceFractionElement);
                        String priceWhole = priceElement.text().replace(",", "").replace(".", "").trim();
                        String priceFraction = priceFractionElement.text().replace(",", "").replace(".", "").trim();
                        String priceString = priceWhole + priceFraction;
                        System.out.println(priceString);
                        try {
                            price = Double.parseDouble(priceString);
                            priceFound = true;
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing price: " + e.getMessage());
                        }
                    }
                }

                // If price was not found
                if (!priceFound) {
                    System.out.println("Price not found in the HTML content.");
                    return -1.0; // Return -1 if price is not found
                }

            } catch (IOException ioe) {
                System.out.println("IOException: " + ioe);
                return -1.0; // Return -1 in case of an IOException
            }
        } catch (IOException e) {
            System.out.println("Error with I/O operations: " + e);
            return -1.0; // Return -1 in case of an I/O operation failure
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e);
            return -1.0; // Return -1 for any unexpected errors
        }
        
        // Return the found price
        priceHistoryIndex = (priceHistoryIndex + 1) % priceHistory.length;
        priceHistory[priceHistoryIndex] = price;
        return price;
    }
}
