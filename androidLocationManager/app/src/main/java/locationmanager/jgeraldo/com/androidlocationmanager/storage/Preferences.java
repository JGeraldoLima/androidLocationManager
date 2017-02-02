package locationmanager.jgeraldo.com.androidlocationmanager.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

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

    public static boolean getPermissionGrantFlag(String permissionKey, Context context) {
        initiatePreferencesIfNull(context);
        return loggedUserPreferences.getBoolean(permissionKey, false);
    }

    public static Double getUserLatitudePos(Context context) {
        initiatePreferencesIfNull(context);
        String userLatitudePos = Util.getString(context, R.string.latitude_pos);
        return Double.parseDouble(loggedUserPreferences.getFloat(userLatitudePos, -1F) + "");
    }

    public static Double getUserLongitudePos(Context context) {
        initiatePreferencesIfNull(context);
        String userLongitudePos = Util.getString(context, R.string.longitude_pos);
        return Double.parseDouble(loggedUserPreferences.getFloat(userLongitudePos, -1F) + "");
    }

    public static void setPermissionGrantFlag(Context context, String permissionKey, boolean granted) {
        initiatePreferencesIfNull(context);
        loggedUserPreferenvesEditor.putBoolean(permissionKey, granted);
        saveChanges();
    }

    public static void setUserLatitudePos(Context context, double latitude) {
        initiatePreferencesIfNull(context);
        String userLatitudePos = Util.getString(context, R.string.latitude_pos);
        loggedUserPreferenvesEditor.putFloat(userLatitudePos, (float) latitude);
        saveChanges();
    }

    public static void setUserLongitudePos(Context context, double longitude) {
        initiatePreferencesIfNull(context);
        String userLongitudePos = Util.getString(context, R.string.longitude_pos);
        loggedUserPreferenvesEditor.putFloat(userLongitudePos, (float) longitude);
        saveChanges();
    }
}