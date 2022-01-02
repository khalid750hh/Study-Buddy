package com.example.studybuddy;

import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.bindings.User;

import java.util.HashMap;

/**
 *  I originally made this class to store any constant objects/data we would be referencing and using globally across
 *   multiple files, but it turns out we didn't end up storing much stuff.
 *   All we are storing here is a RecombeeClient object and two HashMaps used to convert class year standing values from
 *   Strings (ie: Freshman, Sophomore, ...) to their representative numerical values in the Recombee database (ie: 1,2,...)
 */
public class UniversalObjects {
    public static final RecombeeClient2 CLIENT =
            new RecombeeClient2("study-buddy-2-dev", "RB6SjOzJD7IJ1enLNEcWsjtkzdzr6o9z2zW84FnQqvKiLk8tSiJBMqn8553YOJnC");

    public static final HashMap<Integer, String> numToClassStanding = new HashMap<Integer, String>() {{
        put(0, "Freshman");
        put(1, "Sophomore");
        put(2, "Junior");
        put(3, "Senior");
        put(4, "Graduate");
        put(5, "Other");
    }};

    public static final HashMap<String, Integer> classStandingToNum = new HashMap<String, Integer>() {{
        put("Freshman", 0);
        put("Sophomore", 1);
        put("Junior", 2);
        put("Senior", 3);
        put("Graduate", 4);
        put("Other", 5);
    }};


//    private static StudentUser currentSignedInStudentUser;
//
//    public static void setCurrentSignedInStudentUser(StudentUser su) {
//        currentSignedInStudentUser = su;
//    }
//
//    public static StudentUser getCurrentSignedInStudentUser() {
//        return currentSignedInStudentUser;
//    }
}
