package locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import locationmanager.jgeraldo.com.androidlocationmanager.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    public static Context mContext;

    private static FragmentActivity mActivity;

    private static Toolbar mToolbar;

    private static FragmentManager mFragmentManager;

    private View view;

    private GoogleMap mMap;

    public MapsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);

        mFragmentManager = getChildFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mToolbar.setTitle("Map"/*R.string.menu_profile*/);
        if (view == null) {
            Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
            view = inflater.inflate(R.layout.fragment_map, container, false);

            SupportMapFragment mapFragment = (SupportMapFragment) mFragmentManager.findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng campina = new LatLng(-7.2190974, -35.903685);
        mMap.addMarker(new MarkerOptions().position(campina).title("Marker in Campina Grande"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campina, 16.0f));
    }

}
