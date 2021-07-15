package com.doublefree.navigateus.ui.stops_services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.doublefree.navigateus.MainActivity;
import com.doublefree.navigateus.R;
import com.google.android.material.tabs.TabLayout;

public class StopsServicesMasterFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager firstViewPager;

    public StopsServicesMasterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_stops_services_master, container, false);

//        Toolbar stopsServicesMasterToolbar = rootView.findViewById(R.id.stops_services_master_toolbar);
//        stopsServicesMasterToolbar.setTitle("Stops & Services");
//        ((AppCompatActivity)getActivity()).setSupportActionBar(stopsServicesMasterToolbar);
//        stopsServicesMasterToolbar.setTitleTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.white));
//        setHasOptionsMenu(true);

        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout2);
        tabLayout.setupWithViewPager(firstViewPager);
        tabLayout.bringToFront();

        setupViewPager(firstViewPager);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.stops_services_master_toolbar_menu, menu);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Stops & Services");
        super.onCreateOptionsMenu(menu, inflater);
    }

    StopsServicesFragment newStopsServicesFragment = new StopsServicesFragment();

    private void setupViewPager(ViewPager viewPager) {
        TabViewPagerAdapter adapter = new TabViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(newStopsServicesFragment, "NUS Stops");
//        adapter.addFragment(new StopsServicesLTAFragment(), "LTA Stops");
        adapter.addFragment(new StopsServicesServicesFragment(), "NUS Routes");

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
//        StopsServicesMasterFragmentDirections.ActionNavigationStopsServicesMasterToNavigationStopsServicesStops action =
//                StopsServicesMasterFragmentDirections.actionNavigationStopsServicesMasterToNavigationStopsServicesStops(jsonIntermediate);
//        NavHostFragment.findNavController(StopsServicesMasterFragment.this).navigate(action);
    }

//    LocationManager locationManager;
//    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
//    boolean isLocationPermissionGranted = false;
//
//
//    private void checkLocationPermission() {
//        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            ActivityCompat.requestPermissions(this.getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_LOCATION);
//
//            return;
//        } else {
//            newStopsServicesFragment.setLocationPermissionGranted(true);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this.getContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        isLocationPermissionGranted = true;
//                        //TODO: pass in Lat/Lng and bool for location
//                    }
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    isLocationPermissionGranted = false;
//
//                }
//                return;
//            }
//
//        }
////        getUserLocationAndStopList(isLocationPermissionGranted);
//        newStopsServicesFragment.setLocationPermissionGranted(isLocationPermissionGranted);
//
//    }
//
//    Location userLocation;
//    Double userLatitude;
//    Double userLongitude;

//    private void getUserLocationAndStopList(boolean isLocationPermissionGranted) {
//
//        try {
//            userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (userLocation == null) {
//                getUserLocationAndStopList(isLocationPermissionGranted);
//            }
//        } catch (SecurityException e) {
//            userLocation.setLatitude(0.0);
//            userLocation.setLongitude(0.0);
//        }
//        newStopsServicesFragment.setUserLocation(userLocation);
//    }

}
