package com.raj.pricecatcher.model;

import java.io.Serializable;

public class PriceHistory implements Serializable{
    private final double[] prices;
    private int currentIndex;
    private int size;

    public PriceHistory(){
        this(10); // Default capacity of 10
    }

    public PriceHistory(int capacity) {
        this.prices = new double[capacity];
        this.currentIndex = 0;
        this.size = 0;
    }

    public void addPrice(double price) {
        prices[currentIndex] = price;
        currentIndex = (currentIndex + 1) % prices.length;
        if (size < prices.length) {
            size++;
        }
    }

    public double getCurentPrice() {
        if (size == 0) {
            return Double.NaN; // No prices recorded yet
        }
        int lastIndex = (currentIndex - 1 + prices.length) % prices.length;
        return prices[lastIndex];
    }

    public double[] getPrices() {
        double[] history = new double[size];
        for (int i = 0; i < size; i++) {
            history[i] = prices[(currentIndex - i - 1) % prices.length];
        }
        return history;
    }
}