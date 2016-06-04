package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {
    private String mEncodedEmail;
    private DatabaseReference mUserRef, mShoppingListRef;
    private User mUser;
    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout, Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mEncodedEmail=encodedEmail;

    }

    @Override
    protected void populateView(View v, ShoppingList model, int position) {
        String owner=model.getOwner();
        mUserRef= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USER_ACCOUNTS).child(owner);
        User user;
        TextView textViewListName=(TextView)v.findViewById(R.id.text_view_list_name);
        final TextView textViewOwner=(TextView)v.findViewById(R.id.text_view_created_by_user);
        TextView textViewUserShopping=(TextView)v.findViewById(R.id.text_view_users_shopping);
        textViewListName.setText(model.getListName());
       if (owner.equals(mEncodedEmail)){
           textViewOwner.setText("You");
       }
        else {
           mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   User user=dataSnapshot.getValue(User.class);
                   mUser=user;
                   textViewOwner.setText(user.getName());
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {
                   Log.d("Adapter", databaseError.getMessage());
               }
           });
       }
        if (model.getUserShopping()!=null){
            int nUsers=model.getUserShopping().size();
            if (nUsers==1){
                textViewUserShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.person_shopping),
                        nUsers));
            }
            else {
                textViewUserShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.people_shopping),
                        nUsers));
            }
        }
//        if (model.getTimestampLastChanged()!=null){
//            textViewDateChanged.setText(Utils.SIMPLE_DATE_FORMAT.format(new Date((long)model.getTimestampLastChanged().get(Constants.FIREBASE_PROPERTY_TIMESTAMP))));
//        }
//        else {
//            textViewDateChanged.setText("");
//        }
    }
}