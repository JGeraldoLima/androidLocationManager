package locationmanager.jgeraldo.com.androidlocationmanager.entities;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class LocationSearchTimer extends CountDownTimer {

    private static final int LONG_DIVISOR = 1000;

    private final MyLocationManager myLocationManager = Util.getLocationManager();

    private final AsyncTask<Void, Void, Void> mTask;

    private final ProgressDialog mProgress;

    private final String progressMessage;

    private final boolean mCancelTask;

    public LocationSearchTimer(final Context context,
                               final AsyncTask<Void, Void, Void> task,
                               final ProgressDialog progressDialog, final String message,
                               final long millisInFuture, final long countDownInterval,
                               final boolean cancelCurrentTask) {
        super(millisInFuture, countDownInterval);
        mTask = task;
        mProgress = progressDialog;
        progressMessage = message;
        mCancelTask = cancelCurrentTask;
    }

    /*
     * (non-Javadoc)
     * @see android.os.CountDownTimer#onTick(long)
     */
    @Override
    public final void onTick(final long millisUntilFinished) {
        Location tempSatLocation = myLocationManager.getSatelliteLocation();
        Location tempNetLocation = myLocationManager.getNetworkLocation();
        Log.i("CURRENT NETWORK COORDINATES",
            "Latitude: "
                + tempNetLocation.getLatitude()
                + "; Longitude: "
                + tempNetLocation.getLongitude());
        Log.i("CURRENT GPS COORDINATES",
            "Latitude: "
                + tempSatLocation.getLatitude()
                + "; Longitude: "
                + tempSatLocation.getLongitude());
        mProgress.setMessage(progressMessage + " "
            + (millisUntilFinished / LONG_DIVISOR) + "s");

        if (!mProgress.isShowing()) {
            myLocationManager.setTimerState(true);
            myLocationManager.setGPSUpdatingState(false);
            if (mCancelTask) {
                this.cancel();
            }
        }

        if (!MyLocationManager
            .isLocationInvalid(tempSatLocation)) {
            myLocationManager.setTimerState(true);
            myLocationManager.setGPSUpdatingState(false);
            if (mCancelTask) {
                this.cancel();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.CountDownTimer#onFinish()
     */
    @Override
    public final void onFinish() {
        mProgress.cancel();
        myLocationManager.setTimerState(true);
        if (mCancelTask) {
            mTask.cancel(true);
            this.cancel();
        }
    }
}
