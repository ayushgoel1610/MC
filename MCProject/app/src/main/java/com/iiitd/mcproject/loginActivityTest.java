package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Shubham on 29 Nov 14.
 */
public class loginActivityTest extends Activity
{
    Button signUpButton;
    Button signInButton;
    TextView waitText;
    ProgressBar waitProgessbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_test);

        signInButton = (Button) findViewById(R.id.btnSignIn);
        signUpButton = (Button) findViewById(R.id.btnSignUp);
        waitText = (TextView) findViewById(R.id.loading_text);
        waitProgessbar = (ProgressBar) findViewById(R.id.loading_progressbar);

        waitText.setVisibility(View.GONE);
        waitProgessbar.setVisibility(View.GONE);

        SharedPreferences sp = getSharedPreferences(Common.PREF,MODE_PRIVATE);
        String checkSP = sp.getString("userRailsID","null");
        if(checkSP.equals("null")==false)
        {
            Log.d("checkSP", checkSP);
            signInButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            waitText.setVisibility(View.VISIBLE);
            waitProgessbar.setVisibility(View.VISIBLE);
            RailsServerSignUp r1 = new RailsServerSignUp(loginActivityTest.this);
            r1.userChat_auth();
//            waitText.setText("Loading Topics...");
        }
//        else
//        {
//            RailsServerSignUp r1 = new RailsServerSignUp(loginActivityTest.this);
//            r1.userChat_auth();
//        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intentSignIn = new Intent(loginActivityTest.this,SignInActivity.class);
                startActivity(intentSignIn);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignUp = new Intent(loginActivityTest.this,SignUpActivity.class);
                startActivity(intentSignUp);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //buttonsEnabled(Session.getActiveSession().isOpened());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
    }
}
