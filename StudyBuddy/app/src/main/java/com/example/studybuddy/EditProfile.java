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
import java.util.Arrays;
import java.util.HashMap;

/**
 * This is an activity where the current user can edit their profile to update
 *  their user information. It is very similar to the AccountCreation activity.
 *  See the AccountCreation.java file for more specific comments for each part
 *  of this file
 */
public class EditProfile extends AppCompatActivity {

    private Spinner classYearSpinner;
    private String selectedClassYear;
    private Spinner majorSpinner;
    private String selectedMajor;
    private Spinner collegeSpinner;
    private String selectedCollege;
    private Spinner addCourseCollegeSpinner;
    private String selectedAddCourseCollege;
    private Spinner addCourseDeptSpinner;
    private String selectedAddCourseDept;
    private EditText addCourseCourseNumber;
    private String addCourseCourseNumberInput;
    private Button addCourseButton;
    private ListView addCourseCoursesLV;
    private ArrayList<String> addCoursesCoursesList;
    private Button continueButton;
    private GoogleSignInClient mGoogleSignInClient;
    private String thisUserId;
    private String email;
    private String name;
    private String photo;

    // storing a constant Recombee client
    //  (see RecombeeClient2 class for explanation on why there is a 2 after RecombeeClient)
    private static final RecombeeClient2 client = UniversalObjects.CLIENT;


    // override onBackPressed so that we sign out and finish the current activity when the user
    //  clicks back
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

        // retrieve the information from the Bundle that was sent with the activity's start intent
        Intent extras = getIntent();
        thisUserId = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));

        // initialize the private variables for the views to be their respective views
        classYearSpinner = (Spinner) findViewById(R.id.classYearSpinner);
        majorSpinner = (Spinner) findViewById(R.id.majorSpinner);
        collegeSpinner = (Spinner) findViewById(R.id.collegeSpinner);
        addCourseCollegeSpinner = (Spinner) findViewById(R.id.addCourseCollegeSpinner);
        addCourseDeptSpinner = (Spinner) findViewById(R.id.addCourseDeptSpinner);
        addCourseCourseNumber = (EditText) findViewById(R.id.addCourseCourseNumber);
        addCourseButton = (Button) findViewById(R.id.addCourseButton);

        // store the current user's courses
        Bundle prevBundle = getIntent().getExtras();
        addCoursesCoursesList= new ArrayList<String>();
        String[] classesFromBundle = (String[]) prevBundle.get("classes");
        for (String c: classesFromBundle) {
            addCoursesCoursesList.add(c);
        }
        addCourseCoursesLV = (ListView) findViewById(R.id.addCourseCoursesLV);
        TextView coursesLVHeader = new TextView(this);
        coursesLVHeader.setText(R.string.courses_header);
        addCourseCoursesLV.addHeaderView(coursesLVHeader);
        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setText("Save");

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

        classYearSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        majorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classYearSpinner.setAdapter(classYearSpinnerAdapter);
        majorSpinner.setAdapter(majorSpinnerAdapter);
        collegeSpinner.setAdapter(collegeSpinnerAdapter);
        addCourseCollegeSpinner.setAdapter(addCourseCollegeSpinnerAdapter);
        addCourseDeptSpinner.setAdapter(addCourseDeptSpinnerAdapter);
        addCourseCoursesLV.setAdapter(addCourseCoursesLVAdapter);

        classYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedClassYear = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMajor = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCollege = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                addCourseCourseNumberInput = addCourseCourseNumber.getText().toString();
                String courseStr = selectedAddCourseCollege + " " +  selectedAddCourseDept + " " +
                        addCourseCourseNumberInput;
                addCoursesCoursesList.add(courseStr);
                addCourseCoursesLVAdapter.setCoursesList(addCoursesCoursesList);
                addCourseCoursesLVAdapter.notifyDataSetChanged();
            }
        });


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



//                String courseStr = selectedAddCourseCollege + "_" +  selectedAddCourseDept + "_" +
//                                addCourseCourseNumberInput;


                Intent goToHome = new Intent(getApplicationContext(), HomeActivity.class);
                goToHome.putExtra(getString(R.string.user_id), thisUserId);
                goToHome.putExtra(getString(R.string.user_name), name);
                goToHome.putExtra(getString(R.string.user_email), email);
                goToHome.putExtra(getString(R.string.user_photo), photo);
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