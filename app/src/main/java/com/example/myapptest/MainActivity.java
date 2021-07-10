package com.example.myapptest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.example.myapptest.data.naviagationdata.NavigationSearchInfo;
import com.example.myapptest.data.tutorial.IsFirstRuns;
import com.example.myapptest.data.tutorial.IsFirstRunsDatabase;
import com.example.myapptest.databinding.ActivityMainBinding;
import com.example.myapptest.favourites.FavouriteDatabase;
import com.example.myapptest.favourites.FavouriteStop;
import com.example.myapptest.ui.directions.DirectionsFragment;
import com.example.myapptest.ui.home.HomeFragment;
import com.example.myapptest.ui.stops_services.SetArrivalNotificationsDialogFragment;
import com.example.myapptest.ui.stops_services.StopsServicesMasterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
    public static IsFirstRunsDatabase isFirstRunsDatabase;

    //    private final StopsServicesMasterFragment stopsServicesMasterFragment = new StopsServicesMasterFragment();
//    private final DirectionsFragment directionsFragment = new DirectionsFragment();
//    private final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mainActivity = this;

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        navView.bringToFront();
        createNotificationChannel();

//        getStringOfGroupStops();
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
        isFirstRunsDatabase = Room.databaseBuilder(getApplicationContext(), IsFirstRunsDatabase.class, "isFirstRundb").allowMainThreadQueries().build();

        active = homeFragment;

        navView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);

        if (isFirstRunsDatabase.isFirstRunsCRUD().isFirstRunPresent() == 0) {
            IsFirstRuns isFirstRuns = new IsFirstRuns();
            isFirstRunsDatabase.isFirstRunsCRUD().addData(isFirstRuns);
        }

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
     * Creates notification channels (as required by Android)
     * for arrival alerts and persistent arrival notifications
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

            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.quite_impressed_565);
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

    private void getStringOfGroupStops() {

        String url = "https://nnextbus.nus.edu.sg/BusStops";

        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        firstPassStopsList = response;
                        listOfAllStops = new ArrayList<>();
                        listOfNames = JsonPath.read(response, "$.BusStopsResult.busstops[*].caption");
                        listOfIds = JsonPath.read(response, "$.BusStopsResult.busstops[*].name");
                        listOfLong = JsonPath.read(response, "$.BusStopsResult.busstops[*].longitude");
                        listOfLat = JsonPath.read(response, "$.BusStopsResult.busstops[*].latitude");
                        for (int i = 0; i < listOfNames.size(); i++) {
                            listOfStops = new StopList();
                            listOfStops.setStopName(listOfNames.get(i));
                            listOfStops.setStopId(listOfIds.get(i));
                            listOfStops.setStopLongitude(listOfLong.get(i));
                            listOfStops.setStopLatitude(listOfLat.get(i));
                            listOfAllStops.add(listOfStops);
                        }
                        Log.d("response is", response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("volley API error", "" + error);
                    }


                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", getString(R.string.auth_header));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        requestQueue.add(stringRequest);

    }

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
                        if (listOfPreviousFavourites.get(i).equals(singleStopArrivalNotifications.getServicesFavourited().get(i))) {
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
            if (singleStopArrivalNotifications.getStopId().equals(arrivalNotificationsArray.get(i).getStopId())
                    && singleStopArrivalNotifications.isWatchingForArrival() && singleStopArrivalNotifications.getServicesBeingWatched().size() > 0) {
                arrivalNotificationsArray.set(i, singleStopArrivalNotifications);
                Log.e("stopname repeated is" , singleStopArrivalNotifications.getStopName());
                stopRepeated = true;
                startNewMonitoring = true;
                break;
            }
        }
        if (!stopRepeated && singleStopArrivalNotifications.isWatchingForArrival() && singleStopArrivalNotifications.getServicesBeingWatched().size() > 0) {
            arrivalNotificationsArray.add(singleStopArrivalNotifications);
            startNewMonitoring = true;
            Log.e("stopname new is" , singleStopArrivalNotifications.getStopId() + " " + singleStopArrivalNotifications.getServicesBeingWatched());
        }
        if (startNewMonitoring) {
            persistentBuilder.setContentTitle("Monitoring " + singleStopArrivalNotifications.getStopName() + "...")
                    .setContentText("Please do not quit the app. "
                            + "We'll notify you when any of your selected buses are "
                            + singleStopArrivalNotifications.getTimeToWatch()
                            + " minute(s) away.")
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                    .setSound(null)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationManager.cancel(singleStopArrivalNotifications.getStopId(), 0);
            notificationManager.notify(singleStopArrivalNotifications.getStopId(), 0, persistentBuilder.build());
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
        getChildTimings(singleStopArrivalNotificationForUpdate.getStopId(), new MainActivity.VolleyCallBack() {
            @Override
            public void onSuccess() {
                List<ServiceInStopDetails> monitoringAllServicesAtStop = servicesAllInfoAtStop;
                int i = 0;
                int j = 0;
                while (i < singleStopArrivalNotificationForUpdate.getServicesBeingWatched().size()
                        && j < singleStopArrivalNotificationForUpdate.getServicesAtStop().size()) {
                    //check if service from most recent data pull is a service being monitored
                    if (singleStopArrivalNotificationForUpdate.getServicesAtStop().get(j).getServiceNum()
                            .equals(singleStopArrivalNotificationForUpdate.getServicesBeingWatched().get(i))) {
                        //check if service has an estimate time, is not at "Arr" timing, and has an arrival time greater than the set monitoring threshold.
                        //If true, it sets the monitoring boolean value to true
                        if (!arrivalNotificationsArray.get(index).getBeginWatching().get(i) && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("-")
                                && !monitoringAllServicesAtStop.get(j).getFirstArrival().equals("Arr")
                                && Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) >= singleStopArrivalNotificationForUpdate.getTimeToWatch()){
                            arrivalNotificationsArray.get(index).getBeginWatching().set(i, true);
                        }
                        i++;
                        j++;

                    } else {
                        j++;
                    }
                }
                List<String> returnInfo = DetermineMonitoringThresholdReached(singleStopArrivalNotificationForUpdate, monitoringAllServicesAtStop);
                if (returnInfo != null) {
                    ChangeNotification(returnInfo, singleStopArrivalNotificationForUpdate);
                    ChangeArrivalNotificationsArray(singleStopArrivalNotificationForUpdate);
//                    monitoringHandler.removeCallbacksAndMessages(0);
                } else {
                    //?
                }
            }
        });
    }

    /**
     *
     *
     * @param returnInfo
     * @param singleStopNotificationForUpdate
     */
    private void ChangeNotification(List<String> returnInfo, ArrivalNotifications singleStopNotificationForUpdate) {
        NotificationCompat.Builder regularBuilder = new NotificationCompat.Builder(this, getString(R.string.arrivalnotifications_triggered_notif_id));
        Log.e("entered", "changenotification");
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
        regularBuilder.setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(singleStopNotificationForUpdate.getStopId(), 0, regularBuilder.build());
    }

    private void ChangeArrivalNotificationsArray(ArrivalNotifications singleStopArrivalNotificationForUpdate) {
        for (int i = 0; i < arrivalNotificationsArray.size(); i++) {
            if (arrivalNotificationsArray.get(i).getStopId().equals(singleStopArrivalNotificationForUpdate.getStopId())) {
                arrivalNotificationsArray.get(i).setWatchingForArrival(false);
            }
        }
    }

    private List<String> DetermineMonitoringThresholdReached(ArrivalNotifications singleStopArrivalNotificationForUpdate, List<ServiceInStopDetails> monitoringAllServicesAtStop) {
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

    private List<String> AdditionalThresholdInformation(ArrivalNotifications singleStopArrivalNotificationForUpdate, List<ServiceInStopDetails> monitoringAllServicesAtStop, String serviceMatched) {
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
                        && Integer.parseInt(monitoringAllServicesAtStop.get(j).getFirstArrival()) < nextEarliestArrival && !monitoringAllServicesAtStop.get(j).getServiceNum().equals(serviceMatched)) {
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


    //variables and method for retrieving service info at a particular stop
    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;

    private void getChildTimings(String stopId, final MainActivity.VolleyCallBack callback) {

        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + stopId;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                servicesAllInfoAtStop = new ArrayList<>();
                Log.e("GetStopInfo in activity response is", response);
                servicesAtStop = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].name");
                serviceFirstArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime");
                serviceSecondArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime");
                firstArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime_veh_plate");
                secondArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime_veh_plate");
                Log.e("servicesAtStop is: ", servicesAtStop.get(0));
                for (int i = 0; i < servicesAtStop.size(); i++) {
                    serviceInfoAtStop = new ServiceInStopDetails();
                    serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
                    serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
                    Log.e("first arrival is: ", "" + serviceFirstArrival.get(i));
                    serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
                    serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
                    serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
                    servicesAllInfoAtStop.add(serviceInfoAtStop);
                }
                callback.onSuccess();

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", getString(R.string.auth_header));
                return params;
            }
        };

        if (this != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(this);
            stopRequestQueue.add(stopStringRequest);
        }

//        Log.e("list is: ", list.toString());
//        return list;
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

    public interface VolleyCallBack {
        void onSuccess();
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