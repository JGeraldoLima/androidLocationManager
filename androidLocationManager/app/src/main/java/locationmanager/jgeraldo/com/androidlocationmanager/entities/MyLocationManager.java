package locationmanager.jgeraldo.com.androidlocationmanager.entities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.listeners.NetworkLocationListener;
import locationmanager.jgeraldo.com.androidlocationmanager.listeners.SatelliteLocationListener;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public final class MyLocationManager implements GoogleApiClient.ConnectionCallbacks {

    private static Context mContext;

    private final Activity mActivity;

    private final LocationManager mLocationManager;

    private static com.google.android.gms.location.LocationListener mNetworkLocationListener;

    private static SatelliteLocationListener mSatteliteLocationListener;

    private static Location mLastKnownLocation;

    private static Location mSatelliteLocation;

    private static Location mNetworkLocation;

    private static GoogleApiClient mGoogleClient;

    private LocationRequest mLocationRequest;

    /**
     * The request time interval in milliseconds. The LocationManager will ask for
     * new updates every LOCATION_REQUEST_INTERVAL ms.
     **/
    private static final int LOCATION_REQUEST_INTERVAL = 1000;

    /**
     * The GPS sattelite updating state to control whether start GPS updates.
     * If it is already receiving updates, there is no need to ask again for it.
     **/
    private static boolean mGPSUpdatingState;

    /**
     * The network updating state to control whether start network updates.
     * If it is already receiving updates, there is no need to connect the client
     * and ask again for it.
     **/
    private static boolean mNetworkUpdatingState;

    /**
     * The minTime flag used by LocationManager's requestLocationUpdates method.
     **/
    private static final long LOCATION_MANAGER_MIN_TIME = 10;

    /**
     * The minDistance flag used by LocationManager's requestLocationUpdates method.
     **/
    private static final float LOCATION_MANAGER_MIN_DISTANCE = 0;

    /**
     * The minimum accuracy considered for a valid position.
     **/
    private static final float LOCATION_MANAGER_MIN_ACCURACY = 5;

    /**
     * The maximum accuracy considered for a valid position.
     **/
    private static final float LOCATION_MANAGER_MAX_ACCURACY = 20;

    /**
     * Max limit time in milliseconds to wait for valid coordinates.
     **/
    public static final int TIMER_TIMEOUT = 60000;

    /**
     * Timer tic-tac interval in milliseconds
     **/
    public static final int TIMER_TIMEOUT_INTERVAL = 1000;

    /**
     * Timer State flag to control it externally.
     * This is necessary due some bugs on it.
     **/
    private boolean timerState;

    public MyLocationManager(final Context context, final Activity activity) {
        mContext = context;
        mActivity = activity;
        timerState = false;

        mLocationManager = (LocationManager) mContext
            .getSystemService(Context.LOCATION_SERVICE);

        connectClient();
        setLocationListeners();
        instantiateLastKnowLocation();
        setLocationRequest();
    }

    private static void setLocationListeners() {
        mSatelliteLocation = new Location(LocationManager.GPS_PROVIDER);
        mSatelliteLocation.setLatitude(0);
        mSatelliteLocation.setLongitude(0);
        mSatelliteLocation.setAltitude(0);

        mSatteliteLocationListener = new SatelliteLocationListener();

        mNetworkLocation = new Location(LocationManager.NETWORK_PROVIDER);
        mNetworkLocation.setLatitude(0);
        mNetworkLocation.setLongitude(0);
        mNetworkLocation.setAltitude(0);

        mNetworkLocationListener = new NetworkLocationListener();
    }

    private void instantiateLastKnowLocation() {
        mLastKnownLocation = new Location(LocationManager.PASSIVE_PROVIDER);
        mLastKnownLocation.setLatitude(0);
        mLastKnownLocation.setLongitude(0);
        mLastKnownLocation.setAltitude(0);
    }

    public Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    public static void setLastKnownLocation(final Location lastKnownLocation) {
        mLastKnownLocation = lastKnownLocation;
    }

    public Location getSatelliteLocation() {
        return mSatelliteLocation;
    }

    public static void setSatelliteLocation(final Location satelliteLocation) {
        mSatelliteLocation = satelliteLocation;
    }

    public Location getNetworkLocation() {
        return mNetworkLocation;
    }

    public static void setNetworkLocation(final Location networkLocation) {
        mNetworkLocation = networkLocation;
    }

    public void setLastKnownLocation() {
        if (!Util.isPermissionsGranted(Constants.LOCATION_PERMISSION_CODES, mContext)) return;
        Location lastKnownGPS = getBetterLocationAux(mLocationManager
            .getLastKnownLocation(LocationManager.GPS_PROVIDER), mSatelliteLocation);
        Location lastKnownNetwork = getBetterLocationAux(mLocationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
            mNetworkLocation);
        if (lastKnownGPS != null && lastKnownNetwork != null) {
            setLastKnownLocation(getBetterLocationAux(lastKnownGPS,
                lastKnownNetwork));
        } else if (lastKnownGPS != null) {
            setLastKnownLocation(getBetterLocationAux(lastKnownGPS,
                mLastKnownLocation));
        } else if (lastKnownNetwork != null) {
            setLastKnownLocation(getBetterLocationAux(lastKnownNetwork,
                mLastKnownLocation));
        }
    }

    private void setLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
    }

    public Location getBetterLocation() {
        Location locationA = getBetterLocationAux(mSatelliteLocation,
            mNetworkLocation);
        Location locationB = getBetterLocationAux(mSatelliteLocation,
            mLastKnownLocation);
        Location tempLoc = getBetterLocationAux(locationA, locationB);

        if (!isLocationInvalid(tempLoc)) {
            setLastKnownLocation(tempLoc);
        }

        return tempLoc;
    }

    /**
     * Determines whether one Location reading is better than the current
     * Location fix.
     *
     * @param locationA the location a
     * @param locationB the location b
     * @return the better location aux
     */
    private static Location getBetterLocationAux(final Location locationA,
                                                 final Location locationB) {

        if (isLocationInvalid(locationA) && isLocationInvalid(locationB)) {
            return null;
        } else if (isLocationInvalid(locationA)
            && !isLocationInvalid(locationB)) {
            return locationB;
        } else if (!isLocationInvalid(locationA)
            && isLocationInvalid(locationB)) {
            return locationA;
        } else {
            long timeDelta = locationA.getTime() - locationB.getTime();
            boolean isNewer = timeDelta > 0;

            int accuracyDelta = (int) (locationA.getAccuracy() - locationB
                .getAccuracy());
            boolean isLessAccurate = accuracyDelta > LOCATION_MANAGER_MIN_ACCURACY;
            boolean isMoreAccurate = accuracyDelta < LOCATION_MANAGER_MIN_ACCURACY;
            boolean isSignificantlyLessAccurate = accuracyDelta
                > LOCATION_MANAGER_MAX_ACCURACY;

            if (isMoreAccurate) {
                return locationA;
            } else if (isNewer && !isLessAccurate) {
                return locationA;
            } else if (isNewer && !isSignificantlyLessAccurate) {
                return locationA;
            }
            return locationB;
        }
    }

    private void connectClient() {
        if (mGoogleClient == null) {
            mGoogleClient = new GoogleApiClient.Builder(mContext).addApi(
                LocationServices.API).build();
        }

        if (isNetworkProviderEnable()) {
            mGoogleClient.connect();
            mGoogleClient.registerConnectionCallbacks(this);
        }
    }

    public void disconnectClient() {
        mGoogleClient.disconnect();
    }

    public void startLocationUpdatesByPrecisionStatus() {
        if (isGPSProviderEnable() && isNetworkProviderEnable()) {
            startUpdates(true, true);
        } else if (!isGPSProviderEnable() && isNetworkProviderEnable() && !isNetworkLocationUpdating()) {
            startUpdates(false, true);
        } else if (isGPSProviderEnable() && !isNetworkProviderEnable() && !isGPSLocationUpdating()) {
            startUpdates(true, false);
        }
        setLastKnownLocation();
    }

    public void startUpdates(final boolean useGPS, final boolean useNetwork) {
        if (Util.isPermissionsGranted(Constants.LOCATION_PERMISSION_CODES, mContext)) {
            if (useGPS) {
                Log.e("START", "GPS");
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_MANAGER_MIN_TIME,
                    LOCATION_MANAGER_MIN_DISTANCE, mSatteliteLocationListener);
                setGPSUpdatingState(true);
            }

            if (useNetwork) {
                Log.e("START", "NETWORK");
                if (!isLocationClientConnected()) {
                    connectClient();
                }
            }
        }
    }

    public void stopUpdates(final boolean clearData) {
        if (!Util.isPermissionsGranted(Constants.LOCATION_PERMISSION_CODES, mContext)) return;
        mLocationManager.removeUpdates(mSatteliteLocationListener);
        setGPSUpdatingState(false);

        if (isLocationClientConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleClient, mNetworkLocationListener);
            disconnectClient();
            setNetworkUpdatingState(false);
        }

        if (clearData) {
            setLatitude(0);
            setLongitude(0);
            setAltitude(0);
        }
    }

    public double getLatitude() {
        return mSatelliteLocation.getLatitude();
    }

    public double getLongitude() {
        return mSatelliteLocation.getLongitude();
    }

    public double getAltitude() {
        return mSatelliteLocation.getAltitude();
    }

    public void setLatitude(final double latitude) {
        mSatelliteLocation.setLatitude(latitude);
    }

    public void setLongitude(final double longitude) {
        mSatelliteLocation.setLongitude(longitude);
    }

    public void setAltitude(final double altitude) {
        mSatelliteLocation.setAltitude(altitude);
    }

//    public void openLocationPrecisionConfigDialog(final Activity activity,
//                                                  final Intent intentToStart, final AsyncTask<Void, Void, Void> task,
//                                                  final boolean finishActivity) {
//        final Dialog alertDialog = new Dialog(activity);
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.setContentView(R.layout.dialog_sentence);
//
//        TextView tvTitle = (TextView) alertDialog.findViewById(R.id.tvTitleDialogSentence);
//        tvTitle.setText(Util.getString(activity,
//                R.string.enableGPSforMoreAccuracyTitle));
//
//        TextView tvMessage = (TextView) alertDialog.findViewById(R.id.tvMessageDialogSentence);
//        tvMessage.setText(Util.getString(mContext, R.string.enableGPSforMoreAccuracy));
//
//        Button btOk = (Button) alertDialog.findViewById(R.id.btOkDialogSentence);
//        btOk.setText(Util.getString(mContext, R.string.positiveSentence));
//        btOk.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                final Intent i = new Intent(
//                        Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(
//                        Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(i);
//                alertDialog.dismiss();
//            }
//        });
//
//        Button btCancel = (Button) alertDialog.findViewById(R.id.btCancelDialogSentence);
//        btCancel.setText(Util.getString(mContext, R.string.negativeSentence));
//        btCancel.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                if (intentToStart == null) {
//                    Log.i("AccuracyWarning", "Intent is null, preparing for execute task");
//                } else {
//                    mActivity.startActivity(intentToStart);
//                    alertDialog.dismiss();
//                }
//
//                if (task == null) {
//                    Log.i("AccuracyWarning", "Task is null, intent started");
//                } else {
//                    alertDialog.dismiss();
//                    task.execute();
//                }
//
//                if (finishActivity) {
//                    activity.finish();
//                    alertDialog.dismiss();
//                }
//            }
//        });
//
//        alertDialog.show();
//    }

    public void openEnableLocationServicesDialog(final Activity activity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
            .title(R.string.notice)
            .content(R.string.enable_location_services)
            .positiveColorRes(R.color.colorPrimaryDark)
            .negativeColorRes(R.color.colorPrimaryDark)
            .positiveText("OK")  // TODO: waiting for @afollestad fix on MaterialDialogs lib
            .negativeText(R.string.cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    final Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(i);
                    dialog.dismiss();
                }
            })
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    activity.finish();
                    dialog.dismiss();
                }
            });

        MaterialDialog alertDialog = builder.build();
        alertDialog.show();
    }

    public boolean checkLocationServicesStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;

            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            String locationProviders = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public boolean isGPSProviderEnable() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkProviderEnable() {
        return mLocationManager
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isLocationInvalid(final Location location) {
        final boolean isNull = location == null;
        if (isNull) {
            return isNull;
        } else {
            final boolean isZero = location.getLatitude() == 0.0
                && location.getLongitude() == 0.0;
            return isNull || isZero;
        }
    }

    private boolean isGPSLocationUpdating() {
        return mGPSUpdatingState;
    }

    private boolean isNetworkLocationUpdating() {
        return mNetworkUpdatingState;
    }

    void setGPSUpdatingState(final boolean state) {
        mGPSUpdatingState = state;
    }

    private void setNetworkUpdatingState(final boolean state) {
        mNetworkUpdatingState = state;
    }

    public boolean getTimerState() {
        return timerState;
    }

    public void setTimerState(final boolean state) {
        timerState = state;
    }

    private static boolean isLocationClientConnected() {
        return mGoogleClient.isConnected();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setNetworkUpdatingState(true);
        new NetworkConnection().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: remove code coupling between this manager and other classes like Util
        Util.showSnackBar(mActivity, Util.getString(mContext, R.string.google_client_not_connected));
    }

    private class NetworkConnection extends AsyncTask<Void, Void, Void> {

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        protected Void doInBackground(final Void... params) {
            while (!isLocationClientConnected()) {
                //Log.i("GooglePlayServices", "doInBackGround");
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
            Log.i("NETWORK", "CONNECTED");
            if (!Util.isPermissionsGranted(Constants.LOCATION_PERMISSION_CODES, mContext)) return;
            LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleClient, mLocationRequest,
                    mNetworkLocationListener);
        }
    }
}