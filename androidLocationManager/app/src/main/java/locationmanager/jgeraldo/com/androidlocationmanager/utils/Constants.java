package locationmanager.jgeraldo.com.androidlocationmanager.utils;

import android.Manifest;

public class Constants {

    //  REALM Constants
    public static final int REALM_DATABASE_CURRENT_VERSION = 1;

    public static final String REALM_DATABASE_NAME = "androidlocationmanager.realm";

    // FRAGMENTS TAGS
    public static final String ADD_POI_FRAGMENT = "add_poi_fragment";

    public static final String MAPS_FRAGMENT = "maps_fragment";

    public static final String POI_LIST_FRAGMENT = "poi_list_fragment";

    public static final String SETTINGS_FRAGMENT = "settings_fragment";

    // Util Constants
    public static final String PREFS_NAME = "androidLocationManager";

    public static final String LOCATION_PERMISSIONS_FLAG = "hasLocationPermissions";

    public static final int BUFFER_SIZE = 1024;

    public static final int NEARBY_POINTS = 5;

    // PERMISSIONs Constants
    public static final int LOCATION_PERMISSIONS_CODE = 0;

    public static final String[] LOCATION_PERMISSION_CODES = {Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION};

    // Locations Constantes
    public static final float DEFAULT_MAP_ZOOM = 15.5f;


}