package com.example.myapptest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.myapptest.databinding.ActivityMainBinding;
import com.example.myapptest.ui.directions.DirectionsFragment;
import com.example.myapptest.ui.home.HomeFragment;
import com.example.myapptest.ui.stops_services.StopsServicesFragment;
import com.example.myapptest.ui.stops_services.StopsServicesMasterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapptest.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    Fragment homeFragment = new HomeFragment();
    Fragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
    Fragment directionsFragment = new DirectionsFragment();
    FragmentManager fm = getSupportFragmentManager();
    Fragment active;
    BottomNavigationView navView;

    //    private final StopsServicesMasterFragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
//    private final DirectionsFragment directionsFragment = new DirectionsFragment();
//    private final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        navView.bringToFront();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_stops_services_master, R.id.navigation_directions)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //TODO: need to remove all FragmentTransaction code
//        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, directionsFragment, "3").hide(directionsFragment).commit();
//        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, stopsServicesMasterFragment, "2").hide(stopsServicesMasterFragment).commit();
//        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, homeFragment, "1").commit();
        active = homeFragment;

        navView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
//        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }


    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {

        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            //do nothing
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
////                    fm.beginTransaction().remove(homeFragment).remove(stopsServicesMasterFragment).remove(directionsFragment).commit();
////                    fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, homeFragment, "1").commit();
//                    fm.beginTransaction().remove(homeFragment).commit();
//                    homeFragment = new HomeFragment();
//                    fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, homeFragment, "1").show(homeFragment).commitNow();
////                    fm.beginTransaction().hide(active).show(homeFragment).commit();
////                    active = homeFragment;
//
//                case R.id.navigation_stops_services_master:
//                    fm.beginTransaction().remove(stopsServicesMasterFragment).commit();
//                    stopsServicesMasterFragment = new StopsServicesMasterFragment();
//                    fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, stopsServicesMasterFragment, "2").show(stopsServicesMasterFragment).commitNow();
////                    fm.beginTransaction().replace(R.id.nav_host_fragment_activity_main, stopsServicesMasterFragment).commitNow();
////                    fm.beginTransaction().hide(active).show(stopsServicesMasterFragment).commit();
////                    active = stopsServicesMasterFragment;
//
//                case R.id.navigation_directions:
//                    fm.beginTransaction().remove(directionsFragment).commit();
//                    directionsFragment = new DirectionsFragment();
//                    fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, directionsFragment, "3").show(directionsFragment).commitNow();
////                    fm.beginTransaction().replace(R.id.nav_host_fragment_activity_main, directionsFragment).commitNow();
////                    fm.beginTransaction().hide(active).show(directionsFragment).commit();
////                    active = directionsFragment;
//            }
        }

    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fm.beginTransaction().hide(active).show(homeFragment).addToBackStack(null).commit();
                    active = homeFragment;
                    if (active != homeFragment) {
                        navView.setSelectedItemId(R.id.navigation_home);
                    }
                    return true;

                case R.id.navigation_stops_services_master:
//                    if (active == homeFragment) {
//                        fm.beginTransaction().addToBackStack(null);
//                    }
                    fm.beginTransaction().hide(active).show(stopsServicesMasterFragment).addToBackStack(null).commit();

                    active = stopsServicesMasterFragment;
//                    navView.setSelectedItemId(R.id.navigation_stops_services_master);
                    return true;

                case R.id.navigation_directions:
//                    if (active == homeFragment) {
//                        fm.beginTransaction().addToBackStack(null);
//                    }
                    fm.beginTransaction().hide(active).show(directionsFragment).addToBackStack(null).commit();
                    active = directionsFragment;
//                    navView.setSelectedItemId(R.id.navigation_directions);
                    return true;
            }
            return false;
        }
    };

//    fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//        @Override
//        public void onBackStackChanged() {
//            // If the stack decreases it means I clicked the back button
//            if( fragmentManager.getBackStackEntryCount() <= count){
//                //check your position based on selected fragment and set it accordingly.
//                navigation.getMenu().getItem(your_pos).setChecked(true);
//            }
//        }
//    });

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        Fragment fragmentChecker = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else if (fragmentChecker instanceof StopsServicesMasterFragment
                || fragmentChecker instanceof DirectionsFragment || fragmentChecker instanceof HomeFragment) {
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            navView.getMenu().getItem(1).setChecked(true);
//        } else if (fragmentChecker instanceof HomeFragment) {
//            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            navView.getMenu().getItem(1).setChecked(true);
//            super.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }
}

//    @Override
//    public void onBackPressed() {
//        // if your using fragment then you can do this way
//        int fragments
//                = getSupportFragmentManager().getBackStackEntryCount();
//        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
//                getSupportFragmentManager().popBackStack();
//                Fragment selectedFragment = null;
//                List<Fragment> fragmentsList = fm.getFragments();
//                for (Fragment fragment : fragmentsList) {
//                    if (fragment != null && fragment.isVisible()) {
//                        selectedFragment = fragment;
//                        break;
//                    }
//                }
//                if (selectedFragment instanceof HomeFragment) {
//                    nav.setSelectedItemId(R.id.your_first_item);
//                } if (selectedFragment instanceof StopsServicesMasterFragment) {
//                    navigation.setSelectedItemId(R.id.your_second_item);
//                } if (selectedFragment instanceof DirectionsFragment) {
//                    navigation.setSelectedItemId(R.id.your_third_item);
//                } else {
//                    super.onBackPressed();
//                }
//
//            } else {
//                super.onBackPressed();
//            }
//        }
//
//}