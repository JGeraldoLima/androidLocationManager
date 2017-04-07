package locationmanager.jgeraldo.com.androidlocationmanager.storage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.LocationCategory;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class RealmUtil {

    private RealmUtil() {
    }

    public static void initialize(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
            .name(Constants.REALM_DATABASE_NAME)
//            .encryptionKey(getEncryptionKey())
            .schemaVersion(Constants.REALM_DATABASE_CURRENT_VERSION)
            .build();

        Realm.setDefaultConfiguration(config);
//        populateBD();
    }

    public static void close() {
        Realm realm = Realm.getDefaultInstance();
        realm.close();
    }

    private static void populateBD() {
        //        TODO: refactor this
//        LocationCategory lc1 = createNewCategory("Favorites");
//        LocationCategory lc2 = createNewCategory("General");
//        LocationCategory lc3 = createNewCategory("Food");
//
//        MyLocation location1 = createNewLocation("location1", lc1.getId(), -7.224561, -35.914142, null);
//        MyLocation location2 = createNewLocation("location2", lc1.getId(), -7.223891, -35.914420, null);
//        MyLocation location3 = createNewLocation("location3", lc1.getId(), -7.224381, -35.913428, null);
//        updateCategory(lc1.getId(), lc1.getName(), new MyLocation[]{location1, location2, location3});
//
//        MyLocation location4 = createNewLocation("location4", lc2.getId(), -7.223556, -35.913986, null);
//        MyLocation location5 = createNewLocation("location5", lc2.getId(), -7.223571, -35.915212, null);
//        MyLocation location6 = createNewLocation("location6", lc2.getId(), -7.223873, -35.915522, null);
//        updateCategory(lc2.getId(), lc2.getName(), new MyLocation[]{location4, location5, location6});
//
//        MyLocation location7 = createNewLocation("location7", lc3.getId(), -7.222041, -35.915048, null);
//        MyLocation location8 = createNewLocation("location8", lc3.getId(), -7.221615, -35.915520, null);
//        MyLocation location9 = createNewLocation("location9", lc3.getId(), -7.220646, -35.916453, null);
//        updateCategory(lc3.getId(), lc3.getName(), new MyLocation[]{location7, location8, location9});
    }

    // TODO: would be nice some refatoring to reuse these Realm steps
    public static MyLocation createNewLocation(Activity activity, String name, String categoryName, Double latitude,
                                               Double longitude, Double altitude) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<LocationCategory> found = realm.where(LocationCategory.class)
            .equalTo("mName", categoryName)
            .findAll();

        LocationCategory category = found.get(0);

        MyLocation realmLocation = realm.createObject(MyLocation.class, UUID.randomUUID().toString());
        realmLocation.setName(name);
        realmLocation.setCategory(category.getId());
        realmLocation.setLatitude(latitude);
        realmLocation.setLongitude(longitude);
        realmLocation.setAltitude(altitude != null ? altitude : 0);

        //TODO: find in Realm Docs how to do it automatically
        updateCategory(category.getId(), category.getName(), new MyLocation[]{realmLocation}, realm);

        Util.showSnackBar(activity,
            String.format(Util.getString(activity, R.string.location_created_successfully), name));
        return realmLocation;
    }

    public static LocationCategory createNewCategory(Activity activity, String name) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        LocationCategory realmCategory = realm.createObject(LocationCategory.class, UUID.randomUUID().toString());
        realmCategory.setName(name);
        realm.commitTransaction();
        Util.showSnackBar(activity,
            String.format(Util.getString(activity, R.string.category_created_successfully), name));
        return realmCategory;
    }

    public static void updateCategory(String id, String name, MyLocation[] locations, Realm realm) {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();
        }

        LocationCategory updatedCategory = realm.where(LocationCategory.class)
            .equalTo("mId", id)
            .findFirst();

        updatedCategory.setName(name);
        if (locations != null) {
            Collections.addAll(updatedCategory.locations, locations);
        }
        realm.copyToRealmOrUpdate(updatedCategory);
        realm.commitTransaction();
    }


    public static List<MyLocation> getAllLocations() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MyLocation> locations = realm.where(MyLocation.class)
            .findAll();
        return locations.subList(0, locations.size());
    }

    public static List<LocationCategory> getAllLocationCategories() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<LocationCategory> categories = realm.where(LocationCategory.class)
            .findAll();
        return categories.subList(0, categories.size());
    }

    public static boolean locationNameAlreadyExists(String name) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MyLocation> found = realm.where(MyLocation.class)
            .equalTo("mName", name)
            .findAll();
        return found.size() != 0;
    }

    public static boolean categoryNameAlreadyExists(String name) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<LocationCategory> found = realm.where(LocationCategory.class)
            .equalTo("mName", name)
            .findAll();
        return found.size() != 0;
    }
}
