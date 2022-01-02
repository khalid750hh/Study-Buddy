package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.squareup.picasso.Picasso;

public class MainActivityChat extends AppCompatActivity {

    private String id;
    private String email;
    private String name;
    private String photo;
    private String chatID;
    private String chatName;
    private String otherUserImage;

    private EditText txtMessage;
    private FloatingActionButton btnSend, btnImage;
    private ListView listViewChat;
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference myRef;
    private DatabaseReference chatroomRef;
    private StorageReference filePath;

    private final int sendImageCode = 7500;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String myUrl;

    private String lastId = null;
    private String lastYear = null;
    private String lastMonth = null;
    private String lastDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().hide();

        //set up references to database and get intent extras
        myRef = FirebaseDatabase.getInstance().getReference(getString(R.string.chat_rooms));
        Intent extras = getIntent();
        id = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));
        chatID = extras.getStringExtra(getString(R.string.chat_id));
        chatName = extras.getStringExtra(getString(R.string.chat_name));

        //set the title text to user's name/group name/tutor name
        getSupportActionBar().setTitle(chatName);

        //get a reference to the chat in our database
        chatroomRef = myRef.child(chatID);

        //configure the views
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        btnSend = (FloatingActionButton) findViewById(R.id.btnSend);
        btnImage = (FloatingActionButton) findViewById(R.id.btnImage);
        listViewChat = (ListView) findViewById(R.id.listViewChat);

        //call to get the messages from the database and display them
        displayChatMessages();

        //onClick to send text messages
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check if the message is empty or all blanks
                if(txtMessage.getText().toString().replaceAll("\\s", "").isEmpty() == false){
                    ChatMessage message = new ChatMessage(txtMessage.getText().toString(), getString(R.string.text), name, id);
                    DatabaseReference messageRef = chatroomRef.push();
                    messageRef.setValue(message);
                    txtMessage.setText("");
                }
            }
        });

        //onClick to send images
        //starts the intent to get the image. implicit intent.
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendImage = new Intent();
                sendImage.setAction(Intent.ACTION_GET_CONTENT);
                sendImage.setType(getString(R.string.image_type));
                startActivityForResult(sendImage, sendImageCode);
            }
        });
    }

    //function gets the messages from the database and puts them in the message.xml format
    //checks whether the message is text or image and acts accordingly
    //for images, we used picasso library to download images from their urls
    private void displayChatMessages(){
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, chatroomRef) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                String messageId = model.getUserId();
                String messageType = model.getMessageType();

                //in case the message sent is an image
                if (messageType.equals(getString(R.string.image))){
                    ImageView imageDisplay = (ImageView) v.findViewById(R.id.imageDisplay);
                    ImageView userImage = (ImageView) v.findViewById(R.id.userImage);
                    TextView messageText = (TextView) v.findViewById(R.id.message_text);
                    TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                    TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                    messageText.setVisibility(View.GONE);

                    //sets the background of the messages set by the user to orange.
                    if (messageId.equals(id)){
                        v.setBackgroundResource(R.color.orange);
                    }

                    messageUser.setText(model.getMessageUser());

                    //date in the database is set in miliseconds, so this formats it to dd-MM-yy HH:mm
                    android.text.format.DateFormat df = new android.text.format.DateFormat();

                    messageTime.setText(df.format(getString(R.string.date_format),
                            model.getMessageTime()));

                    //call to picasso to download the image and place it into the imageview
                    //sets a drawable as a place holder until it's done downloading the image.
                    Picasso.get().load(model.getMessageText()).placeholder(R.drawable.addfile).into(imageDisplay);


                    //displays the image of the user that sent the message.
                    DatabaseReference userImageRef = FirebaseDatabase.getInstance().getReference(getString(R.string.users)).child(model.getUserId());
                    userImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StudentUser post = snapshot.getValue(StudentUser.class);
                            //call to picasso to download the image and place it into the imageview
                            //sets a drawable as a place holder until it's done downloading the image.
                            Picasso.get().load(post.getImage_of_user()).placeholder(R.drawable.addfile).into(userImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                //in case the message sent was a text
                else if(messageType.equals(getString(R.string.text))){
                    ImageView imageDisplay = (ImageView) v.findViewById(R.id.imageDisplay);
                    ImageView userImage = (ImageView) v.findViewById(R.id.userImage);
                    TextView messageText = (TextView) v.findViewById(R.id.message_text);
                    TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                    TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                    imageDisplay.setVisibility(View.GONE);

                    messageText.setText(model.getMessageText());
                    if (messageId.equals(id)){
                        v.setBackgroundResource(R.color.orange);
                    }
                    messageUser.setText(model.getMessageUser());

                    android.text.format.DateFormat df = new android.text.format.DateFormat();

                    messageTime.setText(df.format(getString(R.string.date_format),
                            model.getMessageTime()));

                    //displays the image of the user that sent the message.
                    DatabaseReference userImageRef = FirebaseDatabase.getInstance().getReference(getString(R.string.users)).child(model.getUserId());
                    userImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StudentUser post = snapshot.getValue(StudentUser.class);
                            //call to picasso to download the image and place it into the imageview
                            //sets a drawable as a place holder until it's done downloading the image.
                            Picasso.get().load(post.getImage_of_user()).placeholder(R.drawable.addfile).into(userImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        };
        //set the firebase adapter as the adapter for the listview that displays the chat
        listViewChat.setAdapter(adapter);
        listViewChat.setDivider(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //uses StorageTask to send the images and store them to firebase's storage
        if(requestCode == sendImageCode && resultCode==RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            StorageReference myStorageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.images));
            DatabaseReference messageRef = chatroomRef.push();
            StorageReference filePath = myStorageReference.child(messageRef.getKey().toString());
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
                    //when image upload is completed, send the message containing the url to the image in firebase storage
                    //downloading the image and displaying it is handled by picasso library
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        ChatMessage message = new ChatMessage(myUrl, getString(R.string.image), name, id);
                        messageRef.setValue(message);
                    }
                }
            });
        }
    }
}