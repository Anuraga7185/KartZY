package com.ordering.blinkit_clone.ui.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrderCreateRequest implements Serializable {

    public int primaryKey;
    public String branch;
    public ArrayList<Items> items;
    public Map<String, Items> itemsMap = new HashMap<>();
    public String status;
    public double totalPrice;

    public int getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public ArrayList<Items> getItems() {
        return items;
    }

    public void setItems(ArrayList<Items> items) {
        this.items = items;
    }

    public Map<String, Items> getItemsMap() {
        return itemsMap;
    }

    public void setItemsMap(Map<String, Items> itemsMap) {
        this.itemsMap = itemsMap;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

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
