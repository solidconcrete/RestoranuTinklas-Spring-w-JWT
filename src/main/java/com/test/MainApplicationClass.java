package com.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@SpringBootApplication
public class MainApplicationClass {
    public static void main (String[] args)
    {
        SpringApplication.run(MainApplicationClass.class, args);
    }
    public static ArrayList<String> getAddresses()
    {
        String uri = "mongodb+srv://admin:TrtY2c94xzxdDrj@cluster0-ekcge.mongodb.net/test?retryWrites=true&w=majority";
        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        MongoDatabase db = mongoClient.getDatabase("restaurant_chain");
        MongoCollection<Document> collection = db.getCollection("restaurants");
        MongoCursor<Document> cursor = collection.find().iterator();
        ArrayList<String> addresses = new ArrayList<String>();
        {
            try {
                while (cursor.hasNext())
                {
                    addresses.add((String) cursor.next().get("address"));
                }
            }finally {
                cursor.close();
            }
        }
        return addresses;
    }
}

@RestController
class HelloController{
    @GetMapping("/")
    String hello()
    {
        return "yo";
    }
    @GetMapping("/addresses")
    ArrayList<String > addresses()
    {
        return MainApplicationClass.getAddresses();
    }
}
