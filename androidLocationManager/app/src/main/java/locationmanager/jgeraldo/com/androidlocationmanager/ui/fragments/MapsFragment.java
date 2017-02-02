package locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.LocationSearchTimer;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.Preferences;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.RealmUtil;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    public Context mContext;

    private FragmentActivity mActivity;

    private MyLocationManager mLocationManager;

    private Toolbar mToolbar;

    private static FragmentManager mFragmentManager;

    private static Fragment mFragment;

    private FabSpeedDial mFabAdd;

    private FabSpeedDial mFabTypes;

    private View view;

    private GoogleMap mMap;

    private Marker currentMarker;

    private static ArrayList<Marker> mMarkers = new ArrayList<>();

    private MarkerOptions currentMarkerOptions;

    private LatLng currentMarkerLocation;

    private BitmapDescriptor myLocationIcon;

    private static LatLngBounds.Builder builder;

    public MapsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = this;
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);

        mFragmentManager = getChildFragmentManager();
        mLocationManager = Util.getLocationManager();

        Util.checkPermissions(mActivity, Constants.LOCATION_PERMISSIONS_FLAG, Constants.LOCATION_PERMISSION_CODES,
            Constants.LOCATION_PERMISSIONS_CODE, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLocationManager != null
            && Preferences.getPermissionGrantFlag(Constants.LOCATION_PERMISSIONS_FLAG, mContext)) {

            mLocationManager.checkLocationServicesStatus();

            if (Preferences.getUserLatitudePos(mContext) == -1
                && Preferences.getUserLongitudePos(mContext) == -1) {
                new GetCurrentCoordinates().execute();
            } else {
                currentMarkerLocation = new LatLng(Preferences.getUserLatitudePos(mContext), Preferences.getUserLongitudePos(mContext));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mToolbar.setTitle(R.string.nav_map);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);

            setViews();
            setUpMap();
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Util.onRequestPermissionsResult(mActivity, mContext, requestCode, grantResults);
    }

    @Override
    public void onDestroyView() {
        if (mLocationManager != null) {
            mLocationManager.stopUpdates(false);
        }
        RealmUtil.close();
        super.onDestroyView();
    }

    private void setViews() {
        mFabAdd = (FabSpeedDial) view.findViewById(R.id.fab_map_add_options);
        mFabAdd.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.ic_save_current_location) {
                    openNewLocationNameChoosingDialog();
                    return true;
                } else if (itemId == R.id.ic_goto_my_position) {
                    if (!Preferences.getPermissionGrantFlag(Constants.LOCATION_PERMISSIONS_FLAG, mContext)) {
                        Util.checkPermissions(mActivity, Constants.LOCATION_PERMISSIONS_FLAG, Constants.LOCATION_PERMISSION_CODES,
                            Constants.LOCATION_PERMISSIONS_CODE, mFragment);
                    } else {
                        new GetCurrentCoordinates().execute();
                    }
                    return true;
                }
                return false;
            }
        });

        mFabTypes = (FabSpeedDial) view.findViewById(R.id.fab_map_type_options);
        mFabTypes.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                int map_type;

                if (itemId == R.id.ic_change_map_to_normal) {
                    map_type = GoogleMap.MAP_TYPE_NORMAL;
                } else if (itemId == R.id.ic_change_map_to_terrain) {
                    map_type = GoogleMap.MAP_TYPE_TERRAIN;
                } else {
                    map_type = GoogleMap.MAP_TYPE_SATELLITE;
                }

                mMap.setMapType(map_type);
                return true;
            }
        });

    }

    private void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) mFragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void updateMapPosition() {
        if (currentMarker != null) {
            currentMarker.remove();
        }

        Preferences.setUserLatitudePos(mContext, currentMarkerLocation.latitude);
        Preferences.setUserLongitudePos(mContext, currentMarkerLocation.longitude);
        currentMarkerOptions = new MarkerOptions()
            .position(currentMarkerLocation)
            .title(Util.getString(mContext, R.string.current_location))
            .icon(myLocationIcon);
        currentMarker = mMap.addMarker(currentMarkerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMarkerLocation, Constants.DEFAULT_MAP_ZOOM));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location);

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);

        if (currentMarkerLocation != null) {
            builder = new LatLngBounds.Builder();
            builder.include(currentMarkerLocation);

            // TODO: change it to get live GPS position instead
            currentMarkerOptions = new MarkerOptions().position(currentMarkerLocation).title(Util.getString(mContext, R.string.current_location)).icon(myLocationIcon);
            currentMarker = mMap.addMarker(currentMarkerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMarkerLocation, Constants.DEFAULT_MAP_ZOOM));

            loadDatabaseLocationsOnMap();
        }

    }

    @Override
    public void onMapClick(LatLng point) {
        currentMarkerLocation = point;
        updateMapPosition();
    }

    private void loadDatabaseLocationsOnMap() {
        // REMOVE ALL POINTS BEFORE CONTINUE?
        List<MyLocation> storedLocations = RealmUtil.getAllLocations();
        for (MyLocation l : storedLocations) {
            BitmapDescriptor renterLocationBitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_location);
            createRenterMarkerOnMap(renterLocationBitmap, l);
        }

        try {
            updateMapCameraZoom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMapCameraZoom() {
        final LatLngBounds bounds = builder.build();
        final CameraUpdate camUp = CameraUpdateFactory.newLatLngBounds(
            bounds, 80);
        mMap.animateCamera(camUp);
    }

    private void createRenterMarkerOnMap(BitmapDescriptor markerIcon, MyLocation location) {
        Marker marker = mMap.addMarker(new MarkerOptions()
            .position(new LatLng(location.getLatitude(), location.getLongitude()))
            .title(location.getName()).icon(markerIcon));
        addMarkerToMap(marker);
    }

    private void addMarkerToMap(Marker marker) {
        // TODO: verify a better way to do this check
        if (mMarkers.contains(marker)) {
            mMarkers.remove(marker);
        }
        mMarkers.add(marker);

        builder = new LatLngBounds.Builder();
        builder.include(currentMarkerLocation);

        for (Marker mark : mMarkers) {
            builder.include(mark.getPosition());
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (currentMarker.equals(marker)) {
                    currentMarker.showInfoWindow();
                } else {
                    for (int i = 0; i < mMarkers.size(); i++) {
                        if (mMarkers.get(i).equals(marker)) {
                            marker.showInfoWindow();
//                            openItemOptionsBottomDialog(mRenters.get(i), marker);
                            break;
                        }
                    }
                }
                return true;
            }

        });
    }

    public void openLocationConnectionTimeout() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity)
            .title(R.string.notice)
            .content(Util.getString(mContext, R.string.location_search_timeout))
            .positiveColorRes(R.color.colorPrimaryDark)
            .positiveText(R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Location betterLocation = mLocationManager
                        .getBetterLocation();
                    if (MyLocationManager.isLocationInvalid(betterLocation)) {
                        Util.openAlertDialog(mActivity, Util.getString(mContext, R.string.invalid_position), false);
                        dialog.dismiss();
                    } else {
                        currentMarkerLocation = new LatLng(betterLocation.getLatitude(), betterLocation.getLongitude());
                        updateMapPosition();
                    }
                }
            })
            .negativeColorRes(R.color.colorPrimaryDark)
            .negativeText(R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mLocationManager.setTimerState(false);
                    dialog.dismiss();
                }
            });

        MaterialDialog alertDialog = builder.build();
        alertDialog.show();
    }

    private void openNewLocationNameChoosingDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity)
            .title(R.string.newlocation_name_choosing_dialog_title)
            .content(R.string.newlocation_name_choosing_dialog_content)
            .positiveText(R.string.save)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // If flow is passing here, it means that the Save button is unlocked
                    // and there are no errors.
                    String name = dialog.getInputEditText().getText().toString();
                    MyLocation location = new MyLocation(name,
                        currentMarkerLocation.latitude, currentMarkerLocation.longitude);
                    RealmUtil.createNewLocation(location);
                    loadDatabaseLocationsOnMap();
                }
            })
            .negativeText(R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .alwaysCallInputCallback()
            .input(R.string.newlocation_name_choosing_dialog_hint, 0, false,
                new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        String name = input.toString();
                        if (RealmUtil.locationNameAlreadyExists(name)) {
                            dialog.getInputEditText().setError(Util.getString(mContext, R.string.newlocation_existing_name_error));
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        } else {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        }
                    }
                });

        MaterialDialog alertDialog = builder.build();
        alertDialog.show();
    }

    private class GetCurrentCoordinates extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        private final String progressMessage = Util.getString(mContext, R.string.getting_current_location);

        private LocationSearchTimer mTimeout;

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(progressMessage);
            progressDialog.show();
            mTimeout = new LocationSearchTimer(mContext, this, progressDialog,
                progressMessage, MyLocationManager.TIMER_TIMEOUT,
                MyLocationManager.TIMER_TIMEOUT_INTERVAL, true);
            mTimeout.start();
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(final Void... params) {

            if (mLocationManager.isGPSEnable()) {
                Log.e("CAPTURE", "BY BOTH");
                while (MyLocationManager.isLocationInvalid(
                    mLocationManager.getSatelliteLocation())
                    && !mLocationManager.getTimerState() && !isCancelled()) {
//                    Log.i("Location", "SEARCHING LOCATION");
                }
            } else {
                Log.e("CAPTURE", "BY NETWORK");
                while (MyLocationManager
                    .isLocationInvalid(mLocationManager.getNetworkLocation())
                    && !mLocationManager.getTimerState() && !isCancelled()) {
//                    Log.i("Location", "SEARCHING LOCATION");
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);

            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e("Map", e.getMessage());
            }

            mTimeout.cancel();

            Location betterLocation = mLocationManager.getBetterLocation();

            if (MyLocationManager
                .isLocationInvalid(mLocationManager.getSatelliteLocation())) {
                if (MyLocationManager
                    .isLocationInvalid(mLocationManager.getNetworkLocation())) {
                    Location lastKnowLoc = mLocationManager.getLastKnownLocation();
                    if (MyLocationManager
                        .isLocationInvalid(lastKnowLoc)) {
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                openLocationConnectionTimeout();
                            }
                        });
                    } else {
                        currentMarkerLocation = new LatLng(lastKnowLoc.getLatitude(), lastKnowLoc.getLongitude());
                        updateMapPosition();
                    }

                } else {
                    currentMarkerLocation = new LatLng(betterLocation.getLatitude(), betterLocation.getLongitude());
                    updateMapPosition();
                }
            } else {
                Location satteliteLocation = mLocationManager.getSatelliteLocation();
                currentMarkerLocation = new LatLng(satteliteLocation.getLatitude(), satteliteLocation.getLongitude());
                updateMapPosition();
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onCancelled()
         */
        @Override
        protected void onCancelled() {
            mTimeout.cancel();
            mLocationManager.setTimerState(false);

            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("Map", e.getMessage());
            }

            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    openLocationConnectionTimeout();
                }
            });
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onCancelled(java.lang.Object)
         */
        @Override
        protected void onCancelled(final Void result) {
            mTimeout.cancel();
            mLocationManager.setTimerState(false);
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    openLocationConnectionTimeout();
                }
            });
        }
    }
}
