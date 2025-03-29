package com.ordering.blinkit_clone.ui.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Branch  implements Serializable {
    public UserLiveLocation location;
    public String _id;
    public String name;
    public String address;
    public ArrayList<String> deliveryPartners;
    public double __v;
}
