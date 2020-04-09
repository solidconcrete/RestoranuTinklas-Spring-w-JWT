package com.RestaurantAPI;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MainApplicationClass {
    public static void main (String[] args)
    {
        SpringApplication.run(MainApplicationClass.class, args);
    }

    public static MongoCollection getCollection(String collectionName)
    {
        String uri = "mongodb+srv://admin:TrtY2c94xzxdDrj@cluster0-ekcge.mongodb.net/test?retryWrites=true&w=majority";
        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        MongoDatabase db = mongoClient.getDatabase("restaurant_chain");
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection;
    }
}

