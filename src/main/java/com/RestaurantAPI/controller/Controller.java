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
import org.springframework.http.HttpStatus;
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
import java.util.Map;
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
        return "hello, this is the main page. Available commands are: \n" +
                "/authenticate (POST) \n" +
                "/addresses (returns managed restaurants) \n" +
                "/menu (Send restaurantAddress and get its menu)\n" +
                "/getUserData (returns user data by getting a jwt)\n" +
                "/getHeaderData (returns restaurant address or chain name, logo url)\n" +
                "/getRestaurantAdmin (send restaurantAddress and get its manager)\n" +
                "/getChainDishes (get dishes that are in this chain\n" +
                "/getChainDishes (get dishes of all chain restaurants (Manager only)\n" +
                "/changePrice (send body with \"id\" and \"newPrice\"";

    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
//    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception
    public ResponseEntity<?> createAuthenticationToken(@RequestBody String user) throws Exception
    {
        String u = URLDecoder.decode(user, "ISO-8859-1");

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
        ArrayList<JSONObject> addresses = MongoActions.getManagedRestaurants(Email);


        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/menu")
    ResponseEntity menu(@RequestHeader("RestaurantAddress") String restaurantAddress, @RequestHeader ("Authorization") String jwt)
    {
        ArrayList<JSONObject> dishes = MongoActions.getRestaurantDishes(restaurantAddress);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/getUserData")
    ResponseEntity username(@RequestHeader("Authorization") String jwt)
    {
        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));
        JSONObject userDataJson = MongoActions.getWorkerData(Email);
        ArrayList<JSONObject> userDataArray = new ArrayList<>();
        userDataArray.add(userDataJson);
        return ResponseEntity.ok(userDataArray);

    }

    @GetMapping("/getHeaderData")
    ResponseEntity restaurantChain(@RequestHeader("Authorization") String jwt)
    {
        ArrayList<JSONObject> headerDataArrayList = new ArrayList<>();
        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));

        String addressOrChain = MongoActions.getAddressOrChain(Email);
        String logo_url = MongoActions.getChainLogoByEmail(Email);

        JSONObject headerData = new JSONObject();
        headerData.put("addressOrChain", addressOrChain);
        headerData.put("logo_url", logo_url);
        headerData.put("id", 1);
        headerDataArrayList.add(headerData);
        return ResponseEntity.ok(headerDataArrayList);
    }

    @GetMapping("/getRestaurantAdmin")
    public ResponseEntity restaurantAdmin(@RequestHeader("Authorization") String jwt,
                                        @RequestHeader("RestaurantAddress") String restaurantAddress)
    {
        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));
        if (MongoActions.getWorkerDuty(Email).equals("Restaurant_chain_manager"))
        {
            return ResponseEntity.ok(MongoActions.getRestaurantAdmin(restaurantAddress));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have permission to this request");
        }
    }

    @GetMapping("/getChainDishes")
    public ResponseEntity chainDishes(@RequestHeader("Authorization") String jwt)
    {

        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));
        if (MongoActions.getWorkerDuty(Email).equals("Restaurant_chain_manager"))
        {
            ArrayList <JSONObject> dishes = MongoActions.getChainDishes(Email);
            return ResponseEntity.ok(dishes);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have permission to this request");
        }
    }
    @PatchMapping("/changePrice")
    public ResponseEntity changePrice (@RequestBody String  dishData)
    {
        Object obj = JSONValue.parse(dishData);
        JSONObject jsonObject = (JSONObject) obj;
        String id =  (String) jsonObject.get("id");
        double newPrice = Double.parseDouble((String) jsonObject.get("newPrice"));

        String text = Double.toString(Math.abs(newPrice));
        int integerPlaces = text.indexOf('.');
        int decimalPlaces = text.length() - integerPlaces - 1;
        if (decimalPlaces > 2)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("price should contain the maximum of" +
                    "2 digits in decimal");
        }

        System.out.println("Got from request: " + id + " " + newPrice);
        return ResponseEntity.ok(MongoActions.changeDishPrice(id, newPrice));
    }


}

