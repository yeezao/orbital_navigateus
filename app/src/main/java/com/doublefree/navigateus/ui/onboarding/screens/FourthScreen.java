package com.doublefree.navigateus.ui.onboarding.screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doublefree.navigateus.R;


public class FourthScreen extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_onboarding_fourth_screen, container, false);
        Button button = view.findViewById(R.id.nextFinish);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.action_viewPagerFragment_to_homeFragment);
                onBoardingFinished();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.action_viewPagerFragment_to_homeFragment);
                onBoardingFinished();
            }
        });
        return view;
    }
    private void onBoardingFinished() {
        // Get the shared preferences
        SharedPreferences preferences =
                requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("Finished", true);
        editor.apply();
    }
}