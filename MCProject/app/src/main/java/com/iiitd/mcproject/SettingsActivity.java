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
    View submitSuggestion;
    View suggestTopic;
    View suggestCategory;
    Button submit;
    EditText topic;
    EditText category;
    Button signout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        submitSuggestion=findViewById(R.id.submitSuggestion);
        suggestCategory=findViewById(R.id.suggestCategory);
        suggestTopic=findViewById(R.id.suggestTopic);
        submitSuggestion.setVisibility(View.GONE);
        suggestTopic.setVisibility(View.GONE);
        suggestCategory.setVisibility(View.GONE);
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
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                startActivity(loginScreen);
                SettingsActivity.this.finish();
            }
        });
    }

    public void toggle_contents(View v){
        submitSuggestion.setVisibility(submitSuggestion.isShown()?View.GONE:View.VISIBLE);
        suggestTopic.setVisibility(suggestTopic.isShown()?View.GONE:View.VISIBLE);
        suggestCategory.setVisibility(suggestCategory.isShown()?View.GONE:View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
