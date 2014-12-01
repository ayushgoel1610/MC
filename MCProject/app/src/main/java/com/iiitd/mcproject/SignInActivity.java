package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by Shubham on 29 Nov 14.
 */
public class SignInActivity extends Activity
{
    EditText emailText;
    EditText passText;
    Button signInButton;
    ProgressBar progressBar;

    private String userEmail;
    private String userPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        progressBar=(ProgressBar) findViewById(R.id.spinner);
        emailText = (EditText) findViewById(R.id.UserName);
        passText = (EditText) findViewById(R.id.Pass);
        signInButton = (Button) findViewById(R.id.btnSignIn);
        progressBar.setVisibility(View.GONE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                boolean emptyPass = isPassEmpty(passText.getText().toString());
                boolean emptyEmail = isPassEmpty(emailText.getText().toString());
                if(!emptyPass && !emptyEmail)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    emailText.setVisibility(View.GONE);
                    passText.setVisibility(View.GONE);
                    signInButton.setVisibility(View.GONE);
                    userPassword = passText.getText().toString();
                    userEmail = emailText.getText().toString();
                    Log.d("password", userPassword);
                    SharedPreferences sharedPref = getSharedPreferences(Common.PREF,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("userPassword",userPassword);
                    editor.putString("userEmail",userEmail);
                    editor.commit();
                    userLoginRequest userLoginRequest = new userLoginRequest(userEmail,userPassword, SignInActivity.this);
                    userLoginRequest.execute();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Fields are left empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private boolean isPassEmpty(String p)
    {
        if(p.matches(""))
            return true;
        else
            return false;
    }

}
