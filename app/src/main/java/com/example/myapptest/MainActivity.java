package com.example.myapptest;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.myapptest.databinding.ActivityMainBinding;
import com.example.myapptest.ui.directions.DirectionsFragment;
import com.example.myapptest.ui.home.HomeFragment;
import com.example.myapptest.ui.stops_services.StopsServicesMasterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapptest.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final HomeFragment homeFragment = new HomeFragment();
//    private final StopsServicesMasterFragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
//    private final DirectionsFragment directionsFragment = new DirectionsFragment();
//    private final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.bringToFront();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_stops_services_master, R.id.navigation_directions)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    Fragment active = homeFragment;

//    private BottomNavigationView.OnNavigationItemSelectedListener menuOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_stops_services_master:
//                    fm.beginTransaction().hide(active).show(stopsServicesMasterFragment).commit();
//                    active = stopsServicesMasterFragment;
//                    break;
//                case R.id.navigation_home:
//                    fm.beginTransaction().hide(active).show(homeFragment).commit();
//                    active = homeFragment;
//                    break;
//                case R.id.navigation_directions:
//                    fm.beginTransaction().hide(active).show(directionsFragment).commit();
//                    active = directionsFragment;
//                    break;
//            }
//            return true;
//        }
//    };



}