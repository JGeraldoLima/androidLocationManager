package locationmanager.jgeraldo.com.androidlocationmanager.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import locationmanager.jgeraldo.com.androidlocationmanager.R;
import locationmanager.jgeraldo.com.androidlocationmanager.entities.MyLocationManager;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.RealmUtil;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.LocationCategory;
import locationmanager.jgeraldo.com.androidlocationmanager.storage.models.MyLocation;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Constants;
import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public class LocationsListFragment extends Fragment {

    public Context mContext;

    private FragmentActivity mActivity;

    private MyLocationManager mLocationManager;

    private Toolbar mToolbar;

    private static FragmentManager mFragmentManager;

    private static Fragment mFragment;

    private FabSpeedDial mFabAdd;

    private FabSpeedDial mFabTypes;

    private View view;

    private ExpandingList mExpandingLocationsList;

    private List<ExpandingItem> mExpandListCategories;

    public LocationsListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = this;
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();

        mFragmentManager = getChildFragmentManager();
        mLocationManager = Util.getLocationManager();

        mExpandListCategories = new ArrayList<ExpandingItem>();

        if (mLocationManager.checkLocationServicesStatus()) {
            Util.checkPermissions(mActivity, Constants.LOCATION_PERMISSIONS_FLAG, Constants.LOCATION_PERMISSION_CODES,
                Constants.LOCATION_PERMISSIONS_CODE, mFragment);
        } else {
            mLocationManager.openEnableLocationServicesDialog(mActivity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.nav_list_poi);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_locations_list, container, false);

            setViews();
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mLocationManager != null) {
            mLocationManager.stopUpdates(false);
        }
        RealmUtil.close();
        super.onDestroyView();
    }

    private void setViews() {
        mExpandingLocationsList = (ExpandingList) view.findViewById(R.id.expandingLocationsList);
        loadExpandListContent();

//        mFabAdd = (FabSpeedDial) view.findViewById(R.id.fab_map_add_options);
//        mFabAdd.setMenuListener(new SimpleMenuListenerAdapter() {
//            @Override
//            public boolean onMenuItemSelected(MenuItem menuItem) {
//                int itemId = menuItem.getItemId();
//
//                if (itemId == R.id.ic_add_new_poi) {
//                    return true;
//                } else if (itemId == R.id.ic_add_new_category) {
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    private void loadExpandListContent() {
        List<LocationCategory> categories = RealmUtil.getAllLocationCategories();
        for (LocationCategory lc : categories) {
            ExpandingItem categoryItem = mExpandingLocationsList.createNewItem(R.layout.location_list_item);
            ((TextView) categoryItem.findViewById(R.id.location_category_name)).setText(lc.getName());
            categoryItem.setIndicatorIconRes(R.drawable.ic_expand_more);
            categoryItem.setIndicatorColorRes(R.color.colorAccent);
            mExpandListCategories.add(categoryItem);

            List<MyLocation> categoryLocations = lc.getLocations();
            if (categoryLocations.size() > 0) {
                categoryItem.createSubItems(categoryLocations.size());
                for (int i = 0; i < categoryLocations.size(); i++) {
                    final MyLocation location = categoryLocations.get(i);
                    View subItem = categoryItem.getSubItemView(i);
                    ((TextView) subItem.findViewById(R.id.location_name)).setText(location.getName());
                    subItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Util.showSnackBar(mActivity, location.getName() + " Lat: " + location.getLatitude() + "; " +
                                "Long:" +
                                " " +
                                location.getLongitude());
                        }
                    });
                }
            } else {
                // no locations for this category, nothing to show on expand
                // TODO: add an 'create button' on the category item layout to show up the 'save a location'
                // create dialog
                categoryItem.setClickable(false);
                categoryItem.setAlpha(0.6f);
            }

        }
    }
}
