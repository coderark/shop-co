package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {
    String mItemId, mItemName;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList, String listId, String itemId, String itemName) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();

        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item, listId);
        bundle.putString(Constants.KEY_ITEM_ID, itemId);
        bundle.putString(Constants.FIREBASE_PROPERTY_ITEM_NAME, itemName);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemId=getArguments().getString(Constants.KEY_ITEM_ID);
        mItemName=getArguments().getString(Constants.FIREBASE_PROPERTY_ITEM_NAME);

    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mItemName);
        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        String newItemName=mEditTextForList.getText().toString();
        if (!mItemName.equals("")){
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
            HashMap<String, Object> updateDatabase=new HashMap<>();
            updateDatabase.put("/"+ Constants.FIREBASE_LOCATION_ITEM_LIST+"/"+mListId+"/"+mItemId+"/"+Constants.FIREBASE_PROPERTY_ITEM_NAME, newItemName);
            HashMap<String, Object> timeStampChanged=new HashMap<>();
            timeStampChanged.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
            updateDatabase.put("/"+Constants.FIREBASE_LOCATION_ACTIVE_LIST+"/"+mListId+"/"+Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timeStampChanged);
            ref.updateChildren(updateDatabase);
        }
    }
}
