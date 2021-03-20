package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.broooapps.otpedittext2.OnCompleteListener;
import com.broooapps.otpedittext2.OtpEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    AppCompatEditText phone;
    AppCompatButton send, verify;
    OtpEditText otpview;
    TextView orreg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phone = findViewById(R.id.etPhoneNumber);
        send = findViewById(R.id.btnSendConfirmationCode);
        verify = findViewById(R.id.verify);
        otpview = findViewById(R.id.otp_view);
        orreg = findViewById(R.id.orlogin);

        orreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
            }
        });

        otpview.setTextColor(getResources().getColor(R.color.white));
        otpview.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(String value) {
                verify.setEnabled(true);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String p = phone.getText().toString().trim();
                if (p.isEmpty() || p.length() < 10) {
                    phone.setError("Field cannot be left empty");
                } else {
                    FirebaseDatabase.getInstance().getReference("users").child("+91" + p)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        phone.setEnabled(false);
                                        send.setEnabled(false);
                                        verify.setVisibility(View.VISIBLE);
                                        otpview.setVisibility(View.VISIBLE);
                                        verify.setEnabled(false);
                                        safetynet.checkEnabled(LoginActivity.this,"+91" + p);
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "Please Register Number And Then Login", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpview.getText().toString();
                Log.e("", "onClick: "+otp );
                if(otp.length()<6||otp.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Wrong OTP Values", Toast.LENGTH_SHORT).show();
                }else {
                    PhoneAuthSender.verifyVerificationCode(otp);
                }
            }
        });

    }
}