package locationmanager.jgeraldo.com.androidlocationmanager.storage.models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class MyLocation extends RealmObject {

    @Index
    @PrimaryKey
    private String mId;

    @Required
    private String mCategoryId;

    @Required
    private Double mLatitude;

    @Required
    private Double mLongitude;

    private Double mAltitude;

    @Required
    private String mName;

    public MyLocation(final String name, final String categoryId, final Double latitude,
                      final Double longitude) {
        mName = name;
        mCategoryId = categoryId;
        mLatitude = latitude;
        mLongitude = longitude;
        mAltitude = 0.0;
    }

    public MyLocation() {

    }

    public String getKey() {
        return mId;
    }

    public void setKey(String id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategoryId;
    }

    public void setCategory(String categoryId) {
        this.mCategoryId = categoryId;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(final Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(final Double longitude) {
        mLongitude = longitude;
    }

    public Double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(Double altitude) {
        this.mAltitude = altitude;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MyLocation) {
            final MyLocation myLocation = (MyLocation) obj;
            if (mName.equals(myLocation.getName())
                && mLatitude.equals(myLocation.getLatitude())
                && mLongitude.equals(myLocation.getLongitude())) {
                return true;
            }
        }
        return false;
    }
}
