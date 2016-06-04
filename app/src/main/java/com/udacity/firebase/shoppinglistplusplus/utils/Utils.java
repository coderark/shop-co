package com.udacity.firebase.shoppinglistplusplus.utils;

import android.content.Context;

import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;

import java.text.SimpleDateFormat;

/**
 * Utility class
 */
public class Utils {
    /**
     * Format the date with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static Boolean CheckOwner(ShoppingList shoppingList, String encodedEmail){
        return shoppingList.getOwner().equals(encodedEmail);
    }

}
