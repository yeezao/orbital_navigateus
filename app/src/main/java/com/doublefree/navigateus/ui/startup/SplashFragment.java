package com.doublefree.navigateus.ui.startup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.doublefree.navigateus.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class SplashFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.GONE);

        int delay = 2000;
        if (onBoardingFinished()) {
            delay = 800;
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ImageView splash = view.findViewById(R.id.splashimageview);
                if (onBoardingFinished()) {
                    splash.setVisibility(View.INVISIBLE);
                    navController.navigate(R.id.action_splashFragment_to_homeFragment);
                } else {
                    navController.navigate(R.id.action_splashFragment_to_viewPagerFragment);
                }
            }
        }, delay);

    }

    private boolean onBoardingFinished() {
        // Get the shared preferences
        boolean finished = false;
        if (isAdded()) {
            SharedPreferences preferences =
                    requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE);
            finished = preferences.getBoolean("Finished", false);

        }
        return finished;
    }
}
