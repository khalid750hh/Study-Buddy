package com.example.studybuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TutorList extends AppCompatActivity {

    private String id;
    private String email;
    private String name;
    private String photo;

    private ListView listViewTutors;

    private FirebaseListAdapter<TutorChat> adapter;
    private DatabaseReference groupsRef;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //set the title of the activity to Tutors list
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.tutors_list));
        groupsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.tutors_reference));
        myRef = FirebaseDatabase.getInstance().getReference(getString(R.string.chat_rooms));

        //get the intent extras
        Intent extras = getIntent();
        id = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));

        listViewTutors = (ListView) findViewById(R.id.listViewTutors);

        //display the list of tutor sessions
        try {
            displayAppTutors();
        }
        catch (Exception e){

        }

        //when you click on a tutor, it checks firebase for if you have access to that session
        //if you do it takes you to that chat
        //if you don't, it takes you to the payment activity
        listViewTutors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TutorChat v = (TutorChat) adapterView.getItemAtPosition(i);

                String userEmail = v.getCreator_email();
                String chatName = v.getGroup_name();
                String chatId = v.getId();
                String price = v.getTutoring_price();

                DatabaseReference checkIfUserHasAccess = FirebaseDatabase.getInstance()
                        .getReference().child(getString(R.string.access)).child(chatId).child(id);

                checkIfUserHasAccess.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            Intent goToChat = new Intent(getApplicationContext(), MainActivityChat.class);
                            goToChat.putExtra(getString(R.string.user_id), id);
                            goToChat.putExtra(getString(R.string.chat_name), name);
                            goToChat.putExtra(getString(R.string.user_email), email);
                            goToChat.putExtra(getString(R.string.user_photo), photo);
                            goToChat.putExtra(getString(R.string.chat_id), chatId);
                            goToChat.putExtra(getString(R.string.chat_name), chatName);

                            startActivity(goToChat);
                        }
                        else{

                            Intent goToPayment = new Intent(getApplicationContext(), StripeTransaction.class);
                            goToPayment.putExtra(getString(R.string.user_id), id);
                            goToPayment.putExtra(getString(R.string.user_name), name);
                            goToPayment.putExtra(getString(R.string.user_email), email);
                            goToPayment.putExtra(getString(R.string.user_photo), photo);
                            goToPayment.putExtra(getString(R.string.chat_id), chatId);
                            goToPayment.putExtra(getString(R.string.other_user_id), v.getCreator_id());
                            goToPayment.putExtra(getString(R.string.price), (int)(Double.parseDouble(price)*100));
                            goToPayment.putExtra(getString(R.string.price_as_string), price);

                            startActivity(goToPayment);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    //displays the list of tutors using the users.xml
    private void displayAppTutors() {
        adapter = new FirebaseListAdapter<TutorChat>(this, TutorChat.class,
                R.layout.users, groupsRef) {
            @Override
            protected void populateView(View v, TutorChat model, int position) {
                TextView userEmail = (TextView) v.findViewById(R.id.email_user);
                TextView userName = (TextView) v.findViewById(R.id.name_user);
                TextView price = (TextView) v.findViewById(R.id.id_user);
                price.setVisibility(View.VISIBLE);

                userEmail.setText(model.getCreator_email());
                userName.setText(model.getGroup_name());
                price.setText("$" + model.getTutoring_price());
            }
        };
        listViewTutors.setAdapter(adapter);
    }

    //inflates the menu button that enables the user to create a new tutoring session
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        //Unfortunately there is no full blown menu editor, eg, like the layout or strings editor in the IDE.
        //Must enter the menu items manually.
        getMenuInflater().inflate(R.menu.addnewtutor, menu);

        return true;  //we've handled it!
    }

    //if the new tutor button is clicked, takes the user to CreatNewTutor activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuId = item.getItemId();

        if(menuId == R.id.newTutor){
            Intent createTutor = new Intent(getApplicationContext(), CreateNewTutor.class);
            createTutor.putExtra(getString(R.string.user_id), id);
            createTutor.putExtra(getString(R.string.user_name), name);
            createTutor.putExtra(getString(R.string.user_email), email);
            createTutor.putExtra(getString(R.string.user_photo), photo);
            startActivity(createTutor);
        }
        return super.onOptionsItemSelected(item);  //default behavior (fall through), shouldn't really get here tho, why?
    }
}