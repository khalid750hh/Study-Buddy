package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * This is the "home" activity of the app. It appears after log in, and it is where users
 *  are presented options to go to their profiles, to find other users, to make a group chat,
 *  or to find tutoring.
 */
public class HomeActivity extends AppCompatActivity {

    // storing firebase info for the current user
    private String thisUserId;
    private String email;
    private String name;
    private String photo;

    // the image buttons users can tap on in this activity
    private ImageButton imgBtnGroups, imgBtnUsers, imgBtnTutors, imgBtnProfile;

    private TextView txtViewUsers, txtViewGroups, txtViewWelcome;

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    public void onBackPressed() {
        //override the original onBackPressed method to
        //disable the app closing the app/activity when pressing the back button.
        //Purpose: use signout menu to go back to the sign in page.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent extras = getIntent();
        thisUserId = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        imgBtnUsers = (ImageButton) findViewById(R.id.imgBtnUsers);
        imgBtnGroups = (ImageButton) findViewById(R.id.imgBtnGroups);
        imgBtnTutors = (ImageButton) findViewById(R.id.imgBtnTutors);
        imgBtnProfile = (ImageButton) findViewById(R.id.imgBtnProfile);
        txtViewWelcome = (TextView) findViewById(R.id.txtViewWelcome);

        txtViewWelcome.setText("Welcome to Study Buddy \n" + name + "!");

        imgBtnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent usersIntent = new Intent(getApplicationContext(), UserList.class);
                usersIntent.putExtra("user id", thisUserId);
                usersIntent.putExtra("user name", name);
                usersIntent.putExtra("user email", email);
                usersIntent.putExtra("user picture url", photo);
                startActivity(usersIntent);
            }
        });

        imgBtnGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("groups", "groups button clicked");
                Intent groupsIntent = new Intent(getApplicationContext(), GroupList.class);
                groupsIntent.putExtra("user id", thisUserId);
                groupsIntent.putExtra("user name", name);
                groupsIntent.putExtra("user email", email);
                groupsIntent.putExtra("user picture url", photo);
                startActivity(groupsIntent);
            }
        });

        imgBtnTutors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupsIntent = new Intent(getApplicationContext(), TutorList.class);
                groupsIntent.putExtra("user id", thisUserId);
                groupsIntent.putExtra("user name", name);
                groupsIntent.putExtra("user email", email);
                groupsIntent.putExtra("user picture url", photo);
                startActivity(groupsIntent);
            }
        });

        imgBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("profile", "about to create intent to go to profile activity");
                Bundle userDataBundle = getIntent().getExtras();
                Intent profileIntent = new Intent(getApplicationContext(), Profile.class);
                profileIntent.putExtras(userDataBundle);
                Log.i("profile", "about to go to profile activity");
                startActivity(profileIntent);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        //Unfortunately there is no full blown menu editor, eg, like the layout or strings editor in the IDE.
        //Must enter the menu items manually.
        getMenuInflater().inflate(R.menu.signoutmenu, menu);

        return true;  //we've handled it!
    }

    //Override onOptionsItemSelected(..) to handle menu clicks by the user
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.signOut) {
            mGoogleSignInClient.signOut();
            finish();
            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);  //default behavior (fall through), shouldn't really get here tho, why?

    }
}