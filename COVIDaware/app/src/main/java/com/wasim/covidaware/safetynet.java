package com.wasim.covidaware;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.content.ContentValues.TAG;


public class safetynet {

    public static void checkEnabled(Context c, String mobile) {
        SafetyNet.getClient(c)
                .isVerifyAppsEnabled()
                .addOnCompleteListener(new OnCompleteListener<SafetyNetApi.VerifyAppsUserResponse>() {
                    @Override
                    public void onComplete(Task<SafetyNetApi.VerifyAppsUserResponse> task) {
                        if (task.isSuccessful()) {
                            SafetyNetApi.VerifyAppsUserResponse result = task.getResult();
                            if (result.isVerifyAppsEnabled()) {
                                Log.d("MY_APP_TAG", "The Verify Apps feature is enabled.");
                                verify(c,mobile);
                               // MainActivity m = new MainActivity();m.verify();
                            } else {
                                enable(c, mobile);
                            }
                        } else {
                            Log.e("MY_APP_TAG", "A general error occurred.");
                        }
                    }
                });
    }

    static int t = 0;

    private static void enable(Context c, String mobile) {
        SafetyNet.getClient(c)
                .enableVerifyApps()
                .addOnCompleteListener(new OnCompleteListener<SafetyNetApi.VerifyAppsUserResponse>() {
                    @Override
                    public void onComplete(Task<SafetyNetApi.VerifyAppsUserResponse> task) {
                        if (task.isSuccessful()) {
                            SafetyNetApi.VerifyAppsUserResponse result = task.getResult();
                            if (result.isVerifyAppsEnabled()) {
                                Log.d("MY_APP_TAG", "The user gave consent " +
                                        "to enable the Verify Apps feature.");
                                verify(c,mobile);
                            } else {
                                if (t > 2)
                                    ((Activity) c).finish();
                                Toast.makeText(c, "Please Enable c Setting to Proceed Further", Toast.LENGTH_LONG).show();
                                enable(c,mobile);
                                t++;
                            }
                        } else {
                            Log.e("MY_APP_TAG", "A general error occurred.");
                        }
                    }
                });
    }

    public static void verify(Context c, String mobile) {
        SafetyNet.getClient((Activity)c).verifyWithRecaptcha("6Lez24YaAAAAAJpQL50xj5M9PQDQJ08XqN8iGfy2")
                .addOnSuccessListener((Activity)c, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse recaptchaTokenResponse) {
                        String userResponseToken = recaptchaTokenResponse.getTokenResult();
                        com.wasim.covidaware.PhoneAuthSender.sendVerificationCode(mobile, (Activity)c);
                    }
                })
                .addOnFailureListener((Activity)c, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    }
                });
    }

}
