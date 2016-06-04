package com.udacity.firebase.shoppinglistplusplus.model;

/**
 * Created by Prit on 29-05-2016.
 */
public class Item {
    String itemName;
    String owner;
    String boughtBy;
    Boolean bought;

    public Item() {
    }

    public Item(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        this.bought=false;
        this.boughtBy=null;
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }

    public String getBoughtBy() {
        return boughtBy;
    }

    public Boolean getBought() {
        return bought;
    }

    public void setBoughtBy(String boughtBy) {
        this.boughtBy = boughtBy;
    }

    public void setBought(Boolean bought) {
        this.bought = bought;
    }
}
