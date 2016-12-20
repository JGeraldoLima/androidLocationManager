package locationmanager.jgeraldo.com.androidlocationmanager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import alugasecadeirasdepraia.jgeraldo.com.alugasecadeirasdepraia.R;
import alugasecadeirasdepraia.jgeraldo.com.alugasecadeirasdepraia.entities.User;
import alugasecadeirasdepraia.jgeraldo.com.alugasecadeirasdepraia.utils.Util;

public class SplashActivity extends Activity {

    /** The splash time out. */
    private static final int SPLASH_TIME_OUT = 2000;

    /** the application context*/
    private static Context mContext;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = getApplicationContext();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = null;
                User loggedUser = Util.getLoggedUser(mContext);

                if (loggedUser == null || loggedUser.getName().equals(Util.getString(mContext, R.string.user_name))) {
                    i = new Intent(SplashActivity.this,
                            LoginActivity.class);
                } else {
                    i = new Intent(SplashActivity.this,
                            MainActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}