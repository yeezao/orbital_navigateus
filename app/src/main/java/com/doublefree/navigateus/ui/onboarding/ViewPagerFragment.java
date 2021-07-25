package com.doublefree.navigateus.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.ui.onboarding.screens.FirstScreen;
import com.doublefree.navigateus.ui.onboarding.screens.FourthScreen;
import com.doublefree.navigateus.ui.onboarding.screens.SecondScreen;
import com.doublefree.navigateus.ui.onboarding.screens.ThirdScreen;

import java.util.ArrayList;

public class ViewPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_onboarding_view_pager, container, false);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        ArrayList<Fragment> FragmentList = new ArrayList<>();
        FragmentList.add(new FirstScreen());
        FragmentList.add(new SecondScreen());
        FragmentList.add(new ThirdScreen());
//        FragmentList.add(new FourthScreen());

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(),getLifecycle(), FragmentList);

        viewPager.setAdapter(adapter);

        return view;
    }
}
