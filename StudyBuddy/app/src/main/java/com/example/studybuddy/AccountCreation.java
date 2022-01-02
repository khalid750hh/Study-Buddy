package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.recombee.api_client.RecombeeClient;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The activity for setting up a user's new profile the first time they log in
 *  This activity starts only if the user signs in and their account id does not yet exist
 *  in our Recombee database
 * This activity consists of several drop down menus. The user fills it out with their Major,
 *  College, Class year, as well as an interactive listview for which classes they are taking
 *  After the user submits this form, they are taken directly to the home activity of the app,
 *   and all the form information gets sent to Recombee
 */
public class AccountCreation extends AppCompatActivity {

    // drop down for class year
    private Spinner classYearSpinner;
    private String selectedClassYear;

    // drop down for major
    private Spinner majorSpinner;
    private String selectedMajor;

    // drop down for college
    private Spinner collegeSpinner;
    private String selectedCollege;

    // the following are two drop downs plus one edit text for adding courses
    //  there is a drop down for selecting the college of the course, a drop down for
    //  selecting the department of the course, and one for specifying the course number
    // ex: CAS CS 501
    private Spinner addCourseCollegeSpinner;
    private String selectedAddCourseCollege;
    private Spinner addCourseDeptSpinner;
    private String selectedAddCourseDept;
    private EditText addCourseCourseNumber;
    private String addCourseCourseNumberInput;

    // button for adding courses to the list view
    private Button addCourseButton;
    // list view for displaying the added courses
    private ListView addCourseCoursesLV;
    private ArrayList<String> addCoursesCoursesList;

    // this button submits the form
    private Button continueButton;

    // google sign in client
    private GoogleSignInClient mGoogleSignInClient;

    // storing some firebase account information for the current user
    private String thisUserId;
    private String email;
    private String name;
    private String photo;

    // storing a constant Recombee client
    //  (see RecombeeClient2 class for explanation on why there is a 2 after RecombeeClient)
    private static final RecombeeClient2 client = UniversalObjects.CLIENT;


    // overriding onBackPressed to sign out the user if they press back
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mGoogleSignInClient.signOut();
        finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        // First, we want to grab the Bundle data that got sent to us from the MainActivity
        //  upon creation of this activity. This is firebase account data for the current user.
        //  Store it into our private variables.
        Intent extras = getIntent();
        thisUserId = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));


        // Initialize all the views for this activity
        classYearSpinner = (Spinner) findViewById(R.id.classYearSpinner);
        majorSpinner = (Spinner) findViewById(R.id.majorSpinner);
        collegeSpinner = (Spinner) findViewById(R.id.collegeSpinner);
        addCourseCollegeSpinner = (Spinner) findViewById(R.id.addCourseCollegeSpinner);
        addCourseDeptSpinner = (Spinner) findViewById(R.id.addCourseDeptSpinner);
        addCourseCourseNumber = (EditText) findViewById(R.id.addCourseCourseNumber);
        addCourseButton = (Button) findViewById(R.id.addCourseButton);
        addCoursesCoursesList = new ArrayList<String>();
        addCourseCoursesLV = (ListView) findViewById(R.id.addCourseCoursesLV);
        TextView coursesLVHeader = new TextView(this);
        coursesLVHeader.setText(R.string.courses_header);
        addCourseCoursesLV.addHeaderView(coursesLVHeader);
        continueButton = (Button) findViewById(R.id.continue_button);

        // create adapters for our drop down views and for our courses list view
        ArrayAdapter<CharSequence> classYearSpinnerAdapter = ArrayAdapter.createFromResource(this,
                                            R.array.class_year_array, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> majorSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.major_array, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> collegeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.college_array, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> addCourseCollegeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.college_array, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> addCourseDeptSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.cas_dept_array, android.R.layout.simple_spinner_item);

        AddCourseCoursesLVAdapter addCourseCoursesLVAdapter =
                new AddCourseCoursesLVAdapter(addCoursesCoursesList, this);

        // set the adapters for the views
        classYearSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        majorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classYearSpinner.setAdapter(classYearSpinnerAdapter);
        majorSpinner.setAdapter(majorSpinnerAdapter);
        collegeSpinner.setAdapter(collegeSpinnerAdapter);
        addCourseCollegeSpinner.setAdapter(addCourseCollegeSpinnerAdapter);
        addCourseDeptSpinner.setAdapter(addCourseDeptSpinnerAdapter);
        addCourseCoursesLV.setAdapter(addCourseCoursesLVAdapter);


        // when the user selects a class year from the class year drop down menu, store it
        // in our private String variable for class year
        classYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedClassYear = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // when the user selects a major from the major drop down menu, store it
        // in our private String variable for major
        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMajor = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // when the user selects a major from the major drop down menu, store it
        // in our private String variable for major
        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCollege = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // the following three listeners are for adding courses
        //  there is a listener for selecting the college of the course from that drop down, ex: CAS
        //  a listener for selecting the department of the course, ex: CS
        //  the user will then presumably enter the course number, ex: 501, into the edit text, and
        //  finally, they will click the + button for adding that course.
        //  thus, the third listener below listens for when addCourseButton is clicked, then takes the
        //  information from the college drop down (CAS), info from the dept drop down (CS), and info
        //  from the course number drop down (501), and adds all this information as a course to the
        //  list view of courses to be displayed on the activity
        addCourseCollegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedAddCourseCollege = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        addCourseDeptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedAddCourseDept = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        addCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // the number of the course that was entered into the edit text, ex: 501
                addCourseCourseNumberInput = addCourseCourseNumber.getText().toString();
                // here, we are concatenating the college, department, and number of the course,
                // ex: CAS CS 501
                String courseStr = selectedAddCourseCollege + " " +  selectedAddCourseDept + " " +
                                addCourseCourseNumberInput;
                // now add it to the list, and update the adapter with this updated list so that
                // the list view is ultimately updated to display this newly added course
                addCoursesCoursesList.add(courseStr);
                addCourseCoursesLVAdapter.setCoursesList(addCoursesCoursesList);
                addCourseCoursesLVAdapter.notifyDataSetChanged();
            }
        });

        // this is the submit button for the whole form
        // upon being clicked, the user will be directed to the home activity of the app
        // also, we will send along a Bundle of information for all of this user's information
        // so that the home activity has access to it
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> userData = new HashMap<String, Object>();
                userData.put("classStanding", selectedClassYear);
                userData.put("major", selectedMajor);
                userData.put("college", selectedCollege);

                Object[] submissionInfo = {thisUserId, userData, addCoursesCoursesList};
                Log.i("userData", userData.toString());
                (new AsyncUpdateUserInfo()).execute(submissionInfo);

                Intent goToHome = new Intent(getApplicationContext(), HomeActivity.class);
                goToHome.putExtra("user id", thisUserId);
                goToHome.putExtra("user name", name);
                goToHome.putExtra("user email", email);
                goToHome.putExtra("user picture url", photo);
                goToHome.putExtra("major", selectedMajor);
                goToHome.putExtra("college", selectedCollege);
                goToHome.putExtra("class year", selectedClassYear);
                goToHome.putExtra("courses", addCoursesCoursesList);
                startActivity(goToHome);
                finish();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

}