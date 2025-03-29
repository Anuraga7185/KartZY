package com.ordering.blinkit_clone.ui.entity;

import android.content.ClipData;

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderDetail implements Serializable {
    public UserLiveLocation deliveryLocation;
    public UserLiveLocation pickupLocation;
    public UserLiveLocation customer;
    public String _id;
    public String branch;
    public ArrayList<Items> items;
    public Map<String, Items> itemsMap = new HashMap<>();
    public String status;
    public double totalPrice;
    public String createdAt;
    public String udatedAt;
    public String orderId;
    public int __v;

    public void calcPrice() {
        totalPrice = 0;
        for (Items items1 : items) {
            totalPrice += items1.count * items1.itemPrice;
        }
    }

    public void updateItemsPrice() {
        items = new ArrayList<>();
        for (Map.Entry<String, Items> entry : itemsMap.entrySet()) {
            items.add(entry.getValue());
        }
    }

}

