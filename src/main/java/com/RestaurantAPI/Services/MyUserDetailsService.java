package com.RestaurantAPI.Services;

import com.RestaurantAPI.MainApplicationClass;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Service
public class MyUserDetailsService implements UserDetailsService {

    //must fetch user from MongoDB instead of hard-coded one
//    UsernameNotFoundException
    @Override
    public UserDetails loadUserByUsername(String userName) {
//        System.out.println("+++ " + userName + "+++");
//        return new User("foo", "foo", new ArrayList<>());

        MongoCollection<Document> collection = MainApplicationClass.getCollection("workers");
        Document worker = collection.find(new Document("Email", userName)).first();
        Object obj = null;
        try {
            obj = new JSONParser().parse(worker.toJson());
            JSONObject jo = (JSONObject)obj;
            return new User(userName, (String) jo.get("Password"), new ArrayList<>());

        } catch (ParseException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return null;
    }
}
