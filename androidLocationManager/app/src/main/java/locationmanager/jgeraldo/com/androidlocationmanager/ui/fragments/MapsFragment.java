package locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import locationmanager.jgeraldo.com.androidlocationmanager.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    public static Context mContext;

    private static FragmentActivity mActivity;

    private static Toolbar mToolbar;

    private static FragmentManager mFragmentManager;

    private static FabSpeedDial mFabAdd;

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
        mToolbar.setTitle(R.string.nav_map);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);

            setFabActions();
            setUpMap();

        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    private void setFabActions() {
        mFabAdd = (FabSpeedDial) view.findViewById(R.id.fab_map);
        mFabAdd.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.ic_save_current_location) {
                    return true;
                } else if (itemId == R.id.ic_goto_my_position) {
                    return true;
                }
                return false;
            }
        });
    }

    private void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) mFragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng campina = new LatLng(-7.2190974, -35.903685);
        mMap.addMarker(new MarkerOptions().position(campina).title("Marker in Campina Grande").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campina, 16.0f));
    }
}
