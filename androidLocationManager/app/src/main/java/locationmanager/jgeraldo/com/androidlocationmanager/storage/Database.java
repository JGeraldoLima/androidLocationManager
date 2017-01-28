package locationmanager.jgeraldo.com.androidlocationmanager.storage;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public final class Database {

    public static final String TABLE_LOCATIONS = "LOCATIONS";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_NAME = "NAME";

    public static final String COLUMN_LATITUDE = "LATITUDE";

    public static final String COLUMN_LONGITUDE = "LONGITUDE";

    public static final String COLUMN_ALTITUDE = "ALTITUDE";


    private static final String CREATE_TABLE_LOCATIONS = "CREATE TABLE "
        + TABLE_LOCATIONS + "  (" +
        COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
        + COLUMN_NAME + " TEXT NOT NULL UNIQUE,"
        + COLUMN_LATITUDE + " REAL NOT NULL,"
        + COLUMN_LONGITUDE + " REAL NOT NULL,"
        + COLUMN_ALTITUDE + " REAL NOT NULL);";

    private static final String TAG = "Database";

    private DatabaseHelper mDbHelper;

    private SQLiteDatabase mDb;

    private static final String DB_NAME = "DBP";

    private static final int DATABASE_VERSION = 1;

    private final Context mContext;

    public static final class DatabaseHelper extends SQLiteOpenHelper {

        /*
         * (non-Javadoc)
         * @see
         * android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.
         * sqlite.SQLiteDatabase)
         */
        @Override
        public void onOpen(final SQLiteDatabase db) {
            super.onOpen(db);
            Log.v(DB_NAME, "onOpenDB");
        }

        DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        /* (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {

            db.execSQL(CREATE_TABLE_LOCATIONS);

            Log.w("DataBaseStorage", "DB created sucefully!");
        }

        /*
         * (non-Javadoc)
         * @see
         * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
         * .sqlite.SQLiteDatabase, int, int)
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                              final int newVersion) {
            Log.w(TAG, "Updating database from version " + oldVersion + " to "
                + newVersion + ", all data will be lost!");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
            onCreate(db);
        }

    }

    public Database(final Context context) {
        mContext = context;
        open();
    }

    public Database open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public DatabaseHelper getMDBHelper() {
        return mDbHelper;
    }

    public SQLiteDatabase getMdb() {
        return mDb;
    }

    public String getPath() {
        return mDb.getPath();
    }

    public void close() {
        mDb.close();
    }

    public void addLocation(final Activity activity, final MyLocation location) {

        final ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, location.getName());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_ALTITUDE, location.getAltitude());

        try {
            mDb.beginTransaction();
            mDb.insert(TABLE_LOCATIONS, null, values);
            mDb.setTransactionSuccessful();
            Util.showSnackBar(activity, location.getName() + " "
                + Util.getString(mContext, R.string.newlocation_saved_successfully));
        } catch (Exception e) {
            Log.e("DB", "EXCEPTION: " + e.getMessage());
            //handle unique name e general exceptions
        } finally {
            mDb.endTransaction();
        }


        List<MyLocation> locations = getAllLocations("");

        //        TODO REMOVE
        for (int i = 0; i < locations.size(); i++) {
            Log.d("LOCATIONS", "Latitude: " + locations.get(i).getLatitude() + "; KEY: " + locations.get(i).getKey());
        }
        //        TODO REMOVE
    }

    public void editLocation(final MyLocation location) {
        final ContentValues values = new ContentValues();

        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_ALTITUDE, location.getAltitude());
        values.put(COLUMN_NAME, location.getName());

        mDb.update(TABLE_LOCATIONS, values, COLUMN_ID + "=" + location.getKey(), null);
    }

    public void removeLocation(final MyLocation location) {
        mDb.delete(TABLE_LOCATIONS, COLUMN_ID + "=?",
            new String[]{
                String.valueOf(location.getKey())
            });
    }

    public List<MyLocation> getAllLocations(final String filterName) {
        final List<MyLocation> mList = new ArrayList<MyLocation>();

        MyLocation mLocation;
        int locationID;
        String locationName;
        Double locationLatitude;
        Double locationLongitude;
        Double locationAltitude;

        final Cursor mCursor = mDb.query(true, TABLE_LOCATIONS, new String[]{
                COLUMN_ID, COLUMN_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                COLUMN_ALTITUDE
            }, null, null, null, null,
            null, null, null);
        if (mCursor != null) {
            try {

                mCursor.moveToFirst();
                while (!mCursor.isAfterLast()) {
                    locationID = mCursor.getInt(mCursor.getColumnIndex(COLUMN_ID));
                    locationName = mCursor.getString(mCursor
                        .getColumnIndex(COLUMN_NAME));
                    locationLatitude = mCursor.getDouble(mCursor
                        .getColumnIndex(COLUMN_LATITUDE));
                    locationLongitude = mCursor.getDouble(mCursor
                        .getColumnIndex(COLUMN_LONGITUDE));
                    locationAltitude = mCursor.getDouble(mCursor
                        .getColumnIndex(COLUMN_ALTITUDE));

                    if (locationName.length() >= filterName.length()
                        && locationName
                        .substring(0, filterName.length())
                        .toLowerCase(java.util.Locale.getDefault())
                        .equals(filterName
                            .toLowerCase(java.util.Locale
                                .getDefault()))) {
                        mLocation = new MyLocation(locationName, locationLatitude,
                            locationLongitude);
                        mLocation.setKey(locationID);
                        mLocation.setAltitude(locationAltitude);
                        mList.add(mLocation);
                    }
                    mCursor.moveToNext();
                }

            } catch (final SQLException e) {
                e.printStackTrace();
            } finally {
                mCursor.close();
            }
        }
        return mList;
    }

    public boolean locationNameAlreadyExists(final String newName) {
        String locationName;
        try {
            final Cursor mCursor = mDb.query(true, TABLE_LOCATIONS,
                new String[]{
                    COLUMN_NAME
                }, null, null, null, null, null,
                null);

            if (mCursor != null) {
                mCursor.moveToFirst();
                while (!mCursor.isAfterLast()) {
                    locationName = mCursor.getString(mCursor
                        .getColumnIndex(COLUMN_NAME));
                    if (locationName.equals(newName)) {
                        return true;
                    }
                    mCursor.moveToNext();
                }
                mCursor.close();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}