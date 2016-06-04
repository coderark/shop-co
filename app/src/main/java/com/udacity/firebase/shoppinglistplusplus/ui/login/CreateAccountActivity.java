package com.udacity.firebase.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String username, email, password;
    DatabaseReference mReference=FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
        Button button=(Button)findViewById(R.id.btn_create_account_final);
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onCreateAccountPressed(v);
                }
                catch (FirebaseAuthUserCollisionException e){
                    mEditTextEmailCreate.setError(e.getMessage());
                }
            }
        });
    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(final View view) throws FirebaseAuthUserCollisionException{
        username=mEditTextUsernameCreate.getText().toString();
        email=mEditTextEmailCreate.getText().toString();
        password=mEditTextPasswordCreate.getText().toString();

        if(!isEmailValid(email) || !isUserNameValid(username) || !isPasswordValid(password)){
            return;
        }
        mAuthProgressDialog.show();
        final FirebaseAuth auth=FirebaseAuth.getInstance();
        final DatabaseReference ref=mReference.child(Constants.FIREBASE_LOCATION_USER_ACCOUNTS).child(Utils.encodeEmail(email));
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                    mAuthProgressDialog.dismiss();
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()==null){
                                User user=new User(username, email);
                                ref.setValue(user);
                            }
                            else {
                                showErrorToast(getResources().getString(R.string.error_email_taken));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(LOG_TAG, databaseError.getMessage());
                        }
                    });
                    onSignInPressed(view);

                }
                else {
                    mAuthProgressDialog.dismiss();
                    Log.d(TAG, "createUserWithEmail:onFailure:" + task.getException());
                    showErrorToast(task.getException().getMessage());

                }
            }


        });
    }



    private boolean isEmailValid(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEditTextEmailCreate.setError(getResources().getString(R.string.error_invalid_email_not_valid));
            return false;
        }
        return true;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")){
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (password.equals("")){
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        else if(password.length()<6){
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_invalid_password_not_valid));
            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
