package com.example.myapptest.ui.onboarding.screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myapptest.R;


public class ThirdScreen extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_onboarding_third_screen, container, false);

        view.setOnClickListener(new View.OnClickListener() {
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