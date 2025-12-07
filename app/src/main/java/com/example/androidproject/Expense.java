package com.example.androidproject;

import java.io.Serializable;

// Serializable interface allows this object to be passed between activities
public class Expense implements Serializable {
    private String name;
    private int amount;
    private String category;

    public Expense(String name, int amount, String category) {
        this.name = name;
        this.amount = amount;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }
}
