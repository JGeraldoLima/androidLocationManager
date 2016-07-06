package locationmanager.jgeraldo.com.androidlocationmanager.entities;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import locationmanager.jgeraldo.com.androidlocationmanager.listeners.NetworkLocationListener;
import locationmanager.jgeraldo.com.androidlocationmanager.listeners.SatelliteLocationListener;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.Preferences;

public final class MyLocationManager {

    private static Context mContext;

    private final Activity mActivity;

    private final LocationManager mLocationManager;

    private static com.google.android.gms.location.LocationListener mNetworkLocationListener;

    private static SatelliteLocationListener mSatteliteLocationListener;

    private static Location mLastKnownLocation;

    private static Location mSatelliteLocation;

    private static Location mNetworkLocation;

    private static GoogleApiClient mLocationClient;

    private LocationRequest mLocationRequest;

    private static final int LOCATION_REQUEST_INTERVAL = 1000;

    private static boolean mGPSUpdaterState;

    private static boolean mNetworkUpdaterState;

    private static final long LOCATION_MANAGER_MIN_TIME = 10;

    private static final float LOCATION_MANAGER_MIN_ACCURACY = 5;

    private static final float LOCATION_MANAGER_MAX_ACCURACY = 20;

    private static final float LOCATION_MANAGER_MIN_DISTANCE = 0;

    public static final int GPS_TIMEOUT = 60000;

    public static final int GPS_TIMEOUT_INTERVAL = 1000;

    private boolean timeoutFinished;

    public MyLocationManager(final Context context, final Activity activity) {
        mContext = context;
        mActivity = activity;
        timeoutFinished = false;

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
        if (!Preferences.getLocationPermissionsGrantFlag(mContext)) return;
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

    public void connectClient() {
        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(mContext).addApi(
                    LocationServices.API).build();
        }

        if (isNetworkEnable()) {
            mLocationClient.connect();
        }
    }

    public void disconnectClient() {
        mLocationClient.disconnect();
    }

    public void checkLocationServicesStatus() {
        if (isGPSEnable() && isNetworkEnable()) {
            startUpdates(true, true);
        } else if (!isGPSEnable() && isNetworkEnable() && !isNetworkLocationUpdating()) {
            startUpdates(false, true);
        } else if (isGPSEnable() && !isNetworkEnable() && !isGPSLocationUpdating()) {
            startUpdates(true, false);
        }
        setLastKnownLocation();
    }

    public void startUpdates(final boolean useGPS, final boolean useNetwork) {
        if (!Preferences.getLocationPermissionsGrantFlag(mContext)) return;
        if (useGPS) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_MANAGER_MIN_TIME,
                    LOCATION_MANAGER_MIN_DISTANCE, mSatteliteLocationListener);
            setGPSUpdatingStatus(true);
        }

        if (useNetwork) {
            if (!isLocationClientConnected()) {
                connectClient();
            }
            setNetworkUpdatingStatus(true);
            new NetworkConnection().execute();
        }
    }

    public void stopUpdates(final boolean clearData) {
        if (!Preferences.getLocationPermissionsGrantFlag(mContext)) return;
        mLocationManager.removeUpdates(mSatteliteLocationListener);
        setGPSUpdatingStatus(false);

        if (isLocationClientConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mLocationClient, mNetworkLocationListener);
            disconnectClient();
            setNetworkUpdatingStatus(false);
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

//    public void dialogPositionAccuracyWarning(final Activity activity,
//                                              final Intent intentToStart, final AsyncTask<Void, Void, Void> task,
//                                              final boolean finishActivity) {
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

//    public void dialogLocationServices(final Activity activity) {
//        final Dialog alertDialog = new Dialog(activity);
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.setContentView(R.layout.dialog_sentence);
//
//        TextView tvTitle = (TextView) alertDialog.findViewById(R.id.tvTitleDialogSentence);
//        tvTitle.setText(Util.getString(mContext,
//                R.string.enableLocationServicesTitle));
//
//        TextView tvMessage = (TextView) alertDialog.findViewById(R.id.tvMessageDialogSentence);
//        tvMessage.setText(Util.getString(mContext,
//                R.string.enableLocationServices));
//
//        Button btOk = (Button) alertDialog.findViewById(R.id.btOkDialogSentence);
//        btOk.setText(Util.getString(mContext, R.string.positiveSentence));
//        btOk.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                final Intent i = new Intent(
//                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                activity.startActivity(i);
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
//                Log.i("Negative onClick dialog", "not implemented");
//                alertDialog.dismiss();
//            }
//        });
//
//        alertDialog.show();
//    }

//    public void dialogInvalidPosition(final Context context, final Activity activity,
//                                      final boolean finishActivity) {
//        final Dialog alertDialog = new Dialog(activity);
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.setContentView(R.layout.dialog_warning);
//
//        TextView tvTitle = (TextView) alertDialog.findViewById(R.id.tvTitleDialogWarning);
//        tvTitle.setText(Util
//                .getString(context, R.string.invalidPositionTitle));
//
//        TextView tvMessage = (TextView) alertDialog.findViewById(R.id.tvMessageDialogWarning);
//        tvMessage.setText(Util.getString(mActivity.getApplicationContext(),
//                R.string.invalidPosition));
//
//        Button btOk = (Button) alertDialog.findViewById(R.id.btOkDialogWarning);
//        btOk.setText(Util.getString(mContext, R.string.ok));
//        btOk.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(final View v) {
//                setTimeoutFinished(false);
//                if (finishActivity) {
//                    activity.finish();
//                }
//                alertDialog.dismiss();
//            }
//        });
//
//        alertDialog.show();
//    }

    public boolean isGPSEnable() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkEnable() {
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

    public boolean isGPSLocationUpdating() {
        return mGPSUpdaterState;
    }

    public boolean isNetworkLocationUpdating() {
        return mNetworkUpdaterState;
    }

    public static void setGPSUpdatingStatus(final boolean state) {
        mGPSUpdaterState = state;
    }

    public static void setNetworkUpdatingStatus(final boolean state) {
        mNetworkUpdaterState = state;
    }

    public boolean isTimeoutFinished() {
        return timeoutFinished;
    }

    public void setTimeoutFinished(final boolean state) {
        timeoutFinished = state;
    }

    public static boolean isLocationClientConnected() {
        return mLocationClient.isConnected();
    }

    private class NetworkConnection extends AsyncTask<Void, Void, Void> {

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
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
            if (!Preferences.getLocationPermissionsGrantFlag(mContext)) return;
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mLocationClient, mLocationRequest,
                            mNetworkLocationListener);
        }
    }
}