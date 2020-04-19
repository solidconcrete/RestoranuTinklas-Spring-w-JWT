package com.RestaurantAPI.controller;

import com.RestaurantAPI.MainApplicationClass;
import com.RestaurantAPI.Services.MyUserDetailsService;
import com.RestaurantAPI.config.JwtUtil;
import com.RestaurantAPI.filters.JwtRequestFilter;
import com.RestaurantAPI.models.AuthenticationRequest;
import com.RestaurantAPI.models.AuthenticationResponse;
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


import java.net.URLDecoder;
import java.util.ArrayList;
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
                "/addresses";
    }


    @RequestMapping({"/hello"})
    public String helloPage()
    {
        return "Stay at home";
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
//    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception
    public ResponseEntity<?> createAuthenticationToken(@RequestBody String user) throws Exception
    {
        String u = URLDecoder.decode(user, "ISO-8859-1");
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
    ArrayList<String> addresses(@RequestHeader("Authorization") String jwt)
    {
        String username = jwtTokenUtil.extractUserName(jwt.substring(15, jwt.length()-2));
        MongoCollection collection = MainApplicationClass.getCollection("restaurants");
        MongoCursor<Document> cursor = collection.find().iterator();
        ArrayList<String> addresses = new ArrayList<String>();
        try {
            while (cursor.hasNext())
            {
                addresses.add((String) cursor.next().get("Address"));
            }
        }
        finally {
            cursor.close();
        }
        return addresses;
    }




    @RequestMapping (value = "login", method = RequestMethod.POST)
    @ResponseBody
    public String loginController (@RequestBody String user) throws Exception
    {
        Object obj = JSONValue.parse(user);
        JSONObject jsonObject = (JSONObject) obj;

        return (String) jsonObject.get("login");
    }
}

