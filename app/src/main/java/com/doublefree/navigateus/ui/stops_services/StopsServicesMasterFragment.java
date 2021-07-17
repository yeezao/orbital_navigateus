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

    }


}
