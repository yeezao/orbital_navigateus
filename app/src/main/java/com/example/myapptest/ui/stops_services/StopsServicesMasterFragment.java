package com.example.myapptest.ui.stops_services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myapptest.R;
import com.example.myapptest.databinding.ActivityMainBinding;
import com.example.myapptest.ui.home.HomeFragment;
import com.example.myapptest.ui.home.HomeFragmentDirections;
import com.google.android.material.tabs.TabLayout;
import com.example.myapptest.ui.stops_services.SectionsPagerAdapter;

public class StopsServicesMasterFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager firstViewPager;

    public StopsServicesMasterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_stops_services_master, container, false);

        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout2);
        tabLayout.setupWithViewPager(firstViewPager);
        tabLayout.bringToFront();

        setupViewPager(firstViewPager);
        return rootView;
    }

    private void setupViewPager(ViewPager viewPager) {
        TabViewPagerAdapter adapter = new TabViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new StopsServicesFragment(), "NUS Stops");
        adapter.addFragment(new StopsServicesLTAFragment(), "LTA Stops");
        adapter.addFragment(new StopsServices2Fragment(), "NUS Services");

        viewPager.setAdapter(adapter);
//        String jsonIntermediate = StopsServicesMasterFragmentArgs.fromBundle(getArguments()).getPassStopsList();
//        StopsServicesMasterFragmentDirections.ActionNavigationStopsServicesMasterToNavigationStopsServicesStops action =
//                StopsServicesMasterFragmentDirections.actionNavigationStopsServicesMasterToNavigationStopsServicesStops(jsonIntermediate);
//        NavHostFragment.findNavController(StopsServicesMasterFragment.this).navigate(action);
    }
}
