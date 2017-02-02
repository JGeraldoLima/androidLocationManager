package locationmanager.jgeraldo.com.androidlocationmanager.storage;

import android.content.Context;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.MyLocation;

public class RealmUtil {

    private RealmUtil() {
    }

    public static void initialize(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
            .name("androidlocationmanager.realm")
//            .encryptionKey(getEncryptionKey())
            .schemaVersion(1)
            .build();

        Realm.setDefaultConfiguration(config);
    }

    public static void close(){
        Realm realm = Realm.getDefaultInstance();
        realm.close();
    }


    public static void createNewLocation(MyLocation location) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(location);
        realm.commitTransaction();
    }

    public static List<MyLocation> getAllLocations(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MyLocation> locations = realm.where(MyLocation.class)
            .findAll();
        return locations.subList(0, locations.size());
    }

    public static boolean locationNameAlreadyExists(String name) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MyLocation> found = realm.where(MyLocation.class)
            .equalTo("mName", name)
            .findAll();
        return found.size() != 0;
    }
}
