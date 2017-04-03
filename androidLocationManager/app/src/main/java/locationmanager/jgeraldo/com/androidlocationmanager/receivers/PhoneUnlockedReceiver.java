package locationmanager.jgeraldo.com.androidlocationmanager.receivers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class PhoneUnlockedReceiver extends BroadcastReceiver {

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public final void onReceive(final Context context, final Intent intent) {

        KeyguardManager keyguardManager = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            MyLocationManager mLocationManager = Util.getLocationManager();
            if (mLocationManager != null) {
                mLocationManager.startLocationUpdatesByPrecisionStatus();
            }
        }
    }
}