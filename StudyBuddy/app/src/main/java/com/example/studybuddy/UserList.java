package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.DeletePurchase;
import com.recombee.api_client.api_requests.ListUserPurchases;
import com.recombee.api_client.api_requests.RecommendUsersToUser;
import com.recombee.api_client.bindings.Purchase;
import com.recombee.api_client.bindings.Recommendation;
import com.recombee.api_client.bindings.RecommendationResponse;
import com.recombee.api_client.exceptions.ApiException;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the activity that displays all the other users to the current user.
 */
public class UserList extends AppCompatActivity {

    // store as private variables the profile information for the current signed in user
    private String thisUserId;
    private String email;
    private String name;
    private String photo;

    // store a list view of all users
    private ListView listViewUsers;
    // store a firebase list adapter and a couple db references
    private FirebaseListAdapter<StudentUser> adapter;
    private DatabaseReference usersRef;
    private DatabaseReference favsRef;

    // storing a constant Recombee client
    //  (see RecombeeClient2 class for explanation on why there is a 2 after RecombeeClient)
    private static final RecombeeClient2 client = new RecombeeClient2("study-buddy-2-dev", "RB6SjOzJD7IJ1enLNEcWsjtkzdzr6o9z2zW84FnQqvKiLk8tSiJBMqn8553YOJnC");

    // store the list containing the recommendations
    private String[] recsAsStrings;
    private StudentUser[] recsAsStudentUsers;
    
    // store an adapter for the recommendations that connects the internal recommendations list
    //  to the list view of the UI
    private RecommendationAdapter recAdapter;

    /**
     * This is a class that inherits from AsyncTask. Its purpose is to execute the asynchronous task of
     *  retrieving all the recommendations properly.
     */
    private class AsyncListViewLoader extends AsyncTask<String, Void, List<StudentUser>> {

        private final ProgressDialog dialog = new ProgressDialog(UserList.this);

        // This method is called before teh asynchronous task starts executing. It displays a dialog
        //  telling the user that it is retrieving recommendations.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Retrieving best recommendations for you...");
            dialog.show();
        }

        // This method is called after the asynchronous task is finished. It dismisses the display dialog
        //  and updates the recommendation adapter to send all the proper recommendations to the list view
        //  to be displayd.
        @Override
        protected void onPostExecute(List<StudentUser> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            recAdapter.setRecommendationList(result);
            recAdapter.notifyDataSetChanged();
        }

        // This method is the bread and butter of this class. It is what runs on a different thread and
        //  attempts to fetch the recommendations for this user from the Recombee database.
        @Override
        protected List<StudentUser> doInBackground(String... strings) {

            // First, make an asynchronous call to grab recommendations from Recombee for the current user. We have
            //  currently chosen to get 10 recommendations only. We have set the recommendation behavior by making our
            //  custom "scenario" in Recombee and setting this scenario for this method call.
            try {
                RecommendationResponse recResponse = client.send(
                        new RecommendUsersToUser(strings[0], 10).setScenario("buddies").setReturnProperties(true)
                );

                // store the recommendations as strings and as StudentUser objects
                Recommendation [] recs = recResponse.getRecomms();
                recsAsStrings = new String[recs.length];
                recsAsStudentUsers = new StudentUser[recs.length];

                // convert each recommendation to a readable string format
                for (int i = 0; i < recs.length; i++) {
                    recsAsStrings[i] = recs[i].getId() + " " + recs[i].getValues().toString();

                    // this is for getting and displaying the courses that this recommended user is taking
                    Purchase[] coursesTaken = client.send(new ListUserPurchases(recs[i].getId()));
                    ArrayList<String> courses = new ArrayList<String>();
                    for (Purchase c: coursesTaken) {
                        courses.add(c.getItemId().replace('_', ' '));
                    }
                    Log.i("COURSE BEING TAKEN: ", "GAP");
                    Log.e("recslogbruh",recsAsStrings[i].toString());

                    DatabaseReference recRef = usersRef.child(recs[i].getId());
                    int recInt = i;
                    recRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StudentUser recUser = snapshot.getValue(StudentUser.class);
                            String coursesPrintStr = "";
                            for (String c: courses) {
                                coursesPrintStr += c + " ";
                            }
                            Log.i("COURSES BEING TAKEN", coursesPrintStr);

                            recUser.setCourses(courses.toArray(new String[courses.size()]));
                            recsAsStudentUsers[recInt] = recUser;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


                return Arrays.asList(recsAsStudentUsers);
            }
            catch(ApiException e) {
                e.printStackTrace();
            }

            return null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get the intent extras and configure the views
        Intent extras = getIntent();
        thisUserId = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));
        usersRef = FirebaseDatabase.getInstance().getReference(getString(R.string.users));
        favsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.favorites));
        listViewUsers = (ListView) findViewById(R.id.listViewUsers);

        recAdapter = new RecommendationAdapter(new ArrayList(), this);

        //displays all the app users. This is the default.
        try {
            displayAppUsers();
        }
        catch (Exception e){
        }

        //takes you to the profile of the selected user
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                StudentUser v = (StudentUser) adapterView.getItemAtPosition(i);

                String userEmail = v.getE_mail();
                String userName = v.getFull_name();
                String userId = v.getId();
                String otherUser = userId;

                Intent goToProfile = new Intent(getApplicationContext(), Profile.class);
                goToProfile.putExtra(getString(R.string.user_id), thisUserId);
                goToProfile.putExtra(getString(R.string.user_name), name);
                goToProfile.putExtra(getString(R.string.user_email), email);
                goToProfile.putExtra(getString(R.string.user_photo), photo);
                goToProfile.putExtra(getString(R.string.other_user_id), otherUser);

                startActivity(goToProfile);
            }
        });
    }

    //displays the list of all users
    private void displayAppUsers() {

        adapter = new FirebaseListAdapter<StudentUser>(this, StudentUser.class,
                R.layout.users, usersRef) {
            @Override
            protected void populateView(View v, StudentUser model, int position) {
                TextView userEmail = (TextView) v.findViewById(R.id.email_user);
                TextView userName = (TextView) v.findViewById(R.id.name_user);
                TextView userId = (TextView) v.findViewById(R.id.id_user);
                TextView courses = (TextView) v.findViewById(R.id.courses_user);

                ImageView userImage = (ImageView) v.findViewById(R.id.userImageInUserList);
                courses.setVisibility(View.GONE);

                Picasso.get().load(model.getImage_of_user()).placeholder(R.drawable.addfile).into(userImage);

                userEmail.setText(model.getE_mail());
                userName.setText(model.getFull_name());
                userId.setText(model.getId());
            }
        };

        listViewUsers.setAdapter(adapter);
        getSupportActionBar().setTitle("All users!");
    }

    //displays the recomended users
    private void displayAppUsersRecs() {
        (new AsyncListViewLoader()).execute(thisUserId);
        listViewUsers.setAdapter(recAdapter);
        getSupportActionBar().setTitle("Your recommendations!");
    }

    //displays your favorite users
    private void displayFavorites() {
        favsRef = favsRef.child(thisUserId);
        adapter = new FirebaseListAdapter<StudentUser>(this, StudentUser.class,
                R.layout.users, favsRef) {
            @Override
            protected void populateView(View v, StudentUser model, int position) {
                TextView userEmail = (TextView) v.findViewById(R.id.email_user);
                TextView userName = (TextView) v.findViewById(R.id.name_user);
                TextView userId = (TextView) v.findViewById(R.id.id_user);

                ImageView userImage = (ImageView) v.findViewById(R.id.userImageInUserList);

                Picasso.get().load(model.getImage_of_user()).placeholder(R.drawable.addfile).into(userImage);

                userEmail.setText(model.getE_mail());
                userName.setText(model.getFull_name());
                userId.setText(model.getId());
            }
        };

        listViewUsers.setAdapter(adapter);
        getSupportActionBar().setTitle(getString(R.string.favorites));
    }

    //inflates the menu buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        //Unfortunately there is no full blown menu editor, eg, like the layout or strings editor in the IDE.
        //Must enter the menu items manually.
        getMenuInflater().inflate(R.menu.recsandall, menu);

        return true;  //we've handled it!
    }

    //displays the list of users according to which menu button is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.allUsersBtn) {
            displayAppUsers();
            return true;
        }
        else if (id == R.id.recsBtn){
            displayAppUsersRecs();
            return true;
        }
        else if (id == R.id.favsBtn){
            displayFavorites();
            return true;
        }
        return super.onOptionsItemSelected(item);  //default behavior (fall through), shouldn't really get here tho, why?

    }




}