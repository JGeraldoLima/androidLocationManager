package locationmanager.jgeraldo.com.androidlocationmanager.entities;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public final class Route {

    private final List<LatLng> mPoints;

    private String mPolyline;

    public Route() {
        mPoints = new ArrayList<LatLng>();
    }

    public void addPoints(final List<LatLng> points) {
        this.mPoints.addAll(points);
    }

    public List<LatLng> getPoints() {
        return mPoints;
    }

    public void setPolyline(final String polyline) {
        this.mPolyline = polyline;
    }

    public String getPolyline() {
        return mPolyline;
    }

    public boolean isRouteEmpty() {
        return mPoints.isEmpty();
    }
}
