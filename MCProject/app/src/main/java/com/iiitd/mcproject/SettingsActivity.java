package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SettingsActivity extends Activity {
    Button submit;
    EditText topic;
    EditText category;
    Button signout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        topic=(EditText)findViewById(R.id.suggestTopic);
        category=(EditText) findViewById(R.id.suggestCategory);
        submit=(Button) findViewById(R.id.submitSuggestion);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSuggestion s1=new SendSuggestion(topic.getText().toString(),category.getText().toString(),SettingsActivity.this);
                s1.execute();
                topic.setText("");
                category.setText("");
            }
        });
        signout=(Button)findViewById(R.id.signout);
        final Intent loginScreen=new Intent(this,loginActivityTest.class);
        loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final SharedPreferences sp=this.getSharedPreferences(Common.PREF, 0);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RailsServerSignUp r1 = new RailsServerSignUp(SettingsActivity.this);
                r1.user_logout();
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                MasterActivity.destroyMaster.finish();
                startActivity(loginScreen);
                SettingsActivity.this.finish();
            }
        });
    }


}
