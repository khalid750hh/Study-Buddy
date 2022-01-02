package com.example.studybuddy;


//Tutor class is how we store the tutor sessions in our firebase
public class TutorChat {
    private String group_name;
    private String id;
    private StudentUser creator_of_group;
    private String group_pic;
    private String tutoring_price;

    public TutorChat(){

    }
    public TutorChat(StudentUser creatorStudent, String imageUrl, String name, String givenId, String price){
        this.group_name = name;
        this.id = givenId;
        this.creator_of_group = creatorStudent;
        this.group_pic = imageUrl;
        this.tutoring_price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public StudentUser getCreator_of_group() {
        return creator_of_group;
    }

    public void setCreator_of_group(StudentUser creator_of_group) {
        this.creator_of_group = creator_of_group;
    }

    public String getCreator_email() {
        return getCreator_of_group().getE_mail();
    }

    public String getCreator_Name(){
        return getCreator_of_group().getFull_name();
    }

    public String getCreator_id(){
        return getCreator_of_group().getId();
    }

    public String getGroup_pic() {
        return group_pic;
    }

    public void setGroup_pic(String group_pic) {
        this.group_pic = group_pic;
    }

    public String getTutoring_price() {
        return tutoring_price;
    }

    public void setTutoring_price(String tutoring_price) {
        this.tutoring_price = tutoring_price;
    }
}
