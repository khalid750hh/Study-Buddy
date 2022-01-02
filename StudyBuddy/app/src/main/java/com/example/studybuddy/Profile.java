package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.GetUserValues;
import com.recombee.api_client.api_requests.ListUserPurchases;
import com.recombee.api_client.api_requests.ListUsers;
import com.recombee.api_client.bindings.Purchase;
import com.recombee.api_client.bindings.User;
import com.recombee.api_client.exceptions.ApiException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private String thisUserId, email, name, photo, otherUserId;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("favorites");

    private StudentUser profileUser;

    private static final RecombeeClient2 client = new RecombeeClient2("study-buddy-2-dev", "RB6SjOzJD7IJ1enLNEcWsjtkzdzr6o9z2zW84FnQqvKiLk8tSiJBMqn8553YOJnC");
    private Purchase[] classes;
    private Map<String, Object> userInfo;


    private ImageButton imgBtnProfilePic;
    private TextView txtViewName, txtViewEmail, txtViewMajor, txtViewYear, txtViewCollege, txtViewClasses;
    private Button btnChat, btnFavorite;

    private final int picHeightAndWidth = 300;
    private final int sendImageCode = 7500;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String myUrl;

    private String[] classesAsStrings;
    private String classesAsOneString;
    private String userMajor;
    private String userYear;
    private String userCollege;


    private class AsyncGetUserInfo extends AsyncTask<String, Void, User> {
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

            try {
                classes = client.send(new ListUserPurchases(curUserId));
            } catch (ApiException e) {
                e.printStackTrace();
            }

            try {
                userInfo = client.send(new GetUserValues(curUserId));
            } catch (ApiException e) {
                e.printStackTrace();
            }


            classesAsStrings = new String[classes.length];
            userCollege = (String) userInfo.get("college");
            userMajor = (String) userInfo.get("major");
            userYear = UniversalObjects.numToClassStanding.get(userInfo.get("classStanding"));

            for (int i = 0; i < classes.length; i++) {
                if(classes[i] != null) {
                    classesAsStrings[i] = classes[i].getItemId();
                }
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Since we can't modify the text views on the async thread,
                    //we have to use this method that runs our code in the main thread.
                    txtViewYear.setText("Class standing: " + userYear);
                    txtViewMajor.setText("Major: " + userMajor);
                    txtViewCollege.setText("College: " + userCollege);
                    for (int i = 0; i < classesAsStrings.length; i++) {
                        if(i == 0){
                            classesAsOneString = "Classes:\n" + classesAsStrings[i].replaceAll("_", " ") + "\n";
                        }
                        else{
                            classesAsOneString += classesAsStrings[i].replaceAll("_", " ") + "\n";
                        }
                    }
                    txtViewClasses.setText(classesAsOneString);
                }
            });
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.profile));
        Intent extras = getIntent();
        thisUserId = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));
        try {
            otherUserId = extras.getStringExtra(getString(R.string.other_user_id));
        }
        catch (Exception e){
            otherUserId = null;
        }

        //configure the views
        imgBtnProfilePic = (ImageButton) findViewById(R.id.imgBtnProfilePic);
        txtViewName = (TextView) findViewById(R.id.txtViewName);
        txtViewEmail = (TextView) findViewById(R.id.txtViewEmail);
        txtViewMajor = (TextView) findViewById(R.id.txtViewMajor);
        txtViewYear = (TextView) findViewById(R.id.txtViewYear);
        txtViewCollege = (TextView) findViewById(R.id.txtViewCollege);
        txtViewClasses = (TextView) findViewById(R.id.txtViewClasses);
        btnChat = (Button) findViewById(R.id.btnChat);
        btnFavorite = (Button) findViewById(R.id.btnFavorite);

        if (otherUserId == null){
            (new AsyncGetUserInfo()).execute(thisUserId);
        }
        else{
            (new AsyncGetUserInfo()).execute(otherUserId);
        }






        //if one of these two conditions are met, then the user is looking at their own profile.
        if (otherUserId == null || thisUserId.equals(otherUserId)){
            imgBtnProfilePic.setClickable(true);
            btnChat.setVisibility(View.GONE);
            btnFavorite.setVisibility(View.GONE);
            usersRef = usersRef.child(thisUserId);
            getSupportActionBar().setTitle(name);

            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    profileUser = snapshot.getValue(StudentUser.class);
                    txtViewName.setText(profileUser.getFull_name());
                    txtViewEmail.setText(profileUser.getE_mail());
                    Picasso.get().load(profileUser.getImage_of_user())
                            .resize(picHeightAndWidth,picHeightAndWidth)
                            .placeholder(R.drawable.addfile).into(imgBtnProfilePic);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            imgBtnProfilePic.setClickable(false);
            btnChat.setVisibility(View.VISIBLE);
            btnFavorite.setVisibility(View.VISIBLE);
            usersRef = usersRef.child(otherUserId);
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    profileUser = snapshot.getValue(StudentUser.class);
                    txtViewName.setText(profileUser.getFull_name());
                    getSupportActionBar().setTitle(profileUser.getFull_name());
                    txtViewEmail.setText(profileUser.getE_mail());
                    Picasso.get().load(profileUser.getImage_of_user())
                            .resize(picHeightAndWidth,picHeightAndWidth)
                            .placeholder(R.drawable.addfile).into(imgBtnProfilePic);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }


        //btnChat is only visible when the profile being previewed belongs to someone other than the user.
        //It takes the user to the chatroom between the this user and the user previewed.
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userEmail = profileUser.getE_mail();
                String userName = profileUser.getFull_name();
                String userId = profileUser.getId();
                String otherUser = userId;

                Intent goToChat = new Intent(getApplicationContext(), MainActivityChat.class);
                goToChat.putExtra(getString(R.string.user_id), thisUserId);
                goToChat.putExtra(getString(R.string.user_name), name);
                goToChat.putExtra(getString(R.string.user_email), email);
                goToChat.putExtra(getString(R.string.user_photo), photo);
                goToChat.putExtra(getString(R.string.chat_name), profileUser.getFull_name());


                //this code's purpose is to determine the unique id of the chat between the two users.
                //Google accounts have unique ids of length 21 as numbers (i.e. id = 1234...535 where id.length == 21)
                //Parse the two ids into int. Since the id is too long to be parsed into an int,
                //parse the 7 most significant digits first, then the second, then third.
                //Whoever has the largest id, his id comes first in the chatroom id.
                for (int x = 0; x < 3; x++) {
                    int y = x*7;
                    int compareThisId = Integer.parseInt(thisUserId.substring(y, y + 7));
                    int compareOtherId = Integer.parseInt(otherUser.substring(y, y + 7));
                    if (compareThisId > compareOtherId){
                        goToChat.putExtra(getString(R.string.chat_id), thisUserId+otherUser);
                        break;
                    }
                    else if (compareThisId < compareOtherId){
                        goToChat.putExtra(getString(R.string.chat_id), otherUser+thisUserId);
                        break;
                    }

                    else if (x == 2){
                        goToChat.putExtra(getString(R.string.chat_id), otherUser+thisUserId);
                        break;
                    }
                }

                startActivity(goToChat);


            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favRef = favRef.child(thisUserId).child(otherUserId);
                favRef.setValue(profileUser);
            }
        });

        imgBtnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otherUserId == null){
                    Intent sendImage = new Intent();
                    sendImage.setAction(Intent.ACTION_GET_CONTENT);
                    sendImage.setType(getString(R.string.image_type));
                    startActivityForResult(sendImage, sendImageCode);
                }

            }
        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if the user is looking at their own profile, show the edit profile menu button
        if(otherUserId == null){
            getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //when the user clicks on edit profile, take them to the edit profile activity
        if(id == R.id.editProfile) {
            Bundle prevBundle = getIntent().getExtras();
            Intent goToEditProfile = new Intent(getApplicationContext(), EditProfile.class);
            goToEditProfile.putExtra("classes", classesAsStrings);
            goToEditProfile.putExtra("year", userYear);
            goToEditProfile.putExtra("major", userMajor);
            goToEditProfile.putExtra("college", userCollege);
            goToEditProfile.putExtras(prevBundle);
            startActivity(goToEditProfile);
        }
        return super.onOptionsItemSelected(item);
    }


    //when the user changes their profile picture,
    //send it to firebase storage and display it using picasso
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == sendImageCode && resultCode==RESULT_OK && data != null && data.getData() != null){

            imageUri = data.getData();
            StorageReference myStorageReference = FirebaseStorage.getInstance().getReference().child("images");
            DatabaseReference imageRef = usersRef.child(getString(R.string.image_of_user));
            StorageReference filePath = myStorageReference.child(thisUserId);

            uploadTask = filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        Toast toast2 = Toast.makeText(getApplicationContext(), task.isSuccessful() + getString(R.string.file_send_fail), Toast.LENGTH_SHORT);
                        toast2.show();

                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        imageRef.setValue(myUrl);
                    }
                }
            });
        }
    }
}