package com.RestaurantAPI.mongoActions;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MongoActions {
    private static MongoClient mongoClient;


    public static MongoCollection getCollection(String collectionName)
    {
        String uri = "mongodb+srv://admin:TrtY2c94xzxdDrj@cluster0-ekcge.mongodb.net/test?retryWrites=true&w=majority";
        mongoClient = new MongoClient(new MongoClientURI(uri));
        MongoDatabase db = mongoClient.getDatabase("restaurant_chain");
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection;
    }

    public static ArrayList<JSONObject> getManagedRestaurants(String Email)
    {
        ArrayList<JSONObject> addresses = new ArrayList<>();
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();
        String duty = (String) workerDoc.get("Duty");
        if (duty.equals("Restaurant_chain_manager"))
        {
            System.out.println("MANAGER DETECTED");
            addresses = getRestaurantsFromChainName(workerDoc.getString("Managed_restaurant_chain"));
        }
        else
        {
            System.out.println("WORKER DETECTED");
            JSONObject restaurantJson = new JSONObject();
            restaurantJson.put("id", 1);
            restaurantJson.put("address", workerDoc.getString("Managed_restaurant"));
            System.out.println(restaurantJson);
            addresses.add (restaurantJson);
        }
        mongoClient.close();
        return addresses;
    }

    public static ArrayList<JSONObject> getRestaurantsFromChainName(String chainName)
    {
        ArrayList<JSONObject> addresses = new ArrayList<>();
        int i = 0;
        MongoCollection restaurantsCollection = MongoActions.getCollection("restaurants");
        MongoCursor<Document> restaurants = restaurantsCollection.find().iterator();
        try {
            while (restaurants.hasNext())
            {
                Document restaurant = restaurants.next();
                if (((String) restaurant.get("Restaurant_chain")).equals(chainName))
                {
                    JSONObject restaurantJson = new JSONObject();
                    String id = restaurant.getObjectId("_id").toString();
                    restaurantJson.put("_id", id);
                    restaurantJson.put("address", (String) restaurant.get("Address"));
                    System.out.println(restaurantJson);

                    addresses.add(restaurantJson);
//                    addresses.add((String) restaurant.get("Address"));
                    i++;
                }
            }
            restaurants.close();
        }
        finally {
            restaurants.close();
        }
        return addresses;
    }

    public static ArrayList<String> getRestaurantDishes(String restaurantAddress)
    {
        ArrayList<String> dishNames= new ArrayList<>();
        MongoCollection restaurantCollection = MongoActions.getCollection("restaurants");
        MongoCollection dishCollection = MongoActions.getCollection("dishes");
        Document restaurantDoc = (Document) restaurantCollection.find(new Document("Address", restaurantAddress)).first();
        Document dishDoc;
        BasicDBObject query = new BasicDBObject();
        List<String > dishes = (List<String>) restaurantDoc.get("Dishes");
        for (String id : dishes)
        {
            query.put("_id", new ObjectId(id));
            dishDoc =(Document) dishCollection.find(query).first();
            dishNames.add((String) dishDoc.get("Dish_name"));
        }
        return dishNames;
    }


}
