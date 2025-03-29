package com.ordering.blinkit_clone.ui.entity;

import java.io.Serializable;
import java.security.SecureRandom;

public class LoggedUser implements Serializable {
    public String accessToken;
    public String refreshToken;
    public UserLiveLocation liveLocation;
//    public String _id;
    public String name;
    public String role;
    public boolean isActivated;
    public String phone;
    public String address;
}
