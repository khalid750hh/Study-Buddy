package com.example.studybuddy;


//not used
public class Courses {
    public String course_college;
    public String course_major;
    public String course_code;
    public String professor_name;

    public Courses(String college, String major, String code){
        course_college = college;
        course_major = major;
        course_code = code;
        professor_name = "";
    }

    public Courses(String college, String major, String code, String professor){
        course_college = college;
        course_major = major;
        course_code = code;
        professor_name = professor;
    }
}
