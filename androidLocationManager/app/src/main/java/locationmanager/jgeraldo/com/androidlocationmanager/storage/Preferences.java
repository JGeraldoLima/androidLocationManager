package locationmanager.jgeraldo.com.androidlocationmanager.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;

@SuppressLint("CommitPrefEdits")
public class Preferences {

    private static SharedPreferences loggedUserPreferences;

    private static SharedPreferences.Editor loggedUserPreferenvesEditor;

    private static void initiatePreferencesIfNull(Context context) {
        if (loggedUserPreferences == null) {
            loggedUserPreferences = context.getSharedPreferences(
                    Constants.PREFS_NAME, 0);
            loggedUserPreferenvesEditor = loggedUserPreferences.edit();
        }
    }

    private static void saveChanges() {
        loggedUserPreferenvesEditor.commit();
        loggedUserPreferenvesEditor.apply();
    }

    public static boolean getLocationPermissionsGrantFlag(Context context) {
        initiatePreferencesIfNull(context);
        return loggedUserPreferences.getBoolean(Constants.LOCATION_PERMISSIONS_FLAG, false);
    }

    public static void setLocationPermissionsGrantFlag(Context context, boolean granted) {
        initiatePreferencesIfNull(context);
        loggedUserPreferenvesEditor.putBoolean(Constants.LOCATION_PERMISSIONS_FLAG, granted);
        saveChanges();
    }
}