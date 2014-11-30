package com.iiitd.mcproject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Shubham on 29 Nov 14.
 */
public class SignUpActivity extends Activity
{

    String userEmail;
    String userPassword;
    String userLogin;

    EditText emailtext;
    EditText logintext;
    EditText passtext;
    EditText passconfirmtext;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        emailtext = (EditText) findViewById(R.id.Email);
        logintext = (EditText) findViewById(R.id.UserName);
        passtext = (EditText) findViewById(R.id.Pass);
        passconfirmtext = (EditText) findViewById(R.id.PassConfirm);
        signUpButton = (Button) findViewById(R.id.btnSignUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                boolean passMatch = checkPasswords(passtext.getText().toString(),passconfirmtext.getText().toString());
                boolean emptyPass = isPassEmpty(passtext.getText().toString());
                boolean emptyEmail = isPassEmpty(emailtext.getText().toString());
                boolean lengthCheck = checkPasswordLength(passtext.getText().toString());
                if(passMatch && !emptyPass && lengthCheck && !emptyEmail)
                {

                    userEmail = emailtext.getText().toString();
                    userLogin = logintext.getText().toString();
                    userPassword = passtext.getText().toString();
                    Log.d("password", userPassword);
                    userSignQB();
                }
                else if(!passMatch)
                {
                    Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                }
                else if (!lengthCheck)
                {
                    Toast.makeText(getApplicationContext(), "Password length too short", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Fields are left empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void userSignQB()
    {
        SharedPreferences sharedPref = getSharedPreferences(Common.PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userLogin",userLogin);
        editor.putString("userPassword",userPassword);
        editor.putString("userEmail",userEmail);
        editor.commit();
        quickbloxRequest newReq = new quickbloxRequest(userLogin, userPassword, userEmail, SignUpActivity.this);
        newReq.execute();
    }

    private boolean checkPasswords(String p1, String p2)
    {
        if(p1.equals(p2))
            return true;
        else
            return false;
    }

    private boolean isPassEmpty(String p)
    {
        if(p.matches(""))
            return true;
        else
            return false;
    }

    private boolean checkPasswordLength(String p)
    {
        if(p.length()>8)
        {
            Log.d("password length",""+p.length());
            return true;
        }
        else
            return false;
    }



}
