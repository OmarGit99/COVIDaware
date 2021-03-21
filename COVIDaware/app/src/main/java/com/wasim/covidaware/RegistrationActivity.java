package com.wasim.covidaware;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {


    TextView orlogin;

    //edittext fields
    EditText usernamefield;
    EditText phonenumberfield;
    EditText Aadharnumberfield;
    EditText dob;

    //storing creds
    String username;
    String password;
    String phonenumber;
    String aadharnumber;

    DatePickerDialog dp;

    Button reg;

    //sign up process
    public void signup(){
        //get users details and store them in variables
        username = usernamefield.getText().toString();

        if(usernamefield.getText().toString().matches("") || usernamefield.getText().toString().contains("\\s")){
            usernamefield.setError("Please enter a valid username");
            usernamefield.requestFocus();
        }
        else if(phonenumberfield.getText().toString().matches("") || phonenumberfield.length() < 10){
            phonenumberfield.setError("Please enter a valid phone number");
            phonenumberfield.requestFocus();
        }
        else if(Aadharnumberfield.getText().length() < 12){
            Aadharnumberfield.setError("Please enter a valid Aadhar card number");
            Aadharnumberfield.requestFocus();
        }
        else if(dob.getText().toString().matches("") || dob.getText().length() < 10){
            dob.setError("Please enter a valid DOB");
            dob.requestFocus();
        }
        else{
            phonenumber = phonenumberfield.getText().toString();
            aadharnumber = Aadharnumberfield.getText().toString();

            Log.i("creds", username + " "+ phonenumber+ " "+ aadharnumber);  //Access creds from here
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child("+91"+phonenumber);
            ref.child("Name").setValue(username);
            ref.child("pn").setValue("+91"+phonenumber);
            ref.child("aadhar").setValue(aadharnumber);
            ref.child("dob").setValue(sdob);

            Toast.makeText(this, "Registration Successful Pls Login", Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, LoginActivity.class));

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.blueappcolorthemelight));
        try {
            getSupportActionBar().hide();
        }catch (NullPointerException e){
            Log.i("check",e.getMessage());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        //Textview orlogin for jumping to login activity

        ((Button)findViewById(R.id.loginbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        //initializing user creds
        username = "";
        phonenumber = "";
        aadharnumber = "";


        //user's details fields
        usernamefield = (EditText) findViewById(R.id.usernamefield);
        phonenumberfield = findViewById(R.id.phonenumberfield);
        Aadharnumberfield = (EditText) findViewById(R.id.aadharnumberfield);
        dob = findViewById(R.id.dobfield);
        reg = findViewById(R.id.btnLogin);

        dob.setClickable(true);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dp = new SpinnerDatePickerDialogBuilder()
                        .context(RegistrationActivity.this)
                        .callback(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                sdob = (dayOfMonth > 9 ? dayOfMonth : "0" + dayOfMonth) + "/" + (monthOfYear > 9 ? monthOfYear : "0" + (monthOfYear + 1)) + "/" + year;
                                dob.setText(sdob);

                            }
                        })
                        .spinnerTheme(R.style.NumberPickerStyle)
                        .showTitle(false)
                        .build();
                dp.show();
            }
        });


        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });


    }

    String sdob;

}