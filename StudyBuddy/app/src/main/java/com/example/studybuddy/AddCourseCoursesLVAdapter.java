package com.example.studybuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * This is a class that extends ArrayAdapter. It is our specialized adapter for processing
 *  the list of courses that the user adds to their profile and updating the list view that
 *  displays these courses so that it displays all the new courses properly.
 *
 *  the inspiration for this class came from the following link:
 *   https://www.survivingwithandroid.com/android-async-listview-jee-and-restful/
 */
public class AddCourseCoursesLVAdapter extends ArrayAdapter<String> {

    // keep track of the list of courses as well as the context
    private List<String> coursesList;
    private Context context;

    // constructor for a list of courses and a context
    public AddCourseCoursesLVAdapter(List<String> coursesList, @NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.coursesList = coursesList;
        this.context = context;
    }

    // get the number of courses in the courseList
    public int getCount() {
        if (coursesList == null) return 0;
        return coursesList.size();
    }

    // get a specific course from the course list according to a given index position
    public String getItem(int position) {
        if (coursesList == null) return null;
        return coursesList.get(position);
    }

    // getter for coursesList
    public List<String> getCoursesList() {
        return coursesList;
    }

    // setter for coursesList
    public void setCoursesList(List<String> coursesList) {
        this.coursesList = coursesList;
    }

    // this method overrides getView() of ArrayAdapter, it returns the view that will be displayed
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.add_courses_layout, null);
        }

        // grabbing the textview and displaying the course string in there, ex: CAS CS 501
        TextView courseTV = (TextView) view.findViewById(R.id.courseTV);
        courseTV.setText(coursesList.get(position));

        // this is the delete button for this row in the list. There is one on each row right next
        // to the course. ex: CAS CS 501 [-]
        // it is a button with the "-" symbol and will delete the current course from the list
        // and update the adapter when clicked, as seen in the listener's onClick method
        Button delBtn = (Button) view.findViewById(R.id.delBtn);

        delBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                coursesList.remove(coursesList.get(position));
                notifyDataSetChanged();
            }
        });

        return view;
    }





}