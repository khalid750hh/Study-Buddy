package com.example.studybuddy;

import java.util.Date;


//how messages are stored in our database
//if the message is text, store the text in messageText
//if the message is an image store the url in messageText
public class ChatMessage {
    private String messageText;
    private String messageUser;
    private long messageTime;
    private String dateFormat;
    private String userId;
    private String messageType;

    public ChatMessage(String text, String type, String user, String id){
        this.messageText = text;
        this.messageUser = user;
        this.userId = id;
        this.messageType = type;
        messageTime = new Date().getTime();
        dateFormat = "";
    }

    public ChatMessage(){

    }

    public String getMessageText(){
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser(){
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime(){
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
