package com.example.studybuddy;


import android.os.AsyncTask;

import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.AddItem;
import com.recombee.api_client.api_requests.AddPurchase;
import com.recombee.api_client.api_requests.AddUser;
import com.recombee.api_client.api_requests.DeletePurchase;
import com.recombee.api_client.api_requests.ListItems;
import com.recombee.api_client.api_requests.ListUserPurchases;
import com.recombee.api_client.api_requests.ListUsers;
import com.recombee.api_client.api_requests.SetItemValues;
import com.recombee.api_client.api_requests.SetUserValues;
import com.recombee.api_client.bindings.Item;
import com.recombee.api_client.bindings.Purchase;
import com.recombee.api_client.bindings.User;
import com.recombee.api_client.exceptions.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a class that inherits from AsyncTask so that it can execute a task
 *  asynchronously.
 * It is our specialized class for updating user information in our Recombee database
 */
public class AsyncUpdateUserInfo extends AsyncTask<Object, Void, Void> {

    // storing a constant Recombee client
    //  (see RecombeeClient2 class for explanation on why there is a 2 after RecombeeClient)
    private static final RecombeeClient2 client = UniversalObjects.CLIENT;

    // This method runs in the background on a different thread than the main thread.
    // It receives as input the ID and data of a certain user, and attempts to update that user's
    // data in Recombee
    @Override
    protected Void doInBackground(Object... inputs) {

        // inputs[0] is the String object for the ID of the particular user whose info we would like
        // to update
        String curUserId = (String) inputs[0];
        boolean isExistingUser = false;

        // first fetch the list of all existing users from Recombee
        User[] allUsers = null;
        try {
            allUsers = client.send(new ListUsers());
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // check if the current userid exists in the Recombee database
        for (User u: allUsers) {
            if (u.getUserId().equals(curUserId)) {
                isExistingUser = true;
                break;
            }
        }
        // if it does not, then create a new user in Recombee
        if (!isExistingUser) {
            try {
                client.send(new AddUser(curUserId));
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        // inputs[1] is a HashMap that contains the user profile information that we want to write to the
        //  Recombee User data table for this user. We send this data to Recombee to set for this user.
        HashMap<String, Object> curUserInfo = (HashMap<String, Object>) inputs[1];
        String classStanding = (String) curUserInfo.get("classStanding");
        Integer classStandingAsNum = UniversalObjects.classStandingToNum.get(classStanding);
        curUserInfo.remove("classStanding");
        curUserInfo.put("classStanding", classStandingAsNum);
        try {
            client.send(new SetUserValues(curUserId, curUserInfo));
        } catch (ApiException e) {
            e.printStackTrace();
        }


        // inputs[2] is the list of courses this user is currently enrolled in. Here, for every course
        //  in this list, we are storing that current user made a "Purchase" of the current course. It
        //  represents the relationship that the current User object in the database is "taking"/subscribed
        //  to the current "Item" (Course) object in the database
        ArrayList<String> courses = (ArrayList<String>) inputs[2];
        Purchase[] curCourses = null;
        try {
            curCourses = client.send(new ListUserPurchases(curUserId));
        } catch (ApiException e) {
            e.printStackTrace();
        }
        // delete all existing purchases first
        if (curCourses != null) {
            if (curCourses.length != 0) {
                for (Purchase p : curCourses) {
                    try {
                        client.send(new DeletePurchase(curUserId, p.getItemId()));
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // now try to add all the purchases that we want to update for this user
        Item[] courseItems = null;
        try {
            courseItems = client.send(new ListItems());
        } catch (ApiException e) {
            e.printStackTrace();
        }

        for (String c: courses) {
            c = c.replace(" ", "_");
            Item courseAsItem = null;
            for (Item i: courseItems) {
                if (c.equals(i.getItemId())) {
                    courseAsItem = i;
                }
            }

            // if the course that the user entered does not yet exist in Recombee, create it
            if (courseAsItem == null) {
                try {
                    client.send(new AddItem(c));
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                String college = c.substring(0, 3);
                String department = c.substring(4, 6);
                int courseNumber = Integer.parseInt(c.substring(7));
                Map<String, Object> itemData = new HashMap<String, Object>();
                itemData.put("college", college);
                itemData.put("department", department);
                itemData.put("courseNumber", courseNumber);
                try {
                    client.send(new SetItemValues(c, itemData));
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }

            try {
                client.send(new AddPurchase(curUserId, c));
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
//        String major = (String) curUserInfo.get("major");
//        String college = (String) curUserInfo.get("college");
//        StudentUser curStudentUser = new StudentUser(classStanding, major, college);
//        curStudentUser.setCourses(courses);

//        UniversalObjects.setCurrentSignedInStudentUser(curStudentUser);
        return null;
    }

}