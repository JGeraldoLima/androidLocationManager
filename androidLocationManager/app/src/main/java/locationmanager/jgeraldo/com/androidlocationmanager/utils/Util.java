package locationmanager.jgeraldo.com.androidlocationmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.Preferences;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments.MapsFragment;

public final class Util {

    private static MyLocationManager gpsManager;

    public static int mNearbyPointsCounter = 0;

    private Util() {
    }

    public static void initGPSManager(final Context context, final Activity activity) {
        gpsManager = new MyLocationManager(context, activity);
    }

    public static String getString(final Context context, final int id) {
        return context.getResources().getString(id);
    }

    public static MyLocationManager getLocationManager() {
        return gpsManager;
    }

    public static boolean checkPermissions(Activity activity, String permissionKey, String[] permissions,
                                           int requestCode, Fragment fragment) {
        Context context = activity.getApplicationContext();
        if (!isPermissionsGranted(permissions, context)) {
            // TODO: VERIFY IMPLEMENTATION FOR OLDER VERSIONS - IS IT NECESSARY?
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (fragment != null) {
                    fragment.requestPermissions(permissions, requestCode);
                } else {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /* We need to check on this way (instead of set flag values on Preferences) because once the user erase the data
       on Apps Configuration menu, the flag value stored on Preferences may be invalid and all the verifications we
       do with it too.
    */
    public static boolean isPermissionsGranted(String[] permissions, Context context) {
        for (String permission : permissions) {
            int granted = context.checkCallingOrSelfPermission(permission);
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Log.e("PERMISSON GRANTED", permission);
        }
        return true;
    }


//    public static void checkLocationsPrecisionConfig(Activity mActivity){
//        if (!gpsManager.isGPSProviderEnable() && gpsManager.isNetworkProviderEnable()) {
//            gpsManager
//                .openLocationPrecisionConfigDialog(mActivity, intent, null, finishActivity);
//        } else if (!gpsManager.isGPSProviderEnable()
//            && !gpsManager.isNetworkProviderEnable()) {
//            gpsManager.openEnableLocationServicesDialog(activity);
//        }
//    }

    public static void onRequestPermissionsResult(Activity mActivity, Context mContext, int requestCode,
                                                  int[] grantResults) {
        switch (requestCode) {
            case Constants.LOCATION_PERMISSIONS_CODE: {
                if (grantResults.length > 0 && ArrayUtils.contains(grantResults, PackageManager.PERMISSION_GRANTED)) {
                    Preferences.setPermissionGrantFlag(mContext, Constants.LOCATION_PERMISSIONS_FLAG, true);
                    gpsManager.startLocationUpdatesByPrecisionStatus();
                    // TODO: find a proper way to call MapsFragment.GetCurrentCoordinates. Would be even better if it
                    // turns to a generic mechanism, so for each request code we could execute something (it's during
                    // these times I miss javascript so much :'( )
                    // TIP: maybe an AsyncTask lib could help me?
                } else {
                    showSnackBar(mActivity, getString(mContext, R.string.locations_permission_denied_msg));
                }
                break;
            }
        }
    }

    public static void showSnackBar(Activity activity, String msgToshow) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), msgToshow, Snackbar.LENGTH_LONG)
            .setAction("Action", null);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(activity.getResources().getColor(android.R.color.white));
        textView.setMaxLines(4);

        snackbar.show();
    }

    public static void openAlertDialog(final Activity activity, String msgToShow, final boolean finishActivity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
            .title(R.string.notice)
            .content(msgToShow)
            .positiveColorRes(R.color.colorPrimaryDark)
            .positiveText("OK")  // TODO: waiting for @afollestad fix on MaterialDialogs lib
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (finishActivity) {
                        activity.finish();
                    }
                    dialog.dismiss();
                }
            });

        MaterialDialog alertDialog = builder.build();
        alertDialog.show();
    }

    public static void openQuestionAlertDialog(final Activity activity, String msgToShow, final boolean finishActivity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
            .title(R.string.notice)
            .content(msgToShow)
            .positiveColorRes(R.color.colorPrimaryDark)
            .negativeColorRes(R.color.colorPrimaryDark)
            .positiveText("OK") // TODO: waiting for @afollestad fix on MaterialDialogs lib
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (finishActivity) {
                        gpsManager.stopUpdates(false);
                        activity.finish();
                    }
                    dialog.dismiss();
                }
            })
            .negativeText(R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            });

        MaterialDialog alertDialog = builder.build();
        alertDialog.show();
    }

    public static void openEnableLocationServicesDialog(final Activity activity) {
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

    public static boolean isConnected(final Context context) {
        boolean connected;
        final ConnectivityManager conectivtyManager = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
            && conectivtyManager.getActiveNetworkInfo().isAvailable()
            && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        } else {
            connected = false;
        }
        return connected;
    }

    public static float distanceTo(MyLocation current, MyLocation to) {
        Location currentLoc = new Location(LocationManager.PASSIVE_PROVIDER);
        currentLoc.setLatitude(current.getLatitude());
        currentLoc.setLongitude(current.getLongitude());

        Location toLoc = new Location(LocationManager.PASSIVE_PROVIDER);
        toLoc.setLatitude(to.getLatitude());
        toLoc.setLongitude(to.getLongitude());

        return currentLoc.distanceTo(toLoc);
    }


    public static void getDirections(double userLatitude, double userLongitude, Double destinyLatitude, Double destinyLongitude) {
        new RouteCalculateTask(userLatitude, userLongitude, destinyLatitude, destinyLongitude).execute();
    }

    private static class RouteCalculateTask
        extends AsyncTask<Void, Void, Void> {

        private static JSONObject jsonLegs;
        private static JSONArray steps;

        private static String urlRoute;
        private URL mFeedURL;
        private static double mUserLatitude, mUserLongitude,
            pointLatitude, pointLongitude;

        public RouteCalculateTask(double userLatitude, double userLongitude, Double destinyLatitude, Double destinyLongitude) {
            mUserLatitude = userLatitude;
            mUserLongitude = userLongitude;
            pointLatitude = destinyLatitude;
            pointLongitude = destinyLongitude;
        }


        /**
         * Do in background.
         *
         * @param params the params
         * @return the void
         */
        @Override
        protected Void doInBackground(final Void... params) {

            // get device location
            this.urlRoute = String.format(Locale.US,
                "http://maps.googleapis.com/maps/api/"
                    + "directions/json?origin="
                    + "%f,%f&destination=%f,%f"
                    + "&sensor=true&mode=walking",
                mUserLatitude, mUserLongitude,
                pointLatitude, pointLongitude);
            try {
                this.mFeedURL = new URL(urlRoute);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                jsonLegs();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                steps = jsonLegs.getJSONArray("steps");
            } catch (JSONException e) {
                e.printStackTrace();
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
        }

        private void jsonLegs() throws JSONException, IOException {
            JSONObject json;
            json = new JSONObject(convertStreamToString(this.mFeedURL.openConnection()
                .getInputStream()));

            Log.e("__json", json.toString());

            final JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);
            jsonLegs = jsonRoute.getJSONArray("legs").getJSONObject(0);
        }
    }

    public static String convertStreamToString(final InputStream input)//igual ao
        throws IOException {

        if (input == null) {
            return "";
        } else {
            final Writer writer = new StringWriter();

            final char[] buffer = new char[Constants.BUFFER_SIZE];
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(
                    input, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                input.close();
            }
            return writer.toString();
        }
    }

}