package com.iiitd.mcproject;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MasterActivity extends Activity
        implements CategoryFragment.OnFragmentInteractionListener,
        RecentFragment.OnFragmentInteractionListener,
        TrendingFragment.OnFragmentInteractionListener{

    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        mDrawerTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        //First frame on opening
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, MainTabsFragment.newInstance(), MainTabsFragment.TAG).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.master, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /*
    A DrawerClickListener for the drawer labels
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            navigateToFragment(position);
        }
    }

    private void navigateToFragment(int position) {

        switch(position) {
            case 0:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, MainTabsFragment.newInstance(), MainTabsFragment.TAG).commit();
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}
