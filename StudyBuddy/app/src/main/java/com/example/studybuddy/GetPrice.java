package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GetPrice extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "com.example.studybuddy";
    EditText enterAmount2;
    Button okButton2;
    TextView priceText;
    double amount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_price);

        configureOkButton();
    }

    private void configureOkButton() {
        //Get amount to be paid
        enterAmount2 = (EditText) findViewById(R.id.enterAmount2);
        okButton2 = (Button) findViewById(R.id.okButton2);
        priceText = (TextView) findViewById(R.id.priceText);

        okButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GetPrice.this,MainActivity.class);
                String USDValue = enterAmount2.getText().toString();
                amount = Double.parseDouble(USDValue)*100;

                i.putExtra(EXTRA_AMOUNT,amount);
                startActivity(i);
            }
        });
    }
}