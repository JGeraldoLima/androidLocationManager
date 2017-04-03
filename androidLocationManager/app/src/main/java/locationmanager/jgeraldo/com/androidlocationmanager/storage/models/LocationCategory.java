package locationmanager.jgeraldo.com.androidlocationmanager.storage.models;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class LocationCategory extends RealmObject {

    @Index
    @PrimaryKey
    private String mId;

    @Required
    private String mName;

    public RealmList<MyLocation> locations;

    public LocationCategory(final String name) {
        mName = name;
    }

    public LocationCategory() {
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public RealmList<MyLocation> getLocations() {
        return locations;
    }

    public void setLocations(RealmList<MyLocation> locations) {
        this.locations = locations;
    }

    /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof LocationCategory) {
            final LocationCategory locationCategory = (LocationCategory) obj;
            if (mName.equals(locationCategory.getName())) {
                return true;
            }
        }
        return false;
    }
}
