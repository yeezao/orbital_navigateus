package com.example.myapptest.ui.stops_services;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;

import com.example.myapptest.R;

import java.util.ArrayList;
import java.util.List;

public class TabViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public TabViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

//    @Override
//    public Fragment getItem(int position) {
//        return mFragmentList.get(position);
//    }
//
//    @Override
//    public int getCount() {
//        return mFragmentList.size();
//    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        return mFragmentTitleList.get(position);
//    }

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.stops, R.string.lta_stops, R.string.services};

    public TabViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new StopsServicesFragment();
                break;
            case 1:
                fragment = new StopsServicesLTAFragment();
                break;
            case 2:
                fragment = new StopsServices2Fragment();
                break;

        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "NUS Stops";
            case 1:
                return "LTA Stops";
            case 2:
                return "NUS Services";

        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}