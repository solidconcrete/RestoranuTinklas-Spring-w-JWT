package com.RestaurantAPI.entities;

public class RestaurantUser {
    String username;
    String duty;

    public RestaurantUser(String username, String duty)
    {
        this.username = username;
        this.duty = duty;
    }

    public String getUsername() {
        return username;
    }

    public String getDuty() {
        return duty;
    }
}
