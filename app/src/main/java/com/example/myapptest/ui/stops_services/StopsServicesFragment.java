package com.example.myapptest.ui.stops_services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
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
import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jayway.jsonpath.JsonPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopsServicesFragment extends Fragment {

    // EXPANDABLE LIST DECLARATIONS
    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;

    LocationManager locationManager;
    LocationManager secondLocationManager;
    Location userLocation = new Location("");
    boolean isLocationPermissionGranted = false;
    boolean isFirstRun = true;
    boolean searchingLocation = true;

    View viewForFragment;

    //to store arrival data
    List<ArrivalNotifications> arrivalNotificationsArray;
    ArrivalNotifications singleStopArrivalNotification;

    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_stops_services, container, false);

        view.findViewById(R.id.StopsNUSMainLoadingProgressBar).setVisibility(View.VISIBLE);

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview_nus_stops);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        secondLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

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
            if (location.getAccuracy() < 25) {
                userLocation = location;
                secondLocationManager.removeUpdates(this);
                locationManager.removeUpdates(gpsLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                if (isFirstRun == true) {
                    getListOfGroupStops();
                } else {
                    initListData();
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
            if (location.getAccuracy() < 25) {
                userLocation = location;
                locationManager.removeUpdates(this);
                secondLocationManager.removeUpdates(networkLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                if (isFirstRun == true) {
                    getListOfGroupStops();
                } else {
                    initListData();
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
            if (isFirstRun == true) {
                getListOfGroupStops();
            } else {
                initListData();
            }
        }

    }

    boolean progressBarInvisible = true;
    FloatingActionButton floatingRefreshButton;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewForFragment = view;

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    getListOfChildServices(groupPosition, true, new VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            expandableListView.expandGroup(groupPosition, true);
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
                    Snackbar snackbar = Snackbar.make(view,
                            "Testing Group Long Click " + groupPosition + "" ,
                            Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(R.id.textView_container);
                    snackbar.show();
                    boolean isStopBeingWatched = false;
                    for (ArrivalNotifications temp : arrivalNotificationsArray) {
                        if (temp.getStopId() == listOfAllStops.get(groupPosition).getStopId() && temp.isWatchingForArrival()) {
                            isStopBeingWatched = true;
                            break;
                            //TODO: break, pull existing watch info to programmatically set toggles/checkboxes and do not instantiate new singleStopArrivalNotification
                        }
                    }
                    if (!isStopBeingWatched) {
                        singleStopArrivalNotification = new ArrivalNotifications();
                        singleStopArrivalNotification.setStopId(listOfAllStops.get(groupPosition).getStopId());
                        singleStopArrivalNotification.setWatchingForArrival(false);
                        try {
                            singleStopArrivalNotification.setServicesAtStop(listItem.get(listGroup.get(groupPosition)));
                        } catch (NullPointerException e) {
                            getListOfChildServices(groupPosition, false, new VolleyCallBack() {
                                @Override
                                public void onSuccess() {
                                    singleStopArrivalNotification.setServicesAtStop(listItem.get(listGroup.get(groupPosition)));
                                }
                            });
                        }
                    }
                    return true;
                }
                return false;
            }
        });

//        Button refreshButton = view.findViewById(R.id.floating_refresh_button);
//        refreshButton.setOnClickListener(new View.OnClickListener());

        floatingRefreshButton = view.findViewById(R.id.floating_refresh_button);
        ProgressBar refreshTimingProgressBar = view.findViewById(R.id.progressBar_refreshTiming);
        floatingRefreshButton.setImageResource(R.drawable.ic_outline_cancel_24);
//        FloatingActionButton floatingGetLocationButton = view.findViewById(R.id.floating_refresh_location_button);
//        ProgressBar refreshLocationProgressBar = view.findViewById(R.id.progressBar_refreshLocation);

        floatingRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                floatingGetLocationButton.setClickable(false);
                if (searchingLocation && isFirstRun) {
                    locationManager.removeUpdates(gpsLocationListener);
                    secondLocationManager.removeUpdates(networkLocationListener);
                    searchingLocation = false;
                    floatingRefreshButton.setClickable(false);
                    getListOfGroupStops();
                } else if (searchingLocation) {
                    locationManager.removeUpdates(gpsLocationListener);
                    secondLocationManager.removeUpdates(networkLocationListener);
                    floatingRefreshButton.setClickable(false);
                    searchingLocation = false;
                    refreshTimings(true, view);
                } else {
                    floatingRefreshButton.setImageResource(R.drawable.ic_outline_cancel_24);
                    refreshTimingProgressBar.setVisibility(View.VISIBLE);
                    refreshTimingProgressBar.bringToFront();
                    progressBarInvisible = false;
                    isFirstRun = false;
                    checkLocationPermission();
                    refreshTimings(true, view);
                }

            }
        });

//        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().equals(Lifecycle.Event.ON_RESUME))


//        floatingGetLocationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                floatingRefreshButton.setClickable(false);
//                floatingGetLocationButton.setClickable(false);
//                isFirstRun = false;
//                refreshLocationProgressBar.setVisibility(View.VISIBLE);
//                refreshLocationProgressBar.bringToFront();
//                checkLocationPermission();
//
//            }
//        });

    }

    Handler timeRefreshHandler = new Handler(Looper.getMainLooper());


//    @Override
//    public void onPause() {
//        ProcessLifecycleOwner
//        super.onPause();
//
//    }

    int i;

    private void refreshTimings(boolean isOnClick, @NotNull View view) {
        ProgressBar refreshTimingProgressBar = view.findViewById(R.id.progressBar_refreshTiming);
        for (i = 0; i < listOfAllStops.size(); i++) {
            if (expandableListView.isGroupExpanded(i)) {
                getListOfChildServices(i, true, new VolleyCallBack() {
                    @Override
                    public void onSuccess() {
//                        if (i == listOfAllStops.size() - 1) {
//                            progressBarInvisible = true;
//                            setRefreshCircleInvisible(refreshTimingProgressBar, 800);
//                        }
                    }
                });
            }
        }
        if (!searchingLocation) {
            setRefreshCircleInvisible(refreshTimingProgressBar, 800);
        }
    }

    private void setRefreshCircleInvisible(ProgressBar refreshTimingProgressBar, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (getContext() != null) {
                    floatingRefreshButton.setClickable(true);
                    refreshTimingProgressBar.setVisibility(View.INVISIBLE);
                    floatingRefreshButton.setImageResource(R.drawable.ic_baseline_refresh_24);
                }
//                getActivity().findViewById(R.id.floating_refresh_location_button).setClickable(true);
            }
        }, delay);
        progressBarInvisible = true;
    }

    List<StopList> listOfAllStops;
    StopList listOfStops;
    List<String> listOfNames;
    List<String> listOfIds;
    List<Double> listOfLat;
    List<Double> listOfLong;
    String jsonRaw;

    private void getListOfGroupStops() {

        floatingRefreshButton.setClickable(false);

        Log.e("im here", "yes");

        String url = "https://nnextbus.nus.edu.sg/BusStops";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("GetBusStopsList response is", response);
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
                initListData();
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

        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
        requestQueue.add(stringRequest);

    }

    ServiceInStopDetails list;
    List<ServiceInStopDetails> listList;

    int j;

    private void initListData() {

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
                    Collections.swap(listOfAllStops, i, nearestToUserIndex);
                }
            }
        }
        listGroup.clear();
        for (i = 0; i < listOfAllStops.size(); i++) {
//            Log.e("stop caption:", captions.get(i));
//            Log.e("i is: ", "" + i);
            listGroup.add(listOfAllStops.get(i));
        }

        Log.e("isfirstrun is:" , "" + isFirstRun);

        ProgressBar stopsNUSMainLoadingProgressBar = getActivity().findViewById(R.id.StopsNUSMainLoadingProgressBar);

        if (isFirstRun == false) {
            adapter.notifyDataSetChanged();
            refreshTimings(true, viewForFragment);
            stopsNUSMainLoadingProgressBar.setVisibility(View.INVISIBLE);
        } else {
            arrivalNotificationsArray = new ArrayList<>();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopsNUSMainLoadingProgressBar.setVisibility(View.INVISIBLE);
                    floatingRefreshButton.setImageResource(R.drawable.ic_baseline_refresh_24);
                    floatingRefreshButton.setClickable(true);
//                    getActivity().findViewById(R.id.floating_refresh_location_button).setClickable(true);
                    adapter.notifyDataSetChanged();
                }
            }, 400);
        }
        isFirstRun = false;
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && isFirstRun == false) {
            timeRefreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshTimings(false, viewForFragment);
                    timeRefreshHandler.postDelayed(this, 20000);
                }
            }, 20000);
        }
    }



    private void getListOfChildServices(int groupPosition, boolean isOnClick, final VolleyCallBack callback) {


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