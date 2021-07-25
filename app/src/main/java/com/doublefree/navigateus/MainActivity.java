package com.doublefree.navigateus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doublefree.navigateus.data.busstopinformation.ArrivalNotifications;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.doublefree.navigateus.data.naviagationdata.NavigationResults;
import com.doublefree.navigateus.data.naviagationdata.NavigationSearchInfo;
import com.doublefree.navigateus.databinding.ActivityMainBinding;
import com.doublefree.navigateus.favourites.FavouriteDatabase;
import com.doublefree.navigateus.favourites.FavouriteStop;
import com.doublefree.navigateus.ui.NotificationBroadcast;
import com.doublefree.navigateus.ui.NotificationBroadcastInterface;
import com.doublefree.navigateus.ui.directions.DirectionsFragment;
import com.doublefree.navigateus.ui.home.HomeFragment;
import com.doublefree.navigateus.ui.stops_services.SetArrivalNotificationsDialogFragment;
import com.doublefree.navigateus.ui.stops_services.StopsServicesMasterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SetArrivalNotificationsDialogFragment.ArrivalNotificationsDialogListenerForActivity {

    Fragment homeFragment = new HomeFragment();
    Fragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
    Fragment directionsFragment = new DirectionsFragment();
    FragmentManager fm = getSupportFragmentManager();
    Fragment active;
    BottomNavigationView navView;

    Activity mainActivity;

    NavController navController;

    public static FavouriteDatabase favouriteDatabase;
//    public static IsFirstRunsDatabase isFirstRunsDatabase;

    //    private final StopsServicesMasterFragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
//    private final DirectionsFragment directionsFragment = new DirectionsFragment();
//    private final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_Navigateus);

        super.onCreate(savedInstanceState);

        mainActivity = this;

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        navView.bringToFront();
        createNotificationChannel();

        BeginMonitoring();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_stops_services_master, R.id.navigation_directions)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        favouriteDatabase = Room.databaseBuilder(getApplicationContext(), FavouriteDatabase.class, "myfavdb").allowMainThreadQueries().build();
//        isFirstRunsDatabase = Room.databaseBuilder(getApplicationContext(), IsFirstRunsDatabase.class, "isFirstRundb").allowMainThreadQueries().build();

        active = homeFragment;

        navView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);

//        if (isFirstRunsDatabase.isFirstRunsCRUD().isFirstRunPresent() == 0) {
//            IsFirstRuns isFirstRuns = new IsFirstRuns();
//            isFirstRunsDatabase.isFirstRunsCRUD().addData(isFirstRuns);
//        }

        //refreshing favourites data to most up-to-date
        List<FavouriteStop> listOfFavouriteStops = MainActivity.favouriteDatabase.favouriteStopCRUD().getFavoriteData();
        if (listOfFavouriteStops.size() > 0) {
            NextbusAPIs.callStopsList(this, this.getApplicationContext(), new NextbusAPIs.VolleyCallBackAllStops() {
                @Override
                public void onSuccessAllStops(List<StopList> listOfAllStops) {
                    for (int i = 0; i < listOfFavouriteStops.size(); i++) {
                        for (int j = 0; j < listOfAllStops.size(); j++) {
                            if (listOfFavouriteStops.get(i).getStopId().equals(listOfAllStops.get(j).getStopId())) {
                                StopList currentStop = listOfAllStops.get(j);
                                FavouriteStop favouriteStop = new FavouriteStop();
                                favouriteStop.setStopName(currentStop.getStopName());
                                favouriteStop.setStopId(currentStop.getStopId());
                                favouriteStop.setLatitude(currentStop.getStopLatitude());
                                favouriteStop.setLongitude(currentStop.getStopLongitude());
                                int finalI = i;
                                NextbusAPIs.callSingleStopInfo(mainActivity, getApplicationContext(), currentStop.getStopId(),
                                        0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                                    @Override
                                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                                        boolean areAnyServicesNotPresent = false;
                                        for (int k = 0; k < FavouriteStop.fromString(listOfFavouriteStops.get(finalI).getServicesFavourited()).size(); k++) {
                                            for (int m = 0; m < servicesAllInfoAtStop.size(); m++) {
                                                if (servicesAllInfoAtStop.get(m).getServiceNum()
                                                        .contains(FavouriteStop.fromString(listOfFavouriteStops.get(finalI).getServicesFavourited()).get(k))) {
                                                    break;
                                                }
                                                if (m == servicesAllInfoAtStop.size() - 1) {
                                                    areAnyServicesNotPresent = true;
                                                }
                                            }
                                        }
                                        if (areAnyServicesNotPresent) {
                                            List<String> servicesNums = new ArrayList<>();
                                            for (int k = 0; k < servicesAllInfoAtStop.size(); k++) {
                                                servicesNums.add(servicesAllInfoAtStop.get(k).getServiceNum());
                                            }
                                            favouriteStop.setServicesFavourited(FavouriteStop.fromArrayList(servicesNums));
                                        } else {
                                            favouriteStop.setServicesFavourited(listOfFavouriteStops.get(finalI).getServicesFavourited());
                                        }
                                        MainActivity.favouriteDatabase.favouriteStopCRUD().updateData(favouriteStop);
                                    }

                                    @Override
                                    public void onFailureSingleStop() {

                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onFailureAllStops() {

                }
            });

        }

    }

    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {

        @Override //do nothing
        public void onNavigationItemReselected(@NonNull MenuItem item) {
        }

    };

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            //Title bar back press triggers onBackPressed()
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
////    Both navigation bar back press and title bar back press will trigger this method
//    @Override
//    public void onBackPressed() {
//        while (!navController.getBackQueue().isEmpty()) {
//            navController.navigateUp();
//        }
//        super.onBackPressed();
//    }

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return true;
    }

    NotificationManager notificationManager;
    Uri soundUri;

    /**
     * Creates notification channels (as required by Android for arrival alerts and persistent arrival notifications
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence persistentName = getString(R.string.persistent_channel_name);
            String descriptionPersistent = getString(R.string.persistent_channel_description);
            CharSequence regularName = getString(R.string.regular_channel_name);
            String descriptionRegular = getString(R.string.regular_channel_description);
            int importanceArrivalNotifications = NotificationManager.IMPORTANCE_HIGH;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channelArrivalNotificationsPersistent
                    = new NotificationChannel(getString(R.string.arrivalnotifications_monitoring_notif_id), persistentName, importanceArrivalNotifications);
            channelArrivalNotificationsPersistent.setDescription(descriptionPersistent);
            channelArrivalNotificationsPersistent.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channelArrivalNotificationsPersistent.enableVibration(false);
            channelArrivalNotificationsPersistent.setSound(null, audioAttributes);
            channelArrivalNotificationsPersistent.setBypassDnd(true);

            NotificationChannel channelArrivalNotificationsRegular
                    = new NotificationChannel(getString(R.string.arrivalnotifications_triggered_notif_id), "Notify Arrival", importanceArrivalNotifications);
            channelArrivalNotificationsRegular.setDescription(descriptionRegular);
            channelArrivalNotificationsRegular.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channelArrivalNotificationsRegular.setBypassDnd(true);
            channelArrivalNotificationsRegular.enableLights(true);
            channelArrivalNotificationsRegular.setVibrationPattern(new long[] {0, 1000, 1000, 1000});

            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bus_announcement_mp3);
            channelArrivalNotificationsRegular.setSound(soundUri, audioAttributes);
            channelArrivalNotificationsRegular.enableVibration(true);

            List<NotificationChannel> listToAdd = new ArrayList<>();
            listToAdd.add(channelArrivalNotificationsPersistent);
            listToAdd.add(channelArrivalNotificationsRegular);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannels(listToAdd);
            notificationManager.createNotificationChannels(listToAdd);
        }
    }

    //currently not in use
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

//    @Override
//    public void onBackPressed() {
//
//        int count = getSupportFragmentManager().getBackStackEntryCount();
//
//        Fragment fragmentChecker = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
//
//        if (count == 0) {
//            super.onBackPressed();
//            //additional code
//        } else if (fragmentChecker instanceof StopsServicesMasterFragment
//                || fragmentChecker instanceof DirectionsFragment || fragmentChecker instanceof HomeFragment) {
//            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            navView.getMenu().getItem(1).setChecked(true);
////        } else if (fragmentChecker instanceof HomeFragment) {
////            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
////            navView.getMenu().getItem(1).setChecked(true);
////            super.onBackPressed();
//        } else {
//            super.onBackPressed();
//        }
//
//    }

    //variables and methods for global list of bus stops
    String firstPassStopsList;

    List<StopList> listOfAllStops;
    StopList listOfStops;
    List<String> listOfNames;
    List<String> listOfIds;
    List<Double> listOfLat;
    List<Double> listOfLong;

    //variables and methods for global bus arrival notifications
    List<ArrivalNotifications> arrivalNotificationsArray = new ArrayList<>();
    Handler monitoringHandler;


    /**
     * This method is a callback from SetArrivalNotificationsDialogFragment, when the user clicks "OK".
     * It updates the favourites and arrival alerts data.
     *
     * @param singleStopArrivalNotifications Object with newly set favourites and arrival alerts data
     */
    @Override
    public void onDialogPositiveClick(ArrivalNotifications singleStopArrivalNotifications) {
        boolean stopRepeated = false;
        boolean startNewMonitoring = false;

        //for favourite adding
        if (singleStopArrivalNotifications.isFavourite()) {
            FavouriteStop favouriteStop = new FavouriteStop();
            favouriteStop.setStopId(singleStopArrivalNotifications.getStopId());
            favouriteStop.setStopName(singleStopArrivalNotifications.getStopName());
            favouriteStop.setServicesFavourited(FavouriteStop.fromArrayList(singleStopArrivalNotifications.getServicesFavourited()));
            favouriteStop.setLatitude(singleStopArrivalNotifications.getLatitude());
            favouriteStop.setLongitude(singleStopArrivalNotifications.getLongitude());
            if (favouriteDatabase.favouriteStopCRUD().isFavorite(favouriteStop.getStopId()) == 1) {
                boolean isServicesTheSame = true;
                List<String> listOfPreviousFavourites = FavouriteStop.fromString(
                        favouriteDatabase.favouriteStopCRUD().getFavoriteDataSingle(favouriteStop.getStopId()).getServicesFavourited());
                if (listOfPreviousFavourites.size() != singleStopArrivalNotifications.getServicesFavourited().size()) {
                    isServicesTheSame = false;
                } else {
                    for (int i = 0; i < listOfPreviousFavourites.size(); i++) {
                        Log.e("compare service", listOfPreviousFavourites.get(i) + " " + singleStopArrivalNotifications.getServicesFavourited().get(i));
                        if (!listOfPreviousFavourites.get(i).equals(singleStopArrivalNotifications.getServicesFavourited().get(i))) {
                            isServicesTheSame = false;
                            break;
                        }
                    }
                }
                if (!isServicesTheSame) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "You'll see your changes to Favourites the next time you return to the Homepage.",
                            Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(R.id.nav_view);
                    snackbar.show();
                }
                favouriteDatabase.favouriteStopCRUD().updateData(favouriteStop);
            } else {
                favouriteDatabase.favouriteStopCRUD().addData(favouriteStop);
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "You'll see your changes to Favourites the next time you return to the Homepage.",
                        Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(R.id.nav_view);
                snackbar.show();
            }
        } else {
            FavouriteStop favouriteStop = new FavouriteStop();
            favouriteStop.setStopId(singleStopArrivalNotifications.getStopId());
            if (favouriteDatabase.favouriteStopCRUD().isFavorite(favouriteStop.getStopId()) == 1) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "You'll see your changes to Favourites the next time you return to the Homepage.",
                        Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(R.id.nav_view);
                snackbar.show();
            }
            favouriteDatabase.favouriteStopCRUD().delete(favouriteStop);
        }

        //for arrival monitoring checks
        List<ServiceInStopDetails> forBoolListCheck = singleStopArrivalNotifications.getServicesAtStop();
        List<Boolean> boolList = new ArrayList<>();
        int i = 0;
        int j = 0;

        //checks which services need to be monitored
        while (singleStopArrivalNotifications.isWatchingForArrival() && i < singleStopArrivalNotifications.getServicesBeingWatched().size()
                && j < singleStopArrivalNotifications.getServicesAtStop().size()) {
            Log.e("checking in threshold for", forBoolListCheck.get(j).getServiceNum());
            if (forBoolListCheck.get(j).getServiceNum().equals(singleStopArrivalNotifications.getServicesBeingWatched().get(i))) {
                Log.e("matching", forBoolListCheck.get(j).getServiceNum());
                if (!forBoolListCheck.get(j).getFirstArrival().equals("-") && !forBoolListCheck.get(j).getFirstArrival().equals("Arr")
                        && (Integer.parseInt(forBoolListCheck.get(j).getFirstArrival()) > singleStopArrivalNotifications.getTimeToWatch())) {
                    boolList.add(true);
                }
                else if (forBoolListCheck.get(j).getFirstArrival().equals("-")){
                    boolList.add(true);
                } else {
                    boolList.add(false);
                }
                i++;
                j++;

            } else {
                j++;
            }
        }
        singleStopArrivalNotifications.setBeginWatching(boolList);

        NotificationCompat.Builder persistentBuilder = new NotificationCompat.Builder(this, getString(R.string.arrivalnotifications_monitoring_notif_id));
        Log.e("entered", "yes in activity");
        for (i = 0; i < arrivalNotificationsArray.size(); i++) {
            if (singleStopArrivalNotifications.getStopId().equals(arrivalNotificationsArray.get(i).getStopId())) {
                if (singleStopArrivalNotifications.isWatchingForArrival() && singleStopArrivalNotifications.getServicesBeingWatched().size() > 0) {
                    arrivalNotificationsArray.set(i, singleStopArrivalNotifications);
                    Log.e("stopname repeated is" , singleStopArrivalNotifications.getStopName());
                    stopRepeated = true;
                    startNewMonitoring = true;
                    break;
                } else {
                    arrivalNotificationsArray.set(i, singleStopArrivalNotifications);
                    stopRepeated = true;
                    startNewMonitoring = false;
                }
                break;
            }

        }
        if (!stopRepeated && singleStopArrivalNotifications.isWatchingForArrival() && singleStopArrivalNotifications.getServicesBeingWatched().size() > 0) {
            arrivalNotificationsArray.add(singleStopArrivalNotifications);
            startNewMonitoring = true;
            Log.e("stopname new is" , singleStopArrivalNotifications.getStopId() + " " + singleStopArrivalNotifications.getServicesBeingWatched());
        }
        if (startNewMonitoring) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Intent cancelMonitoringIntent = new Intent(this, NotificationBroadcast.class);
            cancelMonitoringIntent.setAction("CANCEL_MONITORING");
            cancelMonitoringIntent.setIdentifier(singleStopArrivalNotifications.getStopId());
            PendingIntent cancelMonitoringPendingIntent = PendingIntent.getBroadcast(this, 1, cancelMonitoringIntent, 0);

            persistentBuilder.setContentTitle("Monitoring " + singleStopArrivalNotifications.getStopName() + "...")
                    .setContentText("Please do not quit the app. "
                            + "We'll notify you when any of your selected buses are "
                            + singleStopArrivalNotifications.getTimeToWatch()
                            + " minute(s) away.")
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                    .setSound(null)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);
//                    .setContentIntent(cancelMonitoringPendingIntent)
//                    .addAction(0, "Stop Monitoring", cancelMonitoringPendingIntent);
            notificationManager.cancel(singleStopArrivalNotifications.getStopId(), 0);
            notificationManager.notify(singleStopArrivalNotifications.getStopId(), 0, persistentBuilder.build());

//            NotificationBroadcast nb = new NotificationBroadcast();
//            nb.mainSetter(this, notificationManager, arrivalNotificationsArray);
//
//
//            IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction("android.intent.action.ANY_ACTION");
//            this.registerReceiver(nb, intentFilter);

//            BroadcastReceiver br = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    Log.e("broadcast", "received");
//                    notificationManager.cancel(intent.getIdentifier(), 0);
//                    for (int i = 0; i < arrivalNotificationsArray.size(); i++) {
//                        if (arrivalNotificationsArray.get(i).getStopId().equals(intent.getIdentifier())) {
//                            ArrivalNotifications temp = arrivalNotificationsArray.get(i);
//                            temp.setWatchingForArrival(false);
//                            arrivalNotificationsArray.set(i, temp);
//                        }
//                    }
//                }
//            };
//
//            registerReceiver(br, new IntentFilter("CANCEL_MONITORING"));



        } else {
            notificationManager.cancel(singleStopArrivalNotifications.getStopId(), 0);
        }
    }

    @Override
    public void onDialogNegativeClick() {
        //do nothing
    }

    /**
     * This method is called once when the app starts. It initializes the handler that checks
     * for arrival data every 10 seconds. It leads into the {@link MainActivity#DoMonitoring()} method
     * which is called (once every 10 seconds) only when at least one active arrival alert is present.
     */
    private void BeginMonitoring() {
        monitoringHandler = new Handler();
        monitoringHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DoMonitoring();
                Log.e("domonitoring is active", "yes");
                monitoringHandler.postDelayed(this, 10000);
            }
        }, 10000);
    }

    /**
     * Checks which arrival alert is active, and if active, calls the
     * {@link MainActivity#updateMonitoringAndNotification(ArrivalNotifications, int) method.}
     */
    private void DoMonitoring() {
        try {
            if (arrivalNotificationsArray.size() > 0) {
                for (int i = 0; i < arrivalNotificationsArray.size(); i++) {
                    if (arrivalNotificationsArray.get(i).isWatchingForArrival()) {
                        Log.e("is monitoring", arrivalNotificationsArray.get(i).getStopId());
                        updateMonitoringAndNotification(arrivalNotificationsArray.get(i), i);
                    }
                }
            }
        } catch (NullPointerException e) {

        }
    }

    /**
     * Calls the NUS server to check for arrival data at the specified stop from the {@link ArrivalNotifications} object that
     * is passed in.  It then compares each service from the pulled data against services saved for monitoring. Afterward, it
     * calls the {@link MainActivity#DetermineMonitoringThresholdReached(ArrivalNotifications, List)} method to determine if the
     * monitoring threshold is reached, and if so, calls the {@link MainActivity#ChangeNotification(List, ArrivalNotifications)} and
     * {@link MainActivity#ChangeArrivalNotificationsArray(ArrivalNotifications)} methods.
     *
     * @param singleStopArrivalNotificationForUpdate the object containing the arrival notifications data for the selected stop to be compared
     * @param index the index of the stop to be compared within the {@link  MainActivity#arrivalNotificationsArray} array
     */
    public void updateMonitoringAndNotification(ArrivalNotifications singleStopArrivalNotificationForUpdate, int index) {
        Log.e("entered", "updatemonitoringandnotification");
        NextbusAPIs.callSingleStopInfo(mainActivity, getApplicationContext(), singleStopArrivalNotificationForUpdate.getStopId(), 0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
            @Override
            public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                int i = 0;
                int j = 0;
                while (i < singleStopArrivalNotificationForUpdate.getServicesBeingWatched().size()
                        && j < singleStopArrivalNotificationForUpdate.getServicesAtStop().size()) {
                    //check if service from most recent data pull is a service being monitored
                    if (singleStopArrivalNotificationForUpdate.getServicesAtStop().get(j).getServiceNum()
                            .equals(singleStopArrivalNotificationForUpdate.getServicesBeingWatched().get(i))) {
                        //check if service has an estimate time, is not at "Arr" timing, and has an arrival time greater than the set monitoring threshold.
                        //If true, it sets the monitoring boolean value to true
                        if (!arrivalNotificationsArray.get(index).getBeginWatching().get(i) && !servicesAllInfoAtStop.get(j).getFirstArrival().equals("-")
                                && !servicesAllInfoAtStop.get(j).getFirstArrival().equals("Arr")
                                && Integer.parseInt(servicesAllInfoAtStop.get(j).getFirstArrival()) >= singleStopArrivalNotificationForUpdate.getTimeToWatch()){
                            arrivalNotificationsArray.get(index).getBeginWatching().set(i, true);
                        }
                        i++;
                        j++;

                    } else {
                        j++;
                    }
                }
                List<String> returnInfo = DetermineMonitoringThresholdReached(singleStopArrivalNotificationForUpdate, servicesAllInfoAtStop);
                if (returnInfo != null) {
                    ChangeNotification(returnInfo, singleStopArrivalNotificationForUpdate);
                    ChangeArrivalNotificationsArray(singleStopArrivalNotificationForUpdate);
//                    monitoringHandler.removeCallbacksAndMessages(0);
                } else {
                    //?
                }
            }

            @Override
            public void onFailureSingleStop() {

            }
        });
//        getChildTimings(singleStopArrivalNotificationForUpdate.getStopId(), new MainActivity.VolleyCallBack() {
//            @Override
//            public void onSuccess() {
//                List<ServiceInStopDetails> monitoringAllServicesAtStop = servicesAllInfoAtStop;
//                int i = 0;
//                int j = 0;
//                while (i < singleStopArrivalNotificationForUpdate.getServicesBeingWatched().size()
//                        && j < singleStopArrivalNotificationForUpdate.getServicesAtStop().size()) {
//                    //check if service from most recent data pull is a service being monitored
//                    if (singleStopArrivalNotificationForUpdate.getServicesAtStop().get(j).getServiceNum()
//                            .equals(singleStopArrivalNotificationForUpdate.getServicesBeingWatched().get(i))) {
//                        //check if service has an estimate time, is not at "Arr" timing, and has an arrival time greater than the set monitoring threshold.
//                        //If true, it sets the monitoring boolean value to true
//                        if (!arrivalNotificationsArray.get(index).getBeginWatching().get(i) && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("-")
//                                && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr")
//                                && Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) >= singleStopArrivalNotificationForUpdate.getTimeToWatch()){
//                            arrivalNotificationsArray.get(index).getBeginWatching().set(i, true);
//                        }
//                        i++;
//                        j++;
//
//                    } else {
//                        j++;
//                    }
//                }
//                List<String> returnInfo = DetermineMonitoringThresholdReached(singleStopArrivalNotificationForUpdate, monitoringAllServicesAtStop);
//                if (returnInfo != null) {
//                    ChangeNotification(returnInfo, singleStopArrivalNotificationForUpdate);
//                    ChangeArrivalNotificationsArray(singleStopArrivalNotificationForUpdate);
////                    monitoringHandler.removeCallbacksAndMessages(0);
//                } else {
//                    //?
//                }
//            }
//        });
    }

    /**
     * Changes the monitoring notification from persistent to standard to inform the user that
     * the monitoring threshold has been breached.
     *
     * @param returnInfo list containing bus arrival info to be displayed on the notification
     * @param singleStopNotificationForUpdate object containing information on monitored stops and service(s)
     */
    private void ChangeNotification(List<String> returnInfo, ArrivalNotifications singleStopNotificationForUpdate) {
        NotificationCompat.Builder regularBuilder = new NotificationCompat.Builder(this, getString(R.string.arrivalnotifications_triggered_notif_id));
        if (returnInfo.get(1).equals("0")) {
            if (returnInfo.size() > 2) {
                regularBuilder.setContentTitle("Your monitored bus is arriving now!")
                        .setContentText("Service " + returnInfo.get(0) + " is arriving now at "
                                + singleStopNotificationForUpdate.getStopName() + ". "
                                + "The subsequent arrival is for Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minute(s).")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving now at " + singleStopNotificationForUpdate.getStopName() + ". "
                                        + "The subsequent arrival is Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minute(s)."));
            } else {
                regularBuilder.setContentTitle("Your monitored bus is arriving now!")
                        .setContentText("Service " + returnInfo.get(0) + " is arriving now at " + singleStopNotificationForUpdate.getStopName() + ".")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving now at " + singleStopNotificationForUpdate.getStopName() + "."));
            }
        } else if (returnInfo.get(1).equals("1")) {
            if (returnInfo.size() > 2) {
                regularBuilder.setContentTitle("Your monitored bus is arriving in " + returnInfo.get(1) + " minute!")
                        .setContentText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                        + " minute at " + singleStopNotificationForUpdate.getStopName() + ". "
                        + "The subsequent arrival is for Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minute(s).")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                                        + " minute at " + singleStopNotificationForUpdate.getStopName() + ". "
                                        + "The subsequent arrival is Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minute(s)."));
            } else {
                regularBuilder.setContentTitle("Your monitored bus is arriving in " + returnInfo.get(1) + " minute!")
                        .setContentText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                        + " minute at " + singleStopNotificationForUpdate.getStopName() + ".")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                                        + " minute at " + singleStopNotificationForUpdate.getStopName() + "."));
            }
        } else {
            if (returnInfo.size() > 2) {
                regularBuilder.setContentTitle("Your monitored bus is arriving in " + returnInfo.get(1) + " minutes!")
                        .setContentText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                        + " minutes at " + singleStopNotificationForUpdate.getStopName() + ". "
                        + "The subsequent arrival is for Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minutes.")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                                        + " minutes at " + singleStopNotificationForUpdate.getStopName() + ". "
                                        + "The subsequent arrival is Service " + returnInfo.get(2) + " in " + returnInfo.get(3) + " minutes."));
            } else {
                regularBuilder.setContentTitle("Your monitored bus is arriving in " + returnInfo.get(1) + " minutes!").
                        setContentText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                        + " minutes at " + singleStopNotificationForUpdate.getStopName() + ".")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Service " + returnInfo.get(0) + " is arriving in " + returnInfo.get(1)
                                        + " minutes at " + singleStopNotificationForUpdate.getStopName() + "."));
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        regularBuilder.setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(singleStopNotificationForUpdate.getStopId(), 0, regularBuilder.build());
    }

    /**
     * Changes the monitoring status of the stop where the threshold was breached in the array that stores monitoring info
     *
     * @param singleStopArrivalNotificationForUpdate object containing information on monitored stops and service(s)
     */
    private void ChangeArrivalNotificationsArray(ArrivalNotifications singleStopArrivalNotificationForUpdate) {
        for (int i = 0; i < arrivalNotificationsArray.size(); i++) {
            if (arrivalNotificationsArray.get(i).getStopId().equals(singleStopArrivalNotificationForUpdate.getStopId())) {
                arrivalNotificationsArray.get(i).setWatchingForArrival(false);
            }
        }
    }

    /**
     * Determines if the monitoring threshold is breached (i.e. if a service is arriving at or before the monitoring threshold)
     *
     * @param singleStopArrivalNotificationForUpdate object containing information on monitored stops and service(s)
     * @param monitoringAllServicesAtStop list of services which are being monitored, and their arrival info
     * @return list containing service which triggered the monitoring threshold and its arrival time, and the subsequent arrival service and its arrival time
     */
    private List<String> DetermineMonitoringThresholdReached(ArrivalNotifications singleStopArrivalNotificationForUpdate,
                                                             List<ServiceInStopDetails> monitoringAllServicesAtStop) {
        int i = 0;
        int j = 0;
        while (i < singleStopArrivalNotificationForUpdate.getServicesBeingWatched().size() && j < monitoringAllServicesAtStop.size()) {
            Log.e("checking in threshold for", monitoringAllServicesAtStop.get(j).getServiceNum());
            if (monitoringAllServicesAtStop.get(j).getServiceNum().equals(singleStopArrivalNotificationForUpdate.getServicesBeingWatched().get(i))) {
                Log.e("matching", monitoringAllServicesAtStop.get(j).getServiceNum());
                if ((!monitoringAllServicesAtStop.get(j).getFirstArrival().equals("-") && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr")
                        && singleStopArrivalNotificationForUpdate.getBeginWatching().get(i)
                        && (Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) <= singleStopArrivalNotificationForUpdate.getTimeToWatch()))
                        || (singleStopArrivalNotificationForUpdate.getBeginWatching().get(i) && monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr"))) {
                    Log.e("timing match", "yes");
                    List<String> returnInfo = new ArrayList<>();
                    returnInfo.add(monitoringAllServicesAtStop.get(j).getServiceNum());
                    if (monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr")) {
                        returnInfo.add("0");
                    } else {
                        returnInfo.add(monitoringAllServicesAtStop.get(j).getFirstArrival());
                    }
                    i = 0;
                    j = 0;
                    List<String> additionReturnInfo = AdditionalThresholdInformation(singleStopArrivalNotificationForUpdate,
                            monitoringAllServicesAtStop, monitoringAllServicesAtStop.get(j).getServiceNum());
                    if (additionReturnInfo != null) {
                        returnInfo.add(additionReturnInfo.get(0));
                        returnInfo.add(additionReturnInfo.get(1));
                    }
                    return returnInfo;
                } if (!monitoringAllServicesAtStop.get(j).getSecondArrival().equals("-") && !monitoringAllServicesAtStop.get(j).getSecondArrival().equals("Arr")
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getSecondArrival()) <= singleStopArrivalNotificationForUpdate.getTimeToWatch()) {
                    List<String> returnInfo = new ArrayList<>();
                    returnInfo.add(monitoringAllServicesAtStop.get(j).getServiceNum());
                    if (monitoringAllServicesAtStop.get(j).getSecondArrival().equals("Arr")) {
                        returnInfo.add("0");
                    } else {
                        returnInfo.add(monitoringAllServicesAtStop.get(j).getSecondArrival());
                    }
                    i = 0;
                    j = 0;
                    List<String> additionReturnInfo = AdditionalThresholdInformation(singleStopArrivalNotificationForUpdate,
                            monitoringAllServicesAtStop, monitoringAllServicesAtStop.get(j).getServiceNum());
                    if (additionReturnInfo != null) {
                        returnInfo.add(additionReturnInfo.get(0));
                        returnInfo.add(additionReturnInfo.get(1));
                    }
                    return returnInfo;
                }
                i++;
                j++;

            } else {
                j++;
            }
        }
        return null;
    }

    /**
     * Determines the service no. & time of the immediate subsequent arrival after the arrival
     * which triggered the threshold breach.
     *
     * @param singleStopArrivalNotificationForUpdate object containing information on monitored stops and service(s)
     * @param monitoringAllServicesAtStop list of services which are being monitored, and their arrival info
     * @param serviceMatched service which triggered the initial threshold breach - present to prevent re-matching
     * @return list containing the selected service number and arrival time
     */
    private List<String> AdditionalThresholdInformation(ArrivalNotifications singleStopArrivalNotificationForUpdate,
                                                        List<ServiceInStopDetails> monitoringAllServicesAtStop,
                                                        String serviceMatched) {
        int i = 0;
        int j = 0;
        int nextEarliestArrival = 1000;
        String nextServiceWithEarliestArrival = "";
        while (i < singleStopArrivalNotificationForUpdate.getServicesBeingWatched().size() && j < monitoringAllServicesAtStop.size()) {
            Log.e("checking in threshold for", monitoringAllServicesAtStop.get(j).getServiceNum());
            if (monitoringAllServicesAtStop.get(j).getServiceNum().equals(singleStopArrivalNotificationForUpdate.getServicesBeingWatched().get(i))) {
                Log.e("matching", monitoringAllServicesAtStop.get(j).getServiceNum());
                if (!monitoringAllServicesAtStop.get(j).getFirstArrival().equals("-") && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr")
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) > singleStopArrivalNotificationForUpdate.getTimeToWatch()
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) < nextEarliestArrival
                        && !monitoringAllServicesAtStop.get(j).getServiceNum().equals(serviceMatched)) {
                    nextEarliestArrival = Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival());
                    nextServiceWithEarliestArrival = monitoringAllServicesAtStop.get(j).getServiceNum();
                } else if (!monitoringAllServicesAtStop.get(j).getSecondArrival().equals("-") && !monitoringAllServicesAtStop.get(j).getSecondArrival().equals("Arr")
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getSecondArrival()) > singleStopArrivalNotificationForUpdate.getTimeToWatch()
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getSecondArrival()) < nextEarliestArrival) {
                    nextEarliestArrival = Integer.parseInt(monitoringAllServicesAtStop.get(j).getSecondArrival());
                    nextServiceWithEarliestArrival = monitoringAllServicesAtStop.get(j).getServiceNum();
                }
                i++;
                j++;
            } else {
                j++;
            }
        }
        List<String> returnInfo = new ArrayList<>();
        if (nextEarliestArrival == 1000) {
            return null;
        }
        returnInfo.add(nextServiceWithEarliestArrival);
        returnInfo.add(nextEarliestArrival + "");
        return returnInfo;
    }


    public List<ArrivalNotifications> getArrivalNotificationsArray() {
        return arrivalNotificationsArray;
    }

    public void setArrivalNotificationsArray(List<ArrivalNotifications> arrivalNotificationsArray) {
        this.arrivalNotificationsArray = arrivalNotificationsArray;
    }

    public List<StopList> getListOfAllStops() {
        return listOfAllStops;
    }

    public void setListOfAllStops(List<StopList> listOfAllStops) {
        this.listOfAllStops = listOfAllStops;
    }

    public String getFirstPassStopsList() {
        return firstPassStopsList;
    }

    public void setFirstPassStopsList(String firstPassStopsList) {
        this.firstPassStopsList = firstPassStopsList;
    }

    //variables and methods for global navigation information
    NavigationSearchInfo navigationSearchInfo;

    public NavigationSearchInfo getNavigationSearchInfo() {
        return navigationSearchInfo;
    }

    public void setNavigationSearchInfo(NavigationSearchInfo navigationSearchInfo) {
        this.navigationSearchInfo = navigationSearchInfo;
    }

    private NavigationResults navResultSingle;
    private String origin, dest;

    public void setNavResultSingle(NavigationResults navResultSingle, String origin, String dest) {
        this.navResultSingle = navResultSingle;
        this.origin = origin;
        this.dest = dest;
    }

    public NavigationResults getNavResultSingle() {
        return navResultSingle;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDest() {
        return dest;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    public class NotificationBroadcast extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//
//    }
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