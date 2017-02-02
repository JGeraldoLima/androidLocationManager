package locationmanager.jgeraldo.com.androidlocationmanager.storage.models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class MyLocation extends RealmObject {

    @PrimaryKey
    private String mId;

    @Required
    private Double mLatitude;

    @Required
    private Double mLongitude;

    private Double mAltitude;

    @Required
    private String mName;

    public MyLocation(final String name, final Double latitude,
                      final Double longitude) {
        mId = UUID.randomUUID().toString();
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
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

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
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
