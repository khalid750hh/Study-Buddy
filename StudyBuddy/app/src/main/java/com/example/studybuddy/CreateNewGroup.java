package com.example.studybuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

public class CreateNewGroup extends AppCompatActivity {

    private String id;
    private String email;
    private String name;
    private String photo;

    private DatabaseReference groupsRef;
    private DatabaseReference myRef;
    private DatabaseReference newGroup;

    private EditText edtTxtCreateGroupName;
    private ImageButton imgBtnGroupPic;
    private Button btnCreateGroup;

    private final int sendImageCode = 7500;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String myUrl;

    private String groupImageUrl;

    private boolean created;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        if(created){
            finish();
        }


        //set the database references
        groupsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.group_chats));
        myRef = FirebaseDatabase.getInstance().getReference(getString(R.string.chat_rooms));
        newGroup = groupsRef.push();


        //Get user info passed from previous activity
        Intent extras = getIntent();
        id = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));

        //configure the views
        edtTxtCreateGroupName = (EditText) findViewById(R.id.edtTxtCreateGroupName);
        imgBtnGroupPic = (ImageButton) findViewById(R.id.imgBtnGroupPic);
        btnCreateGroup = (Button) findViewById(R.id.btnCreateGroup);

        groupImageUrl = photo;

        //change the tutor session picture
        imgBtnGroupPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendImage = new Intent();
                sendImage.setAction(Intent.ACTION_GET_CONTENT);
                sendImage.setType(getString(R.string.image_type));
                startActivityForResult(sendImage, sendImageCode);
            }
        });

        //onClick for the button the creates the session,
        //group name can't be empty or all blanks
        //when the condition is met, the group is
        // created and it takes the user to the chat
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtTxtCreateGroupName.getText().toString().replaceAll(" ", "").equals("")){
                    Toast emptyName = Toast.makeText(getApplicationContext(),
                            getText(R.string.error_no_group_name), Toast.LENGTH_SHORT);
                    emptyName.show();
                }
                else{
                    try {
                        StudentUser creator = new StudentUser(photo, name, email, id);
                        GroupChat group = new GroupChat(creator,
                                groupImageUrl,
                                edtTxtCreateGroupName.getText().toString(),
                                newGroup.getKey());
                        newGroup.setValue(group);
                        DatabaseReference chatroomRef = myRef.child(newGroup.getKey());
                        ChatMessage message = new ChatMessage( edtTxtCreateGroupName.getText().toString() + " Created!", "text", name, id);
                        DatabaseReference messageRef = chatroomRef.push();
                        messageRef.setValue(message);
                        edtTxtCreateGroupName.setText("");


                        String userEmail = group.getCreator_email();
                        String chatName = group.getGroup_name();
                        String chatId = group.getId();

                        Intent goToChat = new Intent(getApplicationContext(), MainActivityChat.class);
                        goToChat.putExtra(getString(R.string.user_id), id);
                        goToChat.putExtra(getString(R.string.user_name), name);
                        goToChat.putExtra(getString(R.string.user_email), email);
                        goToChat.putExtra(getString(R.string.user_photo), photo);
                        goToChat.putExtra(getString(R.string.chat_id), chatId);
                        goToChat.putExtra(getString(R.string.chat_name), chatName);
                        created = true;

                        startActivity(goToChat);
                    }
                    catch (Exception e){
                        Toast error = Toast.makeText(getApplicationContext(),
                                getText(R.string.error_try_again), Toast.LENGTH_SHORT);
                        error.show();
                    }
                }
            }
        });
    }


    //to make sure that when the user finishes creating the group
    // and goes to the chat, they don't comeback to this activity
    // when they press the back button
    @Override
    protected void onStop() {
        super.onStop();
        if(created){
            finish();
        }
    }

    //handles uploading the group image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == sendImageCode && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();
            StorageReference myStorageReference = FirebaseStorage.getInstance()
                    .getReference().child(getString(R.string.images));
            DatabaseReference imageRef = newGroup.child(getString(R.string.image_of_user));
            StorageReference filePath = myStorageReference.child(newGroup.getKey());

            uploadTask = filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        Toast toast2 = Toast.makeText(getApplicationContext(),
                                getString(R.string.file_send_fail), Toast.LENGTH_SHORT);
                        toast2.show();

                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        imageRef.setValue(myUrl);
                        Picasso.get().load(myUrl)
                                .placeholder(R.drawable.groups).into(imgBtnGroupPic);
                        groupImageUrl = myUrl;
                    }
                }
            });
        }

    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        finish();
//    }
}