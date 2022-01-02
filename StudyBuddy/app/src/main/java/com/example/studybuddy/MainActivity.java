package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.GetUserValues;
import com.recombee.api_client.api_requests.ListUserPurchases;
import com.recombee.api_client.api_requests.ListUsers;
import com.recombee.api_client.bindings.Purchase;
import com.recombee.api_client.bindings.User;
import com.recombee.api_client.exceptions.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9999;
    private TextView txtViewTellUserToLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleSignInAccount userAccount;
    private static final RecombeeClient2 client = UniversalObjects.CLIENT;

    private class AsyncCheckNewAccount extends AsyncTask<String, Void, User> {
        @Override
        /**
         * this method runs in the background upon execution of the AsyncTask object.
         * it takes as input a string for the user's id and checks it against all the user ids from the Recombee User table
         * rather than returning the result as a boolean, this method will send the user to the AccountCreation activity
         *  if the user account is not yet registered, and will send the user to the app's home activity if the user account
         *   is already registered
         */
        protected User doInBackground(String... ids) {
            String curUserId = ids[0];
            User[] userList = null;
            try {
                userList = client.send(new ListUsers().setReturnProperties(true));
            } catch (ApiException e) {
                e.printStackTrace();
            }
            if (userList == null) return null;
            User resultingUser = null;
            for (User u: userList) {
                if (u.getUserId().equals(curUserId)) {
                    resultingUser = u;
                    Map<String, Object> storedUserVals = null;
                    try {
                        storedUserVals = client.send(new GetUserValues(curUserId));
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                    int classStandingAsNumber = (Integer) storedUserVals.get("classStanding");
                    String classStanding = UniversalObjects.numToClassStanding.get(classStandingAsNumber);
                    String major = (String) storedUserVals.get("major");
                    String college = (String) storedUserVals.get("college");

                    Purchase[] curUserPurchases = null;
                    try {
                         curUserPurchases = client.send(new ListUserPurchases(curUserId));
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                    ArrayList<String> curUserCourses = new ArrayList<String>();
                    for (Purchase p: curUserPurchases) {
                        curUserCourses.add(p.getItemId().replace("_", " "));
                    }
                    Intent goToHome = new Intent(getApplicationContext(), HomeActivity.class);
                    goToHome.putExtra(getString(R.string.user_id), curUserId);
                    goToHome.putExtra(getString(R.string.user_name), userAccount.getDisplayName());
                    goToHome.putExtra(getString(R.string.user_email), userAccount.getEmail());
                    goToHome.putExtra(getString(R.string.user_photo), userAccount.getPhotoUrl().toString());
                    goToHome.putExtra("major", major);
                    goToHome.putExtra("college", college);
                    goToHome.putExtra("class year", classStanding);
                    goToHome.putExtra("courses", curUserCourses);
                    startActivity(goToHome);

                }
            }
            if (resultingUser == null) {
                Intent createNewAccount = new Intent(getApplicationContext(), AccountCreation.class);
                createNewAccount.putExtra(getString(R.string.user_id), curUserId);
                createNewAccount.putExtra(getString(R.string.user_name), userAccount.getDisplayName());
                createNewAccount.putExtra(getString(R.string.user_email), userAccount.getEmail());
                createNewAccount.putExtra(getString(R.string.user_photo), userAccount.getPhotoUrl().toString());
                startActivity(createNewAccount);
            }
            return resultingUser;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        //Configure the reference to our users database
        myRef = database.getReference(getString(R.string.users));
        // Configure views
        txtViewTellUserToLogin = (TextView) findViewById(R.id.txtViewTellUserToLogin);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //Configuring sign in button
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }
            }
        });
    }


    //onStart checks if the user is already logged in.
    //if they are, it takes them to the home activity.
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        try {
            updateUI(account);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //handles the result of logging in.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK && data != null) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                handleSignInResult(task);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //if onActivityResult results in a success,
    //handleSignInResult gets the account that was signed into.
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) throws IOException {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            updateUI(null);
        }
    }

    //starts the sign in intent and calls startActivityForResult on it.
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //updates the UI accordingly when the user is logged in or not
    private void updateUI(GoogleSignInAccount account) throws IOException {
        //if the user is not logged in or the log in fails
        if(account == null){
            signInButton.setClickable(true);
            signInButton.setVisibility(View.VISIBLE);
            txtViewTellUserToLogin.setVisibility(View.VISIBLE);
        }
        //if logging in is succeeds, check if they are in our database in Recombee.
        //if they are they go to the homepage. if they're not they go to accountcreation page.
        else{
            signInButton.setClickable(false);
            signInButton.setVisibility(View.GONE);
            txtViewTellUserToLogin.setVisibility(View.GONE);
            (new AsyncCheckNewAccount()).execute(account.getId());
            sendUserInfoToDatabase(account);
        }

    }

    //when the user successfully logs into our app, it send their info to our database.
    private void sendUserInfoToDatabase(GoogleSignInAccount account) throws IOException {
        userAccount = account;
        myRef.child(userAccount.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    myRef.child(userAccount.getId()).setValue(new StudentUser(userAccount.getPhotoUrl().toString(),
                            userAccount.getGivenName() + " " + userAccount.getFamilyName(),
                            userAccount.getDisplayName(), userAccount.getEmail(),
                            userAccount.getId()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}