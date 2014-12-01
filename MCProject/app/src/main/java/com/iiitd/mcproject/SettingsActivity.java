package com.iiitd.mcproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SettingsActivity extends Activity {
    View submitSuggestion;
    View suggestTopic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        submitSuggestion=findViewById(R.id.submitSuggestion);
        suggestTopic=findViewById(R.id.suggestTopic);
        submitSuggestion.setVisibility(View.GONE);
        suggestTopic.setVisibility(View.GONE);
    }

    public void toggle_contents(View v){
        submitSuggestion.setVisibility(submitSuggestion.isShown()?View.GONE:View.VISIBLE);
        suggestTopic.setVisibility(suggestTopic.isShown()?View.GONE:View.VISIBLE);
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
