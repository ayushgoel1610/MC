package com.iiitd.mcproject;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class loginActivity extends FragmentActivity {

    private LoginButton loginBtn;
    private ProgressBar progressBar;

    private TextView userNameView;

    private Button userLoginButton;
    private Button userSignUpButton;

    private UiLifecycleHelper uiHelper;

    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions","email");

    private static String message = "Sample status posted from android app";
    private String userToken;
    private String userPassword = null;
    private String userLogin;
    private String userEmail;
    private String userFullName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.iiitd.mcproject",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        userLoginButton = (Button) findViewById(R.id.button_login);
        userSignUpButton = (Button) findViewById(R.id.button_signUp);
        userNameView = (TextView) findViewById(R.id.user_name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        loginBtn = (LoginButton) findViewById(R.id.authButton);
        loginBtn.setVisibility(View.GONE);
        userLoginButton.setVisibility(View.GONE);
        userSignUpButton.setVisibility(View.GONE);
        loginBtn.setReadPermissions(Arrays.asList("email"));
        SharedPreferences sp = getSharedPreferences(Common.PREF,MODE_PRIVATE);
        String checkSP = sp.getString("userRailsID","null");
        if(checkSP.equals("null"))
        {
            loginBtn.setVisibility(View.VISIBLE);
            userLoginButton.setVisibility(View.VISIBLE);
            userSignUpButton.setVisibility(View.VISIBLE);
        }
        else
        {
            RailsServerSignUp r1 = new RailsServerSignUp(loginActivity.this);
            r1.userChat_auth();
        }

        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPasswordAlert();

            }
        });

        userSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordAlert();
            }
        });

        loginBtn.setUserInfoChangedCallback(new UserInfoChangedCallback()
        {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null)
                {
                    userNameView.setText("Loading Topics");
                    progressBar.setVisibility(View.VISIBLE);

                    userLogin = user.getName();
                    userLogin = userLogin.replaceAll(" ","");
                    userEmail = user.asMap().get("email").toString();
                    userFullName = user.getName();

                    userToken = Session.getActiveSession().getAccessToken();
                    Log.d("debug", "userToken: " + userToken);

                    /*
                    SharedPreferences sp = getSharedPreferences(Common.PREF,MODE_PRIVATE);
                    String checkPass = sp.getString("userRailsID","null");
                    if(checkPass.equals("null"))
                    {
                        passwordAlert();
                    }
                    else
                    {
                        RailsServerSignUp r1 = new RailsServerSignUp(loginActivity.this);
                        r1.userChat_auth();
                    }
                    */
                }
            }
        });
        /*
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getPasswordAlert();
            }
        });
        */
    }

    private void userSignQB()
    {
       SharedPreferences sharedPref = getSharedPreferences(Common.PREF,MODE_PRIVATE);
       SharedPreferences.Editor editor = sharedPref.edit();
       editor.putString("userLogin",userLogin);
       editor.putString("userPassword",userPassword);
       editor.putString("userEmail",userEmail);
       editor.commit();
       quickbloxRequest newReq = new quickbloxRequest(userLogin, userPassword, userEmail, loginActivity.this);
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

    private void getPasswordAlert()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Password");
        alert.setMessage("Enter password");

        final LinearLayout getPasswordLayout = new LinearLayout(this.getApplicationContext());
        getPasswordLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView textView1 = new TextView(this);
        textView1.setText("Enter the Password");
        getPasswordLayout.addView(textView1);

        final EditText password1 = new EditText(this);
        password1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        getPasswordLayout.addView(password1);


        alert.setView(getPasswordLayout);

        alert.setCancelable(false);

        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog passDialog = alert.create();
        passDialog.show();
        passDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getPasswordLayout.getWindowToken(), 0);
                boolean emptyPass = isPassEmpty(password1.getText().toString());
                if(!emptyPass)
                {
                    userPassword = password1.getText().toString();
                    Log.d("password", userPassword);
                    passDialog.dismiss();
                    SharedPreferences sharedPref = getSharedPreferences(Common.PREF,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("userLogin",userLogin);
                    editor.putString("userPassword",userPassword);
                    editor.putString("userEmail",userEmail);
                    editor.commit();
                    userLoginRequest userLoginRequest = new userLoginRequest(userEmail,userPassword, loginActivity.this);
                    userLoginRequest.execute();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Password field left empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void passwordAlert()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Password");
        alert.setMessage("Set app password");

        final LinearLayout passwordLayout = new LinearLayout(this.getApplicationContext());
        passwordLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView textView1 = new TextView(this);
        textView1.setText("Enter the Password");
        passwordLayout.addView(textView1);

        final EditText password1 = new EditText(this);
        password1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordLayout.addView(password1);

        final TextView textView2 = new TextView(this);
        textView2.setText("Re-enter the Password");
        passwordLayout.addView(textView2);

        final EditText password2 = new EditText(this);
        password2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordLayout.addView(password2);

        alert.setView(passwordLayout);

        alert.setCancelable(false);

        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog passDialog = alert.create();
        passDialog.show();
        passDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(passwordLayout.getWindowToken(), 0);
                boolean passMatch = checkPasswords(password1.getText().toString(),password2.getText().toString());
                boolean emptyPass = isPassEmpty(password1.getText().toString());
                boolean lengthCheck = checkPasswordLength(password1.getText().toString());
                if(passMatch && !emptyPass && lengthCheck)
                {
                    userPassword = password1.getText().toString();
                    Log.d("password", userPassword);
                    passDialog.dismiss();
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
                    Toast.makeText(getApplicationContext(), "Password field left empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                //buttonsEnabled(true);
                Log.d("FacebookSampleActivity", "Facebook session opened");
                Log.d("debug","userToken"+userToken);
            } else if (state.isClosed()) {
                //buttonsEnabled(false);
                Log.d("FacebookSampleActivity", "Facebook session closed");
            }
        }
    };

    public boolean checkPermissions() {
        Session s = Session.getActiveSession();
        if (s != null) {
            return s.getPermissions().contains("publish_actions");
        } else
            return false;
    }

    public void requestPermissions() {
        Session s = Session.getActiveSession();
        if (s != null)
            s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
                    this, PERMISSIONS));
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        //buttonsEnabled(Session.getActiveSession().isOpened());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

}
