package locationmanager.jgeraldo.com.androidlocationmanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import locationmanager.jgeraldo.com.androidlocationmanager.listeners.OnHomePressedListener;

public class KeyWatcher {

    static final String TAG = "keywatcher";

    private final Context mContext;

    private final IntentFilter mFilter;

    private OnHomePressedListener mListener;

    private InnerRecevier mRecevier;

    public KeyWatcher(final Context context) {
        mContext = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    public final void setOnHomePressedListener(final OnHomePressedListener listener) {
        mListener = listener;
        mRecevier = new InnerRecevier();
    }

    public final void startWatch() {
        if (mRecevier != null) {
            mContext.registerReceiver(mRecevier, mFilter);
        }
    }

    public final void stopWatch() {
        if (mRecevier != null) {
            mContext.unregisterReceiver(mRecevier);
        }
    }

    class InnerRecevier extends BroadcastReceiver {

        /** The system dialog reason key. */
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";

        /** The system dialog reason recent apps. */
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        /** The system dialog reason home key. */
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        /** The system dialog reason power key. */
        private static final String SYSTEM_DIALOG_REASON_LOCK_KEY = "lock";

        /*
         * (non-Javadoc)
         * @see android.content.BroadcastReceiver#onReceive
         * (android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason == null) {
                    return;
                } else {
                    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (mListener == null) {
                        return;
                    } else {
                        if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)
                                || SYSTEM_DIALOG_REASON_LOCK_KEY.equals(reason)) {
                            mListener.onHomePressed();
                        } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            } else {
                return;
            }
        }
    }
}