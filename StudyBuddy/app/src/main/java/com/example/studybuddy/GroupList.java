package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class GroupList extends AppCompatActivity {

    private String id;
    private String email;
    private String name;
    private String photo;

    private ListView listViewGroups;

    private ListView listViewUsers;
    private FirebaseListAdapter<GroupChat> adapter;
    private DatabaseReference groupsRef;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.group_chats_title));

        groupsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.group_chats));
        myRef = FirebaseDatabase.getInstance().getReference(getString(R.string.chat_rooms));

        //get the intent extras and configure the views
        Intent extras = getIntent();
        id = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));
        listViewGroups = (ListView) findViewById(R.id.listViewGroups);

        //displays all the groups in the database
        try {
            displayAppGroups();
        }
        catch (Exception e){

        }


        //takes the user to the selected group chat.
        listViewGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GroupChat v = (GroupChat) adapterView.getItemAtPosition(i);

                String userEmail = v.getCreator_email();
                String chatName = v.getGroup_name();
                String chatId = v.getId();

                Intent goToChat = new Intent(getApplicationContext(), MainActivityChat.class);
                goToChat.putExtra(getString(R.string.user_id), id);
                goToChat.putExtra(getString(R.string.user_name), name);
                goToChat.putExtra(getString(R.string.user_email), email);
                goToChat.putExtra(getString(R.string.user_photo), photo);
                goToChat.putExtra(getString(R.string.chat_id), chatId);
                goToChat.putExtra(getString(R.string.chat_name), chatName);

                startActivity(goToChat);
            }
        });

    }

    //displays the app groups using users.xml
    private void displayAppGroups() {

        adapter = new FirebaseListAdapter<GroupChat>(this, GroupChat.class,
                R.layout.users, groupsRef) {
            @Override
            protected void populateView(View v, GroupChat model, int position) {
                TextView userEmail = (TextView) v.findViewById(R.id.email_user);
                TextView userName = (TextView) v.findViewById(R.id.name_user);
                TextView userId = (TextView) v.findViewById(R.id.id_user);
                ImageView userImage = (ImageView) v.findViewById(R.id.userImageInUserList);

                Picasso.get().load(model.getGroup_pic()).placeholder(R.drawable.addfile).into(userImage);
                userEmail.setText(model.getGroup_name());
                userName.setText(model.getCreator_email());
                userId.setText(model.getId());
            }
        };
        listViewGroups.setAdapter(adapter);
    }

    //inflates the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addnewgroup, menu);

        return true;
    }

    //when the user clicks add new group menu button, takes the user to CreateNewGroup activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuId = item.getItemId();

        if(menuId == R.id.newGroup){
            Intent createGroup = new Intent(getApplicationContext(), CreateNewGroup.class);
            createGroup.putExtra(getString(R.string.user_id), id);
            createGroup.putExtra(getString(R.string.user_name), name);
            createGroup.putExtra(getString(R.string.user_email), email);
            createGroup.putExtra(getString(R.string.user_photo), photo);
            startActivity(createGroup);
        }

        return super.onOptionsItemSelected(item);  //default behavior (fall through), shouldn't really get here tho, why?

    }
}