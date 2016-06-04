package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Item;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {
    String mListId, mEncodedEmail;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item, listId);
        bundle.putString(Constants.KEY_LIST_ID, listId);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId=getArguments().getString(Constants.KEY_LIST_ID);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEncodedEmail=sp.getString(Constants.KEY_ENCODED_EMAIL, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        String itemName=mEditTextForList.getText().toString();
        if(!itemName.equals("")){
            DatabaseReference refItem= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_ITEM_LIST).child(mListId);
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
            String itemId=refItem.push().getKey();
            String owner=mEncodedEmail;
            HashMap<String, Object> updateDatabase=new HashMap<>();
            Item item=new Item(itemName, owner);
            HashMap<String, Object> addItem=(HashMap<String, Object>)new ObjectMapper().convertValue(item, Map.class);
            updateDatabase.put("/"+Constants.FIREBASE_LOCATION_ITEM_LIST+"/"+mListId+"/"+itemId, addItem);
            HashMap<String, Object> addDate=new HashMap<>();
            addDate.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
            updateDatabase.put("/"+Constants.FIREBASE_LOCATION_ACTIVE_LIST+"/"+mListId+"/"+Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, addDate);
            ref.updateChildren(updateDatabase);
            AddListItemDialogFragment.this.getDialog().cancel();
        }
    }
}
