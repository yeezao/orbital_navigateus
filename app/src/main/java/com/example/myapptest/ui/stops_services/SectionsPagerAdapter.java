package com.example.myapptest.ui.stops_services;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapptest.R;
import com.example.myapptest.ui.directions.DirectionsFragment;
import com.example.myapptest.ui.home.HomeFragment;
import com.example.myapptest.ui.stops_services.StopsServices2Fragment;
import com.example.myapptest.ui.stops_services.StopsServicesFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.stops, R.string.services};

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
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
                return "Stops";
            case 1:
                return "Services";
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}