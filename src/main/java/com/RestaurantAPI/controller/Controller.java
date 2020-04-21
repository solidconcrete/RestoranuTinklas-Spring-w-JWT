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

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            FileWriter fstream = new FileWriter("requestLog.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(dtf.format(now) + "\n" + "/Authenticate \n Got: " + user + " , Decoded to: " + u +"\n");
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("Error while writing to file: " + e.getMessage());
        }

        u = u.substring(0, u.length()-1);
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
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            FileWriter fstream = new FileWriter("requestLog.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("aaaaaaaa");
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("Error while writing to file: " + e.getMessage());
        }

        String Email = jwtTokenUtil.extractUserName(jwt.substring(15, jwt.length()-2));
        ArrayList<String> addresses = MongoActions.getManagedRestaurants(Email);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/menu")
    ResponseEntity menu(@RequestHeader("RestaurantAddress") String restaurantAddress)
    {
        ArrayList<String> dishes = MongoActions.getRestaurantDishes(restaurantAddress);
        return ResponseEntity.ok(dishes);
    }

//    @GetMapping("/changeDishPrice")
//    ResponseEntity changePrice(@RequestHeader("NewDishPrice") int newPrice)
//    {
//
//    }

    @GetMapping("/requestHistory")
    ResponseEntity history()
    {
        try {
            File myObj = new File("requestLog.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNext())
            {
                String data = myReader.nextLine();
                return ResponseEntity.ok(data);
            }
            myReader.close();
        }
        catch (FileNotFoundException e )
        {
            String res = "File not found";
            return ResponseEntity.ok(res);
        }
    }

}

