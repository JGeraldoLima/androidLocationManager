package locationmanager.jgeraldo.com.androidlocationmanager.entities;

public final class MyLocation {

    private Double mLatitude;

    private Double mLongitude;

    private Double mAltitude;

    private String mName;

    private String mDirectory;

    private String mDescription;

    public MyLocation(final int key, final String name, final Double latitude,
                      final Double longitude, final String directory,
                      final String description) {
        mDirectory = directory;
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mDescription = description;
    }

    public MyLocation(final String name, final Double latitude,
                      final Double longitude, final String directory,
                      final String description) {
        mLatitude = latitude;
        mLongitude = longitude;
        mDirectory = directory;
        mName = name;
        mDescription = description;
        mAltitude = 0.0;
    }

    public MyLocation(final String name, final Double latitude,
                      final Double longitude, final Double altitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mAltitude = altitude;
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

    public String getDirectory() {
        return mDirectory;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(final String description) {
        this.mDescription = description;
    }

    public void setDirectory(final String directory) {
        mDirectory = directory;
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
