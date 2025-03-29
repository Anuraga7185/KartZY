package com.ordering.blinkit_clone.ui.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderCreateDetailResponse implements Serializable {
    public UserLiveLocation deliveryLocation;
    public UserLiveLocation pickupLocation;
    public LoggedUser customer;
    public String _id;
    public Branch branch;
    public ArrayList<Items> items;
    public Map<String, Items> itemsMap = new HashMap<>();
    public String status;
    public double totalPrice;
    public String createdAt;
    public String udatedAt;
    public String orderId;
    public int __v;

}
