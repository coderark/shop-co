package com.udacity.firebase.shoppinglistplusplus.utils;

import com.udacity.firebase.shoppinglistplusplus.BuildConfig;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where active lists are stored (ie "activeLists")
     */


    /**
     * Constants for Firebase object properties
     */


    /**
     * Constants for Firebase URL
     */
    public static final String FIREBASE_URL= BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_PROPERTY_TIMESTAMP="timestamp";
    public static final String FIREBASE_PROPERTY_LIST_NAME="listName";
    public static final String FIREBASE_PROPERTY_ITEM_NAME="itemName";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED="timestampLastChanged";
    public static final String FIREBASE_PROPERTY_USER_SHOPPING="userShopping";
    public static final String KEY_LAYOUT_RESOURCE = "LAYOUT_RESOURCE";
    public static final String FIREBASE_LOCATION_ACTIVE_LIST="ShoppingLists";
    public static final String FIREBASE_LOCATION_ITEM_LIST="item_list";
    public static final String FIREBASE_LOCATION_USER_ACCOUNTS="user_accounts";
    public static final String FIREBASE_URL_ACTIVE_LIST=FIREBASE_URL+"/"+FIREBASE_LOCATION_ACTIVE_LIST;
    public static final String KEY_LIST_NAME="key_list_name";
    public static final String KEY_LIST_ID="key_list_id";
    public static final String KEY_ITEM_ID="key_item_id";
    public static final String KEY_USER_NAME="name";
    public static final String FIREBASE_PROPERTY_ITEM_BOUGHTBY="boughtBy";
    public static final String FIREBASE_PROPERTY_ITEM_BOUGHT="bought";


    /**
     * Constants for bundles, extras and shared preferences keys
     *
     */
    public static final String KEY_ENCODED_EMAIL="encoded_email";
    public static final String KEY_PROVIDER="provider";

}
