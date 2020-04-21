package com.RestaurantAPI.controller;

import com.RestaurantAPI.MainApplicationClass;
import com.RestaurantAPI.Services.MyUserDetailsService;
import com.RestaurantAPI.config.JwtUtil;
import com.RestaurantAPI.filters.JwtRequestFilter;
import com.RestaurantAPI.models.AuthenticationRequest;
import com.RestaurantAPI.models.AuthenticationResponse;
import com.RestaurantAPI.mongoActions.MongoActions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;


import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

@CrossOrigin
@RestController
class TestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @GetMapping("/")
    String hello()
    {
        return "hello, this is the main page. Available commands are:" +
                "/authenticate (POST)" +
                "/addresses (GET)";

    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
//    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception
    public ResponseEntity<?> createAuthenticationToken(@RequestBody String user) throws Exception
    {
        String u = URLDecoder.decode(user, "ISO-8859-1");
        

//        u = u.substring(0, u.length()-1);
        Object obj = JSONValue.parse(u);
        JSONObject jsonObject = (JSONObject) obj;
        String username =  (String) jsonObject.get("username");
        String password =  (String) jsonObject.get("password");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(username, password);
        try
        {
            System.out.println("controller: \nusername: " + authenticationRequest.getUserName()
                    + "  pass "+ authenticationRequest.getPassword());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword())
            );
        }
        catch (BadCredentialsException e)
        {
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUserName());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @GetMapping("/addresses")
    ResponseEntity addresses(@RequestHeader("Authorization") String jwt)
    {
        System.out.println("Got from web: " + jwt);
        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));
        ArrayList<String> addresses = MongoActions.getManagedRestaurants(Email);
//        ArrayList<String> addresses = new ArrayList<>();

        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/menu")
    ResponseEntity menu(@RequestHeader("RestaurantAddress") String restaurantAddress)
    {
        ArrayList<String> dishes = MongoActions.getRestaurantDishes(restaurantAddress);
        return ResponseEntity.ok(dishes);
    }


}

