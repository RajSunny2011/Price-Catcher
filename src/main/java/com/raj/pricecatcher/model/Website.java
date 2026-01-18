package com.raj.pricecatcher.model;

public enum Website {
    AMAZON, FLIPKART, CROMA, UNKNOWN;

    public static Website fromHost(String host) {
        if (host == null) return UNKNOWN;
        
        String lowerHost = host.toLowerCase();

        if (lowerHost.contains("amazon")) {
            return AMAZON;
        } else if (lowerHost.contains("flipkart")) {
            return FLIPKART;
        } else if (lowerHost.contains("croma")) {
            return CROMA;
        }
        return UNKNOWN;
    }

    public String toString() {
        switch (this) {
            case AMAZON:
                return "Amazon";
            case FLIPKART:
                return "Flipkart";
            case CROMA:
                return "Croma";
            default:
                return "Unknown";
        }
    }
}
