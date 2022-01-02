package com.example.studybuddy;

import java.util.ArrayList;


//a class that formats the student user in an appropriate way
public class StudentUser {

    private String image_of_user;
    private String full_name;
    private String nick_name;
    private String e_mail;
    private String id;
    private String[] courses;
    private String major;
    private String classStanding;
    private String college;

    //public Courses[] courses_list;

    public StudentUser(String imageofuser, String fullname, String email, String userId){
        image_of_user = imageofuser;
        full_name = fullname;
        nick_name = "";
        e_mail = email;
        id = userId;
    }

    public StudentUser(String imageofuser, String fullname, String nickname, String email, String userId){
        image_of_user = imageofuser;
        full_name = fullname;
        nick_name = nickname;
        e_mail = email;
        id = userId;
    }

    public StudentUser(){

    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public String getE_mail() {
        return e_mail;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setImage_of_user(String image_of_user) {
        this.image_of_user = image_of_user;
    }

    public String getImage_of_user() {
        return image_of_user;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String[] getCourses() {
        return courses;
    }

    public void setCourses(String[] courses) {
        this.courses = courses;
    }


    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassStanding() {
        return classStanding;
    }

    public void setClassStanding(String classStanding) {
        this.classStanding = classStanding;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
}
