package com.RestaurantAPI.controller;


import com.RestaurantAPI.Services.MailingService;
import com.RestaurantAPI.Services.MyUserDetailsService;
import com.RestaurantAPI.config.JwtUtil;
import com.RestaurantAPI.filters.JwtRequestFilter;
import com.RestaurantAPI.models.AuthenticationRequest;
import com.RestaurantAPI.models.AuthenticationResponse;
import com.RestaurantAPI.mongoActions.MongoActions;



import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
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


import javax.xml.ws.Response;
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
    ResponseEntity menu(@RequestHeader ("Authorization") String jwt)
    {
        String duty = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("duty");
        String chainName = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain");
        String address = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("address");
        if (duty.equals("Restaurant_manager"))
        {
            return ResponseEntity.ok(MongoActions.getRestaurantDishes(address));
        }
        else
            if ((duty.equals("Restaurant_chain_manager")))
            {
                return ResponseEntity.ok(MongoActions.getChainDishes(chainName));
            }
        System.out.println(duty + " " + chainName + " " + address);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong");
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
        JSONObject headerData = new JSONObject();

        if (jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("duty").equals("Restaurant_chain_manager"))
        {
            headerData.put("addressOrChain",jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain"));
        }
        else
        {
            headerData.put("addressOrChain",jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("address"));
        }
        headerData.put("logo_url", MongoActions.getChainLogoByChainName((String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain")));
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

    @PatchMapping("/changePrice")
    public ResponseEntity changePrice (@RequestBody String  dishData, @RequestHeader("Authorization") String jwt)
    {
        Object obj = JSONValue.parse(dishData);
        JSONObject jsonObject = (JSONObject) obj;

        String chainName = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain");


        String dishName =  (String) jsonObject.get("name");
        double newPrice = Double.parseDouble((String) jsonObject.get("price"));

        String text = Double.toString(Math.abs(newPrice));
        int integerPlaces = text.indexOf('.');
        int decimalPlaces = text.length() - integerPlaces - 1;
        if (decimalPlaces > 2)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("price should contain the maximum of" +
                    "2 digits in decimal");
        }

        boolean patchResponse = MongoActions.changeDishPrice(dishName, chainName, newPrice);
        if (patchResponse == true)
        {
            return ResponseEntity.ok(patchResponse);
        }
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong");
    }

    @PutMapping("/addDish")
    public ResponseEntity putDish (@RequestBody String data, @RequestHeader("Authorization") String jwt)
    {
        Object obj = JSONValue.parse(data);
        JSONObject jsonObject = (JSONObject) obj;

        String chainName = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain");

        String dishName = (String) jsonObject.get("name");
        String dishPrice = (String) jsonObject.get("price");
        String imgUrl = (String) jsonObject.get("img_url");
        String ingredientsString = (String) jsonObject.get("ingredients");

        String [] ingredientsArray = ingredientsString.split(",");

        MongoActions.addDish(dishName, dishPrice, imgUrl, ingredientsArray, chainName);

        return ResponseEntity.ok("Dish inserted");

    }

    @PatchMapping ("changePassword")
    public ResponseEntity changePassword (@RequestBody String passwords, @RequestHeader("Authorization") String jwt)
    {
        String Email = jwtTokenUtil.extractUserName(jwt.substring(7));
        String password = MongoActions.getUserPassword(Email);

        Object obj = JSONValue.parse(passwords);
        JSONObject jsonObject = (JSONObject) obj;
        String oldPassword = (String) jsonObject.get("oldPassword");
        String newPassword = (String) jsonObject.get("newPassword");

        if (!oldPassword.equals(password))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect current password");
        }
        else
        {
            if (MongoActions.changeUserPassword(Email, newPassword) == true)
            {
                return ResponseEntity.ok().body("Password changed successfully");
            }
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }

    }

    @GetMapping ("/sendMail")
    public ResponseEntity sendMail()
    {
        MailingService.sendMessage("saniasania523@gmail.com");
        return ResponseEntity.ok("yes");
    }

    @PutMapping("/addRestaurant")
    public ResponseEntity addRestaurant (@RequestBody String data, @RequestHeader("Authorization") String jwt)
    {
        Object obj = JSONValue.parse(data);
        JSONObject jsonObject = (JSONObject) obj;

        String chainName = (String) jwtTokenUtil.extractAllClaims(jwt.substring(7)).get("chain");
        String restaurantAddress = (String) jsonObject.get("restaurantAddress");

        MongoActions.addRestaurant(chainName, restaurantAddress);
        return ResponseEntity.ok("Restaurant inserted");

    }

}

