package com.RestaurantAPI.mongoActions;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.json.simple.JSONObject;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Updates.set;

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

    public static ArrayList<JSONObject> getRestaurantDishes(String restaurantAddress)
    {
        ArrayList<JSONObject> dishes= new ArrayList<>();
        MongoCollection restaurantCollection = MongoActions.getCollection("restaurants");
        MongoCollection dishCollection = MongoActions.getCollection("dishes");
        Document restaurantDoc = (Document) restaurantCollection.find(new Document("Address", restaurantAddress)).first();
        Document dishDoc;
        BasicDBObject query = new BasicDBObject();
        List<String > allDishes = (List<String>) restaurantDoc.get("Dishes");
        for (String id : allDishes)
        {
            JSONObject dish = new JSONObject();
            query.put("_id", new ObjectId(id));
            dishDoc =(Document) dishCollection.find(query).first();
            dish.put("name", (String) dishDoc.get("Dish_name"));
            dish.put("img_url", (String) dishDoc.get("Image_link"));
            dish.put("price", (Double) dishDoc.get("Price"));
            dish.put("id", id);
            dishes.add(dish);
//            dishNames.add((String) dishDoc.get("Dish_name"));
        }
        return dishes;
    }

    public static String getChainByRestaurantAddress (String restaurantAddress)
    {
        System.out.println("Got address: " + restaurantAddress);
        MongoCollection<Document> collection = MongoActions.getCollection("restaurants");
        Document restaurantDoc = collection.find(new Document("Address", restaurantAddress)).first();
        return (String) restaurantDoc.get("Restaurant_chain");
    }

    public static String getAddressOrChain(String Email)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();

        String duty = (String) workerDoc.get("Duty");
        if (duty.equals("Restaurant_chain_manager"))
        {
            System.out.println("MANAGER DETECTED");
            return (String) workerDoc.get("Managed_restaurant_chain");
        }
        else
        {
            return (String) workerDoc.get("Managed_restaurant");
        }
    }

    public static String getLogoByChainName(String chainName)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("restaurant_chains");
        Document chainDoc = collection.find(new Document("Restaurant_chain_name", chainName)).first();
        String imgUrl = (String) chainDoc.get("Chain_logo_link");
        return imgUrl;

    }

    public static String getChainLogoByEmail(String Email)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();
        String duty = (String) workerDoc.get("Duty");
        if (duty.equals("Restaurant_chain_manager"))
        {
            System.out.println("MANAGER DETECTED");
            return getLogoByChainName(workerDoc.getString("Managed_restaurant_chain"));
        }
        else
        {
            String chainName = getChainByRestaurantAddress(workerDoc.getString("Managed_restaurant"));
            System.out.println("Chain name : " + chainName);
            return getLogoByChainName(chainName);
        }
    }

    public static String getWorkerDuty(String Email)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();
        String duty = (String) workerDoc.get("Duty");
        return duty;
    }

    public static ArrayList<JSONObject> getRestaurantAdmin(String restaurantAddress)
    {
        ArrayList<JSONObject> workersData = new ArrayList<>();
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        MongoCursor<Document> workers = collection.find(new Document("Managed_restaurant", restaurantAddress)).iterator();
        while (workers.hasNext())
        {
            JSONObject workerData = new JSONObject();
            Document worker = workers.next();
            workerData.put("id", worker.getObjectId("_id").toString());
            workerData.put("name", worker.getString("Name"));
            workerData.put("surname", worker.getString("Surname"));
            workerData.put("email", worker.getString("Email"));
            workersData.add(workerData);
        }
        return workersData;
    }

    public static ArrayList<JSONObject> getChainDishes(String Email)
    {
        ArrayList<JSONObject> chainDishes = new ArrayList<>();
        MongoCollection dishesCollection = MongoActions.getCollection("dishes");;
        String chainName = getManagedRestaurantChain(Email);
        MongoCursor<Document> dishDedails = dishesCollection.find(new Document("Restaurant_chain", chainName)).iterator();

            while (dishDedails.hasNext())
            {
                Document tempDish = dishDedails.next();
                JSONObject dish = new JSONObject();

                dish.put("name", (String) tempDish.get("Dish_name"));
                dish.put("img_url", (String) tempDish.get("Image_link"));
                dish.put("price", (Double) tempDish.get("Price"));
                dish.put("id", tempDish.getObjectId("_id").toString());
                chainDishes.add(dish);
            }
            return chainDishes;
    }


    public static String getManagedRestaurantChain(String Email)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();
        String chainName = (String) workerDoc.get("Managed_restaurant_chain");
        return chainName;
    }

    public static JSONObject getWorkerData(String Email)
    {
        MongoCollection<Document> collection = MongoActions.getCollection("workers");
        Document workerDoc = collection.find(new Document("Email", Email)).first();
        JSONObject workerData = new JSONObject();
        workerData.put("id",workerDoc.getObjectId("_id").toString());
        workerData.put("name", workerDoc.getString("Name"));
        workerData.put("surname", workerDoc.getString("Surname"));
        workerData.put("email", workerDoc.getString("Email"));
        return workerData;
    }

    public static Boolean changeDishPrice (String id, double newPrice)
    {
        MongoCollection<Document> dishCollection = MongoActions.getCollection("dishes");

        Bson filter = eq("_id", new ObjectId(id));
        Bson updateOperation = set("Price", newPrice);
        UpdateResult result = dishCollection.updateOne(filter, updateOperation);
        if (result.getModifiedCount() != 1)
        {
            return false;
        }
        return true;
    }
}
