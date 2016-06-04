package com.udacity.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Created by Prit on 31-05-2016.
 */
public class User {
    private String name;
    private String email;
    private HashMap<String, Object> timestampJoined;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        HashMap<String, Object> timestamp = new HashMap<String, Object>();
        timestamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampJoined = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }

    @Exclude
    public long getTimestampJoinedLong() {
        return (long)timestampJoined.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }
}
