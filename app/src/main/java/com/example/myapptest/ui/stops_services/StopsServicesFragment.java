package com.example.myapptest.ui.stops_services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.ExpandableListViewStandardCode;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.favourites.FavouriteStop;
import com.example.myapptest.ui.BusLocationDisplayDialogFragment;
import com.example.myapptest.ui.StopsMainAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jayway.jsonpath.JsonPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StopsServicesFragment extends Fragment {

    // EXPANDABLE LIST DECLARATIONS
    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;

    //for location permissions and locating
    LocationManager locationManager;
    LocationManager secondLocationManager;
    Location userLocation = new Location("");
    boolean isLocationPermissionGranted = false;
    boolean isFirstRun = true;
    boolean searchingLocation = true;

    View viewForFragment;

    //to store arrival data for setting arrival notifications
    List<ArrivalNotifications> arrivalNotificationsArray = new ArrayList<>();
    ArrivalNotifications singleStopArrivalNotification;

    FloatingActionButton floatingGetLocationButton;
    ProgressBar refreshLocationProgressBar, stopsNUSMainLoadingProgressBar, singleStopClickedProgressBar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_stops_services, container, false);

        view.findViewById(R.id.StopsNUSMainLoadingProgressBar).setVisibility(View.VISIBLE);

        floatingRefreshButton = view.findViewById(R.id.floating_refresh_button);
        floatingRefreshButton.setImageResource(R.drawable.ic_outline_cancel_24);
        stopsNUSMainLoadingProgressBar = view.findViewById(R.id.StopsNUSMainLoadingProgressBar);
        singleStopClickedProgressBar = view.findViewById(R.id.singleStopClickedProgressBar);

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview_nus_stops);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        secondLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        floatingGetLocationButton = view.findViewById(R.id.floating_refresh_location_button);
        refreshLocationProgressBar = view.findViewById(R.id.progressBar_refreshLocation);

        checkLocationPermission();


        return view;
    }

    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        } else {
            isLocationPermissionGranted = true;
            getUserLocationAndStopList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    isLocationPermissionGranted = true;
                    Log.e("im here", "");

                } else {
                    isLocationPermissionGranted = false;
                }
                getUserLocationAndStopList();
                return;
            }
        }
    }

    LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e("network location is: ", location + "");
            if (location.getAccuracy() < 30) {
                userLocation = location;
                secondLocationManager.removeUpdates(this);
                locationManager.removeUpdates(gpsLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                if (isFirstRun) {
                    getListOfGroupStops();
                } else {
                    initListData(listOfAllStops);
                }
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e("gps location is: ", location + "");
            if (location.getAccuracy() < 30) {
                userLocation = location;
                locationManager.removeUpdates(this);
                secondLocationManager.removeUpdates(networkLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                if (isFirstRun) {
                    getListOfGroupStops();
                } else {
                    initListData(listOfAllStops);
                }
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void getUserLocationAndStopList() {
        try {
            searchingLocation = true;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.e("im here before null", " yes");
                isLocationPermissionGranted = true;
//                userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                if (userLocation == null || userLocation.getTime() - ) {
//                Log.e("im here in null", "");
//                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, null);
//                Criteria criteriaForLocation = new Criteria();
////                criteriaForLocation.setAccuracy(Criteria.);
//                String bestProvider = locationManager.getBestProvider(criteriaForLocation, true);
//                Log.e("bestProvider is: ", bestProvider);
                secondLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
            } else if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                userLocation.setLatitude(0.0);
                userLocation.setLongitude(0.0);
                isLocationPermissionGranted = false;
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                        "Please check that your Location setting is switched on and set to High Accuracy.",
                        Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(R.id.nav_view);
                snackbar.show();
                searchingLocation = false;
                if (isFirstRun) {
                    getListOfGroupStops();
                } else {
                    initListData(listOfAllStops);
                }
            }

        } catch (SecurityException e) {
//            Log.e("SecExp", "yes");
            userLocation.setLatitude(0.0);
            userLocation.setLongitude(0.0);
            isLocationPermissionGranted = false;
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "To sort the list of bus stops by proximity, please enable location permissions.",
                    Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.textView_container);
            snackbar.show();
            if (isFirstRun) {
                getListOfGroupStops();
            } else {
                initListData(listOfAllStops);
            }
        }

    }

    boolean progressBarInvisible = true;
    FloatingActionButton floatingRefreshButton;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewForFragment = view;

        ProgressBar refreshTimingProgressBar = view.findViewById(R.id.progressBar_refreshTiming);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    singleStopClickedProgressBar.setVisibility(View.VISIBLE);
                    getListOfChildServices(groupPosition, true, new VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            expandableListView.expandGroup(groupPosition, true);
                            refreshTimingProgressBar.setClickable(true);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    singleStopClickedProgressBar.setVisibility(View.GONE);
                                }
                            }, 600);
                        }
                    });
                }
                return true;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    SetArrivalNotificationsDialogFragment dialogFragment;
                    boolean isStopBeingWatched = false;
                    arrivalNotificationsArray = ((MainActivity) getActivity()).getArrivalNotificationsArray();
                    for (int i = 0; arrivalNotificationsArray != null && i < arrivalNotificationsArray.size(); i++) {
                        if (arrivalNotificationsArray.get(i).getStopId().equals(listOfAllStops.get(groupPosition).getStopId())
                                && arrivalNotificationsArray.get(i).isWatchingForArrival()) {
                            Log.e("entered", "yes i  entered " + arrivalNotificationsArray.get(i).getStopId());
                            isStopBeingWatched = true;
                            singleStopArrivalNotification = new ArrivalNotifications();
                            singleStopArrivalNotification.setStopId(arrivalNotificationsArray.get(i).getStopId());
                            singleStopArrivalNotification.setStopName(arrivalNotificationsArray.get(i).getStopName());
                            singleStopArrivalNotification.setLatitude(arrivalNotificationsArray.get(i).getLatitude());
                            singleStopArrivalNotification.setLongitude(arrivalNotificationsArray.get(i).getLongitude());
                            singleStopArrivalNotification.setWatchingForArrival(true);
                            singleStopArrivalNotification.setServicesBeingWatched(arrivalNotificationsArray.get(i).getServicesBeingWatched());
                            singleStopArrivalNotification.setServicesAtStop(arrivalNotificationsArray.get(i).getServicesAtStop());
                            ExpandableListViewStandardCode.updateFavouritesInfo(singleStopArrivalNotification);
                            dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                            dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
                            dialogFragment.show(getChildFragmentManager(), SetArrivalNotificationsDialogFragment.TAG);
                            break;
                        }
                    }
                    if (!isStopBeingWatched) {
                        singleStopArrivalNotification = new ArrivalNotifications();
                        singleStopArrivalNotification.setStopId(listOfAllStops.get(groupPosition).getStopId());
                        singleStopArrivalNotification.setStopName(listOfAllStops.get(groupPosition).getStopName());
                        singleStopArrivalNotification.setLatitude(listOfAllStops.get(groupPosition).getStopLatitude());
                        singleStopArrivalNotification.setLongitude(listOfAllStops.get(groupPosition).getStopLongitude());
                        singleStopArrivalNotification.setWatchingForArrival(false);
                        ExpandableListViewStandardCode.updateFavouritesInfo(singleStopArrivalNotification);
                        if (listItem.get(listGroup.get(groupPosition)) != null) {
                            Log.e("listitem this pos is:", listItem.get(listGroup.get(groupPosition)) + "");
                            singleStopArrivalNotification.setServicesAtStop(listItem.get(listGroup.get(groupPosition)));
                            dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                            dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
                            dialogFragment.show(getChildFragmentManager(), SetArrivalNotificationsDialogFragment.TAG);
                        } else {
                            getListOfChildServices(groupPosition, false, new VolleyCallBack() {
                                @Override
                                public void onSuccess() {
                                    Log.e("listitem this pos after refresh is:", listItem.get(listGroup.get(groupPosition)) + "");
                                    singleStopArrivalNotification.setServicesAtStop(listItem.get(listGroup.get(groupPosition)));
                                    SetArrivalNotificationsDialogFragment dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                                    dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
//                                    dialogFragment.setTargetFragment(StopsServicesFragment.this, 0);
                                    dialogFragment.show(getChildFragmentManager(), SetArrivalNotificationsDialogFragment.TAG);
                                }
                            });
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        floatingRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingRefreshButton.setImageResource(R.drawable.ic_outline_cancel_24);
                refreshTimingProgressBar.setVisibility(View.VISIBLE);
                refreshTimingProgressBar.bringToFront();
                floatingRefreshButton.setClickable(false);
                floatingGetLocationButton.setClickable(false);
                Log.e("refresh button", "clicked");
                floatingGetLocationButton.setClickable(false);
                if (searchingLocation && isFirstRun) {
                    locationManager.removeUpdates(gpsLocationListener);
                    secondLocationManager.removeUpdates(networkLocationListener);
                    refreshTimingProgressBar.setVisibility(View.INVISIBLE);
                    searchingLocation = false;
                    getListOfGroupStops();
                } else if (searchingLocation) {
                    locationManager.removeUpdates(gpsLocationListener);
                    secondLocationManager.removeUpdates(networkLocationListener);
                    searchingLocation = false;
                    refreshTimings(true, view);
                } else {
                    progressBarInvisible = false;
                    isFirstRun = false;
//                    checkLocationPermission();
                    refreshTimings(true, view);
                }

            }
        });

        floatingGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchingLocation) {
                    locationManager.removeUpdates(gpsLocationListener);
                    secondLocationManager.removeUpdates(networkLocationListener);
                    searchingLocation = false;
                    floatingGetLocationButton.setImageResource(R.drawable.ic_baseline_my_location_24);
                    refreshLocationProgressBar.setVisibility(View.INVISIBLE);
                    floatingRefreshButton.setClickable(true);
                    floatingGetLocationButton.setClickable(true);
                } else {
                    floatingGetLocationButton.setImageResource(R.drawable.ic_outline_cancel_24);
                    searchingLocation = true;
                    floatingRefreshButton.setClickable(false);
                    isFirstRun = false;
                    refreshLocationProgressBar.setVisibility(View.VISIBLE);
                    refreshLocationProgressBar.bringToFront();
                    checkLocationPermission();
                }
            }
        });

        floatingGetLocationButton.setClickable(false);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                Log.e("clicklistener", "yes");

                if (listItem.get(listGroup.get(groupPosition)) != null) {
                    String serviceNum = listItem.get(listGroup.get(groupPosition)).get(childPosition).getServiceNum();
                    String stopName = listGroup.get(groupPosition).getStopName();
                    BusLocationDisplayDialogFragment dialogFragment = BusLocationDisplayDialogFragment.newInstance(serviceNum, stopName);
                    dialogFragment.show(Objects.requireNonNull(getChildFragmentManager()), BusLocationDisplayDialogFragment.TAG);
                }

                return true;
            }
        });

    }

    Handler timeRefreshHandler = new Handler(Looper.getMainLooper());

    int i;

    /**
     * Executes one attempt to pull stop information from NUS servers for each parent in the
     * ExpandableListView that is expanded. When all stops that are expanded have been refreshed,
     * method {@link StopsServicesFragment#setRefreshCircleInvisible(ProgressBar, int)} is called to
     * set ProgressBar to invisible, only if refresh was user-initiated.
     *
     * @param isOnClick to determine if refresh is user-initiated
     * @param view parent view of fragment
     */
    private void refreshTimings(boolean isOnClick, @NotNull View view) {
        ProgressBar refreshTimingProgressBar = view.findViewById(R.id.progressBar_refreshTiming);
        final int[] numExpanded = {0};
        for (int j = 0; j < listOfAllStops.size(); j++) {
            if (expandableListView.isGroupExpanded(j)) {
                numExpanded[0]++;
            }
        }
        if (numExpanded[0] == 0) {
            setRefreshCircleInvisible(refreshTimingProgressBar, 800);
        } else {
            for (i = 0; i < listOfAllStops.size(); i++) {
                if (expandableListView.isGroupExpanded(i)) {
                    getListOfChildServices(i, true, new VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            numExpanded[0]--;
                            if (numExpanded[0] == 0) {
                                if (!searchingLocation && isOnClick) {
                                    setRefreshCircleInvisible(refreshTimingProgressBar, 800);
                                }
                            }
                        }
                    });
                }
            }
        }

    }

    /**
     * Sets the ProgressBar around the refresh FloatingActionButton to View.INVISIBLE
     *
     * @param refreshTimingProgressBar ProgressBar that displays when user clicks to refresh time
     * @param delay delay duration for handler runnable
     */
    private void setRefreshCircleInvisible(ProgressBar refreshTimingProgressBar, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (getContext() != null) {
                    floatingRefreshButton.setClickable(true);
                    floatingGetLocationButton.setClickable(true);
                    refreshTimingProgressBar.setVisibility(View.INVISIBLE);
                    floatingRefreshButton.setImageResource(R.drawable.ic_baseline_refresh_24);
                }
//                getActivity().findViewById(R.id.floating_refresh_location_button).setClickable(true);
            }
        }, delay);
        progressBarInvisible = true;
    }

    //for getting full list of stops (parent only)
    List<StopList> listOfAllStops;
    List<StopList> listOfStopsRetrieved;

    /**
     * Attempts to retrieve the list of stops (as a {@link List<StopList>} from {@link MainActivity}. If failed,
     * proceeds to {@link StopsServicesFragment#RePullStopsList()} to retrieve list again from servers. If
     * successful, proceeds to {@link StopsServicesFragment#initListData(List)}.
     */
    private void getListOfGroupStops() {

        floatingRefreshButton.setClickable(false);

        try {
            listOfStopsRetrieved = ((MainActivity) getActivity()).getListOfAllStops();
            if (listOfStopsRetrieved != null) {
                ((MainActivity) getActivity()).setListOfAllStops(listOfStopsRetrieved);
                listOfAllStops = listOfStopsRetrieved;
                initListData(listOfAllStops);
            } else {
                RePullStopsList();
            }
        } catch (NullPointerException e) {
            RePullStopsList();
        }
    }

    /**
     * Calls the NextBusAPI to pull list of all stops using
     * {@link NextbusAPIs#callStopsList(Activity, Context, NextbusAPIs.VolleyCallBackAllStops)}. If successfully
     * pulled, method continues to {@link StopsServicesFragment#initListData(List)}.
     */
    private void RePullStopsList() {

        NextbusAPIs.callStopsList(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackAllStops() {
            @Override
            public void onSuccessAllStops(List<StopList> listOfAllStops) {
                ((MainActivity) getActivity()).setListOfAllStops(listOfAllStops);
                initListData(listOfAllStops);
            }

            @Override
            public void onFailureAllStops() {
                //TODO: show snackbar
            }
        });

    }

    int j;
    boolean didOrderChange = false;

    /**
     * Sorts {@link StopsServicesFragment#listOfAllStops} by distance from user (if location was
     * successfully found), then adds {@link StopsServicesFragment#listOfAllStops} to {@link StopsServicesFragment#listGroup}.
     * Then, all ProgressBars are set to {@link View#INVISIBLE}, all FloatingActionButtons are set to clickable, and if
     * {@link StopsServicesFragment#isFirstRun} is true, the auto-refresh handler {@link StopsServicesFragment#timeRefreshHandler} is
     * started.
     *
     * @param listOfAllStops list of StopList objects, each being a stop in the network
     */
    private void initListData(List<StopList> listOfAllStops) {

        this.listOfAllStops = listOfAllStops;

        //TODO: sort list of stops by distance
        Log.e("userLocation in initListData is: ", "" + userLocation);
        if (userLocation != null && (userLocation.getLatitude() != 0 && userLocation.getLongitude() != 0) && isLocationPermissionGranted) {
            for (i = 0; i < listOfAllStops.size(); i++) {
                Location stopLocation = new Location("");
                stopLocation.setLongitude(listOfAllStops.get(i).getStopLongitude());
                stopLocation.setLatitude(listOfAllStops.get(i).getStopLatitude());
                Float distanceToUser = userLocation.distanceTo(stopLocation);
                listOfAllStops.get(i).setDistanceFromUser(distanceToUser);
            }
            for (i = 0; i < listOfAllStops.size(); i++) {
                int nearestToUserIndex = i;
                for (j = i + 1; j < listOfAllStops.size(); j++) {
                    if (listOfAllStops.get(j).getDistanceFromUser() < listOfAllStops.get(nearestToUserIndex).getDistanceFromUser()) {
                        nearestToUserIndex = j;
                    }
                }
                if (nearestToUserIndex != i) {
                    didOrderChange = true;
                    Collections.swap(listOfAllStops, i, nearestToUserIndex);
                }
            }
            if (didOrderChange) {
                for (i = 0; i < listOfAllStops.size(); i++) {
                    expandableListView.collapseGroup(i);
                }
            }
            didOrderChange = false;
            TextView sortOrder = viewForFragment.findViewById(R.id.textView13);
            sortOrder.setText("Sorted by distance");
        } else {
            TextView sortOrder = viewForFragment.findViewById(R.id.textView13);
            sortOrder.setText("Sorted by A-Z");
        }
        listGroup.clear();
        for (i = 0; i < listOfAllStops.size(); i++) {
//            Log.e("stop caption:", captions.get(i));
//            Log.e("i is: ", "" + i);
            listGroup.add(listOfAllStops.get(i));
        }

        Log.e("isfirstrun is:" , "" + isFirstRun);


        if (!isFirstRun) {
            adapter.notifyDataSetChanged();
            floatingGetLocationButton.setClickable(true);
            floatingRefreshButton.setClickable(true);
//            refreshTimings(true, viewForFragment);
            stopsNUSMainLoadingProgressBar.setVisibility(View.INVISIBLE);
            refreshLocationProgressBar.setVisibility(View.INVISIBLE);
            floatingGetLocationButton.setImageResource(R.drawable.ic_baseline_my_location_24);
        } else {
            isFirstRun = false;
//            arrivalNotificationsArray = new ArrayList<>();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopsNUSMainLoadingProgressBar.setVisibility(View.INVISIBLE);
                    floatingGetLocationButton.setClickable(true);
                    refreshLocationProgressBar.setVisibility(View.INVISIBLE);
                    floatingRefreshButton.setImageResource(R.drawable.ic_baseline_refresh_24);
                    floatingRefreshButton.setClickable(true);
//                    getActivity().findViewById(R.id.floating_refresh_location_button).setClickable(true);
                    adapter.notifyDataSetChanged();
                }
            }, 400);
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                timeRefreshHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshTimings(false, viewForFragment);
                        timeRefreshHandler.postDelayed(this, 10000);
                    }
                }, 10000);
            }
        }

    }

    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;

    private void getListOfChildServices(int groupPosition, boolean isOnClick, final VolleyCallBack callback) {

        Log.e("null check for", listOfAllStops + "");

        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + listOfAllStops.get(groupPosition).getStopId();

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                servicesAllInfoAtStop = new ArrayList<>();
                Log.e("GetStopInfo response is", response);
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
//                Log.e("servicesAllInfoAtStop is: ", "" + servicesAllInfoAtStop);
//                Log.e("value of j is: ", "" + groupPosition);
                listItem.remove(listGroup.get(groupPosition));
                listItem.put(listGroup.get(groupPosition), servicesAllInfoAtStop);

                adapter.notifyDataSetChanged();
                callback.onSuccess();
                Log.e("listItem is: ", "" + listItem);

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
                params.put("Authorization", getActivity().getString(R.string.auth_header));
                return params;
            }
        };

        if (this.getContext() != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(this.getContext());
            stopRequestQueue.add(stopStringRequest);
        }

//        Log.e("list is: ", list.toString());
//        return list;
    }

    public interface VolleyCallBack {
        void onSuccess();
    }

}
