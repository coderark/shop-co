package com.udacity.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Created by Prit on 28-05-2016.
 */
public class ShoppingList {
    private String listName;
    private String owner;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, User> userShopping;

    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner) {
        this.listName = listName;
        this.owner = owner;
        HashMap<String, Object> timestampLastChangedObj = new HashMap<String, Object>();
        timestampLastChangedObj.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampLastChangedObj;
        this.timestampCreated=timestampLastChangedObj;
        this.userShopping=new HashMap<>();

    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }


    public HashMap<String, User> getUserShopping() {
        return userShopping;
    }
}