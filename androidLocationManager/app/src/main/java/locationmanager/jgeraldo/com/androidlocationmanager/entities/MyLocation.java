package locationmanager.jgeraldo.com.androidlocationmanager.entities;

public final class MyLocation {

    private int mKey;

    private Double mLatitude;

    private Double mLongitude;

    private Double mAltitude;

    private String mName;

    public MyLocation(final int key, final String name, final Double latitude,
                      final Double longitude) {
        mKey = key;
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mAltitude = 0.0;
    }

    public int getKey() {
        return mKey;
    }

    public void setKey(int mKey) {
        this.mKey = mKey;
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
