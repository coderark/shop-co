package com.udacity.firebase.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.MainActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

;

/**
 * Represents Sign in screen and functionality of the app
 */
public class LoginActivity extends BaseActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;
    private String email, password, mEncodedEmail, username;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference mReference= FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USER_ACCOUNTS);
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    /**
     * Variables related to Google Login
     */
    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;
    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;
    /* A Google account object that is populated if the user signs in with Google */
    GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Link layout elements from XML and setup progress dialog
         */
        initializeScreen();

        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    try {
                        signInPassword();
                    } catch (FirebaseAuthException e) {
                        showErrorToast(e.getMessage());
                    } catch (FirebaseNetworkException e) {
                        showErrorToast(e.getMessage());
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuthProgressDialog.dismiss();
                if (firebaseAuth.getCurrentUser()!=null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        auth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    public void onPause() {
        super.onPause();
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
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        try {
            signInPassword();
        } catch (FirebaseAuthException e) {
            showErrorToast(e.getMessage());
        } catch (FirebaseNetworkException e) {
            showErrorToast(e.getMessage());
        }
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
    }

    public class MyOnCompleteListener implements OnCompleteListener<AuthResult>{
        public MyOnCompleteListener() {
        }

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                email=task.getResult().getUser().getEmail();
                mEncodedEmail=Utils.encodeEmail(email);
                username=task.getResult().getUser().getDisplayName();
                SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor spe=sp.edit();
                if (task.getResult().getUser().getProviders().get(0).equals(GoogleAuthProvider.PROVIDER_ID)){
                    Log.d(LOG_TAG, "Hello");
                    final DatabaseReference ref=mReference.child(mEncodedEmail);
                    User user=new User(username, email);
                    ref.setValue(user);
//                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Log.d(LOG_TAG, "Hello");
//                            if (dataSnapshot.getValue()==null){
//                                Log.d(LOG_TAG, "World");
//                                User user=new User(username, email);
//                                ref.setValue(user);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.e(LOG_TAG, databaseError.getMessage());
//                        }
//                    });
                }
                else if (task.getResult().getUser().getProviders().get(0).equals(EmailAuthProvider.PROVIDER_ID)){

                }
                spe.putString(Constants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
                spe.putString(Constants.KEY_PROVIDER, task.getResult().getUser().getProviders().get(0)).apply();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            else {
                showErrorToast(task.getException().getMessage());
            }
        }

    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() throws FirebaseAuthException, FirebaseNetworkException {
        email = mEditTextEmailInput.getText().toString();
        password = mEditTextPasswordInput.getText().toString();
        if (email.equals("")) {
            mEditTextEmailInput.setError(getResources().getString(R.string.error_cannot_be_empty));
            return;
        }
        if (password.equals("")) {
            mEditTextPasswordInput.setError(getResources().getString(R.string.error_cannot_be_empty));
            return;
        }
        mAuthProgressDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new MyOnCompleteListener());
        mAuthProgressDialog.dismiss();

    }


    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     *
     *
     */
    private void setAuthenticatedUserPassword(Task<AuthResult> task) {

    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     *
     */
    private void setAuthenticatedUserGoogle(Task<AuthResult> task) {

    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Signs you into ShoppingList++ using the Google Login Provider
     *
     * @param token A Google OAuth access token returned from Google
     */
    private void loginWithGoogle(String token) throws FirebaseAuthException, FirebaseNetworkException {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        auth.signInWithCredential(credential).addOnCompleteListener(new MyOnCompleteListener());
        mAuthProgressDialog.dismiss();
    }

    /**
     * GOOGLE SIGN IN CODE
     * <p/>
     * This code is mostly boiler plate from
     * https://developers.google.com/identity/sign-in/android/start-integrating
     * and
     * https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java
     * <p/>
     * The big picture steps are:
     * 1. User clicks the sign in with Google button
     * 2. An intent is started for sign in.
     * - If the connection fails it is caught in the onConnectionFailed callback
     * - If it finishes, onActivityResult is called with the correct request code.
     * 3. If the sign in was successful, set the mGoogleAccount to the current account and
     * then call get GoogleOAuthTokenAndLogin
     * 4. getGoogleOAuthTokenAndLogin launches an AsyncTask to get an OAuth2 token from Google.
     * 5. Once this token is retrieved it is available to you in the onPostExecute method of
     * the AsyncTask. **This is the token required by Firebase**
     */


    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
    private void setupGoogleSignIn() {
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_with_google);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInGooglePressed(v);
            }
        });
    }

    /**
     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
     */
    public void onSignInGooglePressed(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
        mAuthProgressDialog.show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
        mAuthProgressDialog.dismiss();
        showErrorToast(result.toString());
        Log.d(TAG, result.toString());
    }


    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            /* Signed in successfully, get the OAuth token */
            mGoogleAccount = result.getSignInAccount();
            try {
                loginWithGoogle(mGoogleAccount.getIdToken());
            } catch (FirebaseAuthException e) {
                showErrorToast(e.getMessage());
                Log.d(TAG, e.getMessage());
            } catch (FirebaseNetworkException e) {
                showErrorToast(e.getMessage());
                Log.d(TAG, e.getMessage());
            }


        } else {
            if (result.getStatus().getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showErrorToast("The sign in was cancelled. Make sure you're connected to the internet and try again.");
            } else {
                showErrorToast("Error handling the sign in: " + result.getStatus().getStatusMessage());
            }
            mAuthProgressDialog.dismiss();
        }
    }

}