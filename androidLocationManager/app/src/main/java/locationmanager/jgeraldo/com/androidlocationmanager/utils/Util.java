package locationmanager.jgeraldo.com.androidlocationmanager.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
import java.util.Locale;

import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;

public final class Util {

    private static MyLocationManager gpsManager;

    private static boolean locationPermissionsGranted = false;

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

    public static void grantLocationPermissions() {
        //atualizar Preferences
        locationPermissionsGranted = true;
    }

    public static void checkLocationPermissions(Activity activity) {
        if (!Util.isLocationPermissionsGranted()) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.LOCATION_PERMISSIONS_CODE);
        }
    }

    public static boolean isLocationPermissionsGranted() {
        return locationPermissionsGranted;
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

            // pegar locale do device
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