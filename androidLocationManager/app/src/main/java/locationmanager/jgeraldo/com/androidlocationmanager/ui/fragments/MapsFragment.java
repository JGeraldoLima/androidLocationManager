package locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.GPSCountDownTimeout;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.Preferences;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    public static Context mContext;

    private static FragmentActivity mActivity;

    private MyLocationManager mLocationManager;

    private static Toolbar mToolbar;

    private static FragmentManager mFragmentManager;

    private static FabSpeedDial mFabAdd;

    private View view;

    private GoogleMap mMap;

    private Marker currentMarker;

    private MarkerOptions currentMarkerOptions;

    private LatLng currentMarkerLocation = new LatLng(-7.2190974, -35.903685);

    private BitmapDescriptor myLocation;

    public MapsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);

        mFragmentManager = getChildFragmentManager();
        mLocationManager = Util.getLocationManager();
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

    private void setViews() {
        mFabAdd = (FabSpeedDial) view.findViewById(R.id.fab_map);
        mFabAdd.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.ic_save_current_location) {
                    Util.showSnackBar(mActivity, "Location: "
                        + currentMarkerLocation.latitude
                        + "; " + currentMarkerLocation.longitude);
                    return true;
                } else if (itemId == R.id.ic_goto_my_position) {
                    if (!Preferences.getLocationPermissionsGrantFlag(mContext)) {
                        Util.checkLocationPermissions(mActivity);
                    } else {
                        new GetCurrentCoordinates().execute();
                    }
                    return true;
                }
                return false;
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

        currentMarkerOptions = new MarkerOptions()
            .position(currentMarkerLocation)
            .title(Util.getString(mContext, R.string.current_location))
            .icon(myLocation);
        currentMarker = mMap.addMarker(currentMarkerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMarkerLocation, Constants.DEFAULT_MAP_ZOOM));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myLocation = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location);
        currentMarkerOptions = new MarkerOptions().position(currentMarkerLocation).title(Util.getString(mContext, R.string.default_location)).icon(myLocation);

        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);
        currentMarker = mMap.addMarker(currentMarkerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMarkerLocation, Constants.DEFAULT_MAP_ZOOM));
    }

    @Override
    public void onMapClick(LatLng point) {
        currentMarkerLocation = point;
        updateMapPosition();
    }

//    public void dialogGPSConnection() {
//        final Dialog alertDialog = new Dialog(mActivity);
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.setContentView(R.layout.dialog_sentence);
//
//        TextView tvTitle = (TextView) alertDialog.findViewById(R.id.tvTitleDialogSentence);
//        tvTitle.setText(Util.getString(mContext, R.string.gps));
//
//        TextView tvMessage = (TextView) alertDialog.findViewById(R.id.tvMessageDialogSentence);
//        tvMessage.setText(Util.getString(mActivity.getApplicationContext(),
//                R.string.gpsTimeout));
//
//        Button btOk = (Button) alertDialog.findViewById(R.id.btOkDialogSentence);
//        btOk.setText(Util.getString(mContext, R.string.positiveSentence));
//        btOk.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                Location betterLocation = mLocationManager
//                        .getBetterLocation();
//                if (MyLocationManager.isLocationInvalid(betterLocation)) {
//                    mLocationManager.dialogInvalidPosition(mContext, mActivity, false);
//                    alertDialog.dismiss();
//                } else {
//                    //seta posicao no mapa
//                    mLocationManager.setTimeoutFinished(false);
//                    alertDialog.dismiss();
//                }
//            }
//        });
//
//        Button btCancel = (Button) alertDialog.findViewById(R.id.btCancelDialogSentence);
//        btCancel.setText(Util.getString(mContext, R.string.negativeSentence));
//        btCancel.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                mLocationManager.setTimeoutFinished(false);
//                alertDialog.dismiss();
//            }
//        });
//
//        alertDialog.show();
//    }

    private class GetCurrentCoordinates extends AsyncTask<Void, Void, Void> {

        /**
         * The progress dialog.
         */
        private ProgressDialog progressDialog;

        /**
         * The progress message.
         */
        private final String progressMessage = Util.getString(mContext, R.string.getting_current_location);

        /**
         * The m timeout.
         */
        private GPSCountDownTimeout mTimeout;

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
            mTimeout = new GPSCountDownTimeout(mContext, this, progressDialog,
                progressMessage, MyLocationManager.GPS_TIMEOUT,
                MyLocationManager.GPS_TIMEOUT_INTERVAL, true);
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
                    && !mLocationManager.isTimeoutFinished() && !isCancelled()) {
                    Log.i("Location", "SEARCHING LOCATION");
                }
            } else {
                Log.e("CAPTURE", "BY NETWORK");
                while (MyLocationManager
                    .isLocationInvalid(mLocationManager.getNetworkLocation())
                    && !mLocationManager.isTimeoutFinished() && !isCancelled()) {
                    Log.i("Location", "SEARCHING LOCATION");
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
//                                dialogGPSConnection();
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
            mLocationManager.setTimeoutFinished(false);

            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("Map", e.getMessage());
            }

            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    //dialogGPSConnection();
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
            mLocationManager.setTimeoutFinished(false);
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    //dialogGPSConnection();
                }
            });
        }
    }
}
