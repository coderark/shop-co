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
 * Lets user edit the list name for all copies of the current list
 */
public class EditListNameDialogFragment extends EditListDialogFragment {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    String mListName;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListNameDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        EditListNameDialogFragment editListNameDialogFragment = new EditListNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_list, listId);
        bundle.putString(Constants.KEY_LIST_NAME, shoppingList.getListName());
        editListNameDialogFragment.setArguments(bundle);
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListName=getArguments().getString(Constants.KEY_LIST_NAME);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mListName);
        return dialog;
    }

    /**
     * Changes the list name in all copies of the current list
     */
    protected void doListEdit() {
        String newListName=mEditTextForList.getText().toString();
        if (!newListName.equals("")){
            if (mListName!=null && mListId!=null){
                HashMap<String, Object> updatedList=new HashMap<>();
                updatedList.put(Constants.FIREBASE_PROPERTY_LIST_NAME, newListName);
                HashMap<String, Object> updateTimeStamp=new HashMap<>();
                updateTimeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                updatedList.put(Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, updateTimeStamp);
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_ACTIVE_LIST).child(mListId);
                ref.updateChildren(updatedList);
            }
        }
    }
}

