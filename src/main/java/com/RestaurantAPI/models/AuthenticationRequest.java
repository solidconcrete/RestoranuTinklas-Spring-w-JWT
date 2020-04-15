package com.RestaurantAPI.models;

import java.io.Serializable;
//dunno if need to implement Serializable
public class AuthenticationRequest implements Serializable {

    private String username;
    private String password;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(String username, String password) {
//        this.username = username;
//        this.password = password;
        this.setUsername(username);
        this.setPassword(password);
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserName() { return username; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
