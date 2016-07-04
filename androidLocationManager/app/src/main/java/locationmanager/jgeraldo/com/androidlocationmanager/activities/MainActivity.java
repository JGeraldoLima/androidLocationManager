package locationmanager.jgeraldo.com.androidlocationmanager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments.MapsFragment;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Activity mActivity;

    private Context mContext;

    private static FragmentManager mFragmentManager;

    private static FragmentTransaction mTransaction;

    private static Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = getApplicationContext();

        mFragmentManager = getSupportFragmentManager();

        loadViewComponents();
        displayView(R.id.nav_map);
    }

    private void loadViewComponents() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, 0, 0);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item.getItemId());
        return true;
    }

    private void displayView(int itemId) {
        Fragment fragment = null;
        Intent intent = null;
        String tag = Constants.MAPS_FRAGMENT;

        if (itemId == R.id.nav_add_poi) {
            tag = Constants.ADD_POI_FRAGMENT;
//            fragment = new AddPOIFragment();
        } else if (itemId == R.id.nav_map) {
            tag = Constants.MAPS_FRAGMENT;
            fragment = new MapsFragment();
//            intent = new Intent(mActivity, MapsActivity.class);
        } else if (itemId == R.id.nav_poi_list) {
            tag = Constants.POI_LIST_FRAGMENT;
//            fragment = new POIListFragment();
        } else if (itemId == R.id.nav_settings) {
            tag = Constants.SETTINGS_FRAGMENT;
//            fragment = new SettingsFragment();
        } else if (itemId == R.id.nav_share) {
        } else if (itemId == R.id.nav_contact) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (fragment != null) {
            changeToFragment(fragment, tag);
        } else if (intent != null) {
            startActivity(intent);
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private void changeToFragment(Fragment fragment, String tag) {
        mTransaction = mFragmentManager.beginTransaction();
        mTransaction.replace(R.id.frame_container, fragment, tag);
        mTransaction.addToBackStack(null);
        mTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFragmentManager.getBackStackEntryCount() <= 1) {
//                Util.openQuestionAlertDialog(mActivity, Util.getString(mContext, R.string.quit_message), true);
            } else {
                mFragmentManager.popBackStackImmediate();
            }
        }
    }

}
