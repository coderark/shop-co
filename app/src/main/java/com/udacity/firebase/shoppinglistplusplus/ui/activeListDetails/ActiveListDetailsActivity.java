package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Item;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.activeLists.ActiveListItemAdapter;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private ListView mListView;
    private ShoppingList mShoppingList;
    private User mCurrentUser;
    private Item mItem;
    private ActiveListItemAdapter mFirebaseListItemAdapter;
    private String mListId, mEncodedEmail;
    private ValueEventListener mActiveListRefListener, mUserListRefListener;
    private DatabaseReference mActiveListRef, mCurrentUserRef, mUserShoppping;
    private Button mShoppingButton;
    private TextView mUserShoppingTextView;
    private Boolean mShopping=false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);
        mEncodedEmail=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_ENCODED_EMAIL, null);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
        invalidateOptionsMenu();

        /**
         * Set up click listeners for interaction.
         */
        Intent intent=getIntent();
        mListId=intent.getStringExtra(Constants.KEY_LIST_ID);
        if (mListId==null){
            finish();
            return;
        }

        mActiveListRef= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_ACTIVE_LIST).child(mListId);
        final DatabaseReference refItem=FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_ITEM_LIST).child(mListId);
        mCurrentUserRef=FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USER_ACCOUNTS).child(mEncodedEmail);
        mUserShoppping=mActiveListRef.child(Constants.FIREBASE_PROPERTY_USER_SHOPPING).child(mEncodedEmail);
        mFirebaseListItemAdapter=new ActiveListItemAdapter(this, Item.class, R.layout.single_active_list_item, refItem, mListId, mEncodedEmail);
        mListView.setAdapter(mFirebaseListItemAdapter);
        mActiveListRefListener=mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList shoppingList=dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList==null){
                    finish();
                    return;
                }
                mShoppingList=shoppingList;
                mFirebaseListItemAdapter.setShoppingList(mShoppingList);
                setTitle(mShoppingList.getListName());
                HashMap<String, User> userShopping=mShoppingList.getUserShopping();

                if(userShopping!=null && userShopping.size()!=0 && userShopping.containsKey(mEncodedEmail)){
                    mShopping = true;
                    mShoppingButton.setText(getString(R.string.button_stop_shopping));
                    mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
                }
                else {
                    mShoppingButton.setText(getString(R.string.button_start_shopping));
                    mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                    mShopping = false;
                }
                setWhosShoppingText(userShopping);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, databaseError.getMessage());
            }
        });

        mUserListRefListener=mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser=dataSnapshot.getValue(User.class);
                if (currentUser!=null){
                    mCurrentUser=currentUser;
                }
                else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, databaseError.getMessage());
            }
        });




        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                Item item=mFirebaseListItemAdapter.getItem(position);
                if(view.getId() != R.id.list_view_footer_empty) {
                    if (mEncodedEmail.equals(mShoppingList.getOwner()) && mEncodedEmail.equals(item.getOwner()) && (!mShopping) && (!item.getBought())){
                        String itemName=item.getItemName();
                        String itemId=mFirebaseListItemAdapter.getRef(position).getKey();
                        showEditListItemNameDialog(itemName, itemId);
                        return true;
                    }
                }
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId()!=R.id.list_view_footer_empty){
                    final Item item=mFirebaseListItemAdapter.getItem(position);
                    if (mShopping && (item.getBoughtBy()==null || mEncodedEmail.equals(item.getBoughtBy()))){
                        DatabaseReference reference=mFirebaseListItemAdapter.getRef(position);
                        HashMap<String, Object> updateItemStatus=new HashMap<String, Object>();
                        if (item.getBought()){
                            updateItemStatus.put("/"+Constants.FIREBASE_PROPERTY_ITEM_BOUGHT, false);
                            updateItemStatus.put("/"+Constants.FIREBASE_PROPERTY_ITEM_BOUGHTBY, null);
                        }
                        else {
                            updateItemStatus.put("/"+Constants.FIREBASE_PROPERTY_ITEM_BOUGHT, true);
                            updateItemStatus.put("/"+Constants.FIREBASE_PROPERTY_ITEM_BOUGHTBY, mEncodedEmail);
                        }
                        reference.updateChildren(updateItemStatus);
                    }
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(true);
        edit.setVisible(true);
        share.setVisible(false);
        archive.setVisible(false);

        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String user=sp.getString(Constants.KEY_ENCODED_EMAIL, null);
        if (!user.equals(mShoppingList.getOwner())){
            remove.setVisible(false);
            edit.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mShopping=false;
        mUserShoppping.removeValue();
    }

    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        mFirebaseListItemAdapter.cleanup();
        mActiveListRef.removeEventListener(mActiveListRefListener);

    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        mShoppingButton=(Button)findViewById(R.id.button_shopping);
        mUserShoppingTextView=(TextView)findViewById(R.id.text_view_people_shopping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, mListId, itemId, itemName);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {
        if (mShoppingList!=null){
            if (mShopping){
                mUserShoppping.removeValue();
            }
            else {
                mUserShoppping.setValue(mCurrentUser);
            }
        }
    }

    private void setWhosShoppingText(HashMap<String, User> usersShopping) {

        if (usersShopping != null) {
            ArrayList<String> usersWhoAreNotYou = new ArrayList<>();
            /**
             * If at least one user is shopping
             * Add userName to the list of users shopping if this user is not current user
             */
            for (User user : usersShopping.values()) {
                if (user != null && !(user.getEmail().equals(mEncodedEmail))) {
                    usersWhoAreNotYou.add(user.getName());
                }
            }

            int numberOfUsersShopping = usersShopping.size();
            String usersShoppingText;

            /**
             * If current user is shopping...
             * If current user is the only person shopping, set text to "You are shopping"
             * If current user and one user are shopping, set text "You and userName are shopping"
             * Else set text "You and N others shopping"
             */
            if (mShopping) {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = getString(R.string.text_you_are_shopping);
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_other_are_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_number_are_shopping),
                                usersWhoAreNotYou.size());
                }
                /**
                 * If current user is not shopping..
                 * If there is only one person shopping, set text to "userName is shopping"
                 * If there are two users shopping, set text "userName1 and userName2 are shopping"
                 * Else set text "userName and N others shopping"
                 */
            } else {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_is_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_other_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.get(1));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_number_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.size() - 1);
                }
            }
            mUserShoppingTextView.setText(usersShoppingText);
        } else {
            mUserShoppingTextView.setText("");
        }
    }
}
