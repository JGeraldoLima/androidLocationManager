package locationmanager.jgeraldo.com.androidlocationmanager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import io.realm.Realm;
import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.listeners.OnHomePressedListener;
import locationmanager.jgeraldo.com.androidlocationmanager.receivers.PhoneUnlockedReceiver;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.RealmUtil;
import locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments.LocationsListFragment;
import locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments.MapsFragment;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.KeyWatcher;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.Preferences;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    private Activity mActivity;

    private Context mContext;

    private MyLocationManager mLocationManager;

    private KeyWatcher mHomeWatcher;

    private PhoneUnlockedReceiver mPhoneLockWatcher;

    private static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";

    private static FragmentManager mFragmentManager;

    private static FragmentTransaction mTransaction;

    private static Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RealmUtil.initialize(this);

        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = getApplicationContext();
        mFragmentManager = getSupportFragmentManager();
        Util.initGPSManager(mContext, mActivity);

        mHomeWatcher = new KeyWatcher(mContext);
        mPhoneLockWatcher = new PhoneUnlockedReceiver();
        mLocationManager = Util.getLocationManager();

        registerReceiver(mPhoneLockWatcher,
            new IntentFilter(ACTION_USER_PRESENT));
        setHomeWatcher();

        loadViewComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocationManager == null) {
            Util.initGPSManager(mContext, mActivity);
            mLocationManager = Util.getLocationManager();
        }

        if (mLocationManager.checkLocationServicesStatus()) {
            if (Util.checkPermissions(mActivity, Constants.LOCATION_PERMISSIONS_FLAG,
                Constants.LOCATION_PERMISSION_CODES, Constants.LOCATION_PERMISSIONS_CODE, null)) {
                mLocationManager.startLocationUpdatesByPrecisionStatus();
                displayView(R.id.nav_map);
            }
            // TODO run more exploration tests: what to do if it returns false?
        } else {
            mLocationManager.openEnableLocationServicesDialog(mActivity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.disconnectClient();
        mLocationManager.stopUpdates(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Util.onRequestPermissionsResult(mActivity, mContext, requestCode, grantResults);
        if (Util.isPermissionsGranted(Constants.LOCATION_PERMISSION_CODES, mContext)) {
            displayView(R.id.nav_map);
        } else {
            mActivity.finish();
        }
    }

    private void setHomeWatcher() {
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mLocationManager.stopUpdates(false);
            }

            @Override
            public void onHomeLongPressed() {
                Log.i("LongHomePressed", "not implemented");
            }
        });
        mHomeWatcher.startWatch();
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

        if (itemId == R.id.nav_map) {
            tag = Constants.MAPS_FRAGMENT;
            fragment = new MapsFragment();
//            intent = new Intent(mActivity, MapsActivity.class);
        } else if (itemId == R.id.nav_poi_list) {
            tag = Constants.POI_LIST_FRAGMENT;
            fragment = new LocationsListFragment();
        } else if (itemId == R.id.nav_settings) {
            tag = Constants.SETTINGS_FRAGMENT;
//            fragment = new SettingsFragment();
        } else if (itemId == R.id.nav_share) {
        } else if (itemId == R.id.nav_about) {
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
//        mTransaction.commit();
        mTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFragmentManager.getBackStackEntryCount() <= 1) {
                Util.openQuestionAlertDialog(mActivity, Util.getString(mContext, R.string.quit_message), true);
            } else {
                mFragmentManager.popBackStackImmediate();
            }
        }
    }

}
