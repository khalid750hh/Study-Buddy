package com.example.studybuddy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.recombee.api_client.bindings.Recommendation;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class RecommendationAdapter extends ArrayAdapter<StudentUser> {

    private List<StudentUser> recommendationList;

    private Context context;

    public RecommendationAdapter(List<StudentUser> recommendationList, @NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.recommendationList = recommendationList;
        this.context = context;
    }

    public int getCount() {
        if (recommendationList == null) return 0;
        return recommendationList.size();
    }

    public StudentUser getItem(int position) {
        if (recommendationList == null) return null;
        return recommendationList.get(position);
    }

    public long getRecommendationId(int position) {
        if (recommendationList == null) return -1;
        return recommendationList.get(position).hashCode();
    }

    public List<StudentUser> getRecommendationList() {
        return recommendationList;
    }

    public void setRecommendationList(List<StudentUser> recommendationList) {
        this.recommendationList = recommendationList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.users, null);

        }

        TextView userEmail = (TextView) v.findViewById(R.id.email_user);
        TextView userName = (TextView) v.findViewById(R.id.name_user);
        TextView userId = (TextView) v.findViewById(R.id.id_user);
        TextView userCourses = (TextView) v.findViewById(R.id.courses_user);
        ImageView userImage = (ImageView) v.findViewById(R.id.userImageInUserList);

        Picasso.get().load(recommendationList.get(position).getImage_of_user()).placeholder(R.drawable.addfile).into(userImage);

        userEmail.setText(this.recommendationList.get(position).getE_mail());
        userName.setText(this.recommendationList.get(position).getFull_name());
        userId.setText(this.recommendationList.get(position).getId());
        String coursesDisplayStr = "";
        for (String c: this.recommendationList.get(position).getCourses()) {
            coursesDisplayStr += c + ", ";
        }
        coursesDisplayStr = coursesDisplayStr.substring(0, coursesDisplayStr.length()-2);
        userCourses.setText(coursesDisplayStr);


//        @Override
//        protected void populateView(View v, StudentUser model, int position) {
//            TextView userEmail = (TextView) v.findViewById(R.id.email_user);
//            TextView userName = (TextView) v.findViewById(R.id.name_user);
//            TextView userId = (TextView) v.findViewById(R.id.id_user);
//
//            userEmail.setText(model.getE_mail());
//            userName.setText(model.getFull_name());
//            userId.setText(model.getId());
//        }
//    };

//        Recommendation r = recommendationList.get(position);
//        TextView text = (TextView) v.findViewById(R.id.name);
//        text.setText(r.getName());
//        TextView text1 = (TextView) v.findViewById(R.id.surname);
//        text1.setText(c.getSurname());
//        TextView text2 = (TextView) v.findViewById(R.id.email);
//        text2.setText(c.getEmail());
//        TextView text3 = (TextView) v.findViewById(R.id.phone);
//        text3.setText(c.getPhoneNum());
        return v;
    }
}