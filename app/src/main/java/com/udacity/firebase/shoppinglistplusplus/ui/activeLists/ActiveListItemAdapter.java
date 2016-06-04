package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Item;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Created by Prit on 29-05-2016.
 */
public class ActiveListItemAdapter extends FirebaseListAdapter<Item>{

    private ShoppingList mShoppingList;
    private String mListId, mEncodedEmail;
    private DatabaseReference mUserRef;

    public ActiveListItemAdapter(Activity activity, Class<Item> modelClass, int modelLayout, Query ref, String listId, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mListId=listId;
        this.mEncodedEmail=encodedEmail;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }


    @Override
    protected void populateView(View v, Item model, int position) {

        TextView itemName=(TextView)v.findViewById(R.id.text_view_active_list_item_name);
        ImageButton remItem=(ImageButton)v.findViewById(R.id.button_remove_item);
        TextView ctext=(TextView)v.findViewById(R.id.text_view_bought_by);
        final TextView boughtBy=(TextView)v.findViewById(R.id.text_view_bought_by_user);
        itemName.setText(model.getItemName());
        final String mItemId=this.getRef(position).getKey();
        if (model.getBought()){
            itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            remItem.setVisibility(View.INVISIBLE);
            ctext.setVisibility(View.VISIBLE);
            boughtBy.setVisibility(View.VISIBLE);
            if (mEncodedEmail.equals(model.getBoughtBy())){
                boughtBy.setText("You");
            }
            else {
                mUserRef= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USER_ACCOUNTS).child(model.getBoughtBy());
                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);
                        boughtBy.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("AdapterItem", databaseError.getMessage());
                    }
                });
            }

        }
        else {
            itemName.setPaintFlags(itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            if (mEncodedEmail.equals(mShoppingList.getOwner()) || mEncodedEmail.equals(model.getOwner())){
                remItem.setVisibility(View.VISIBLE);
            }
            else {
                remItem.setVisibility(View.INVISIBLE);
            }
            ctext.setVisibility(View.INVISIBLE);
            boughtBy.setVisibility(View.INVISIBLE);
        }



        remItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemDialog(mItemId);
            }
        });
    }

    private void removeItem(String mItemId){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> remFromDatabase=new HashMap<>();
        remFromDatabase.put("/"+ Constants.FIREBASE_LOCATION_ITEM_LIST+"/"+mListId+"/"+mItemId, null);
        HashMap<String, Object> timestampChanged=new HashMap<>();
        timestampChanged.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        remFromDatabase.put("/"+Constants.FIREBASE_LOCATION_ACTIVE_LIST+"/"+mListId+"/"+Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timestampChanged);
        ref.updateChildren(remFromDatabase);
    }

    private void removeItemDialog(final String itemId){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                .setTitle(mActivity.getString(R.string.remove_item_option))
                .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(itemId);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                                /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
