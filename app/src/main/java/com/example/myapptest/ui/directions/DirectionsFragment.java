package com.example.myapptest.ui.directions;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.busrouteinformation.ServiceRoute;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.data.naviagationdata.NavigationGraph;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.example.myapptest.data.naviagationdata.NavigationSearchInfo;
import com.google.android.material.snackbar.Snackbar;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionsFragment extends Fragment {

    List<StopList> listOfBusStopsObject;
    String listOfBusStopsString;
    AppCompatAutoCompleteTextView destInputEditor;
    AppCompatAutoCompleteTextView originInputEditor;
//    TextView textViewIntermediate;
    RecyclerView resultRecyclerView;
    CustomAdapterRecyclerView customAdapterRecyclerView;
    List<NavigationResults> savedNavigationResults = new ArrayList<>();
    float dpWidth;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        destInputEditor = view.findViewById(R.id.destInputEditor);
        originInputEditor = view.findViewById(R.id.originInputEditor);

        resultRecyclerView = view.findViewById(R.id.resultrecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        resultRecyclerView.setLayoutManager(linearLayoutManager);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        dpWidth = displayMetrics.widthPixels;
        customAdapterRecyclerView = new CustomAdapterRecyclerView(
                getActivity(), savedNavigationResults, originText, destText, navController, dpWidth);
        resultRecyclerView.setAdapter(customAdapterRecyclerView);

        listOfBusStopsObject = ((MainActivity) getActivity()).getListOfAllStops();
        listOfBusStopsString = ((MainActivity) getActivity()).getFirstPassStopsList();
//        textViewIntermediate = view.findViewById(R.id.textView6);
        SetAutoFillAdapter();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        menu.clear();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Directions");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar_menu, menu);
    }

    Button goButtonForNav;
    NavController navController;


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(DirectionsFragment.this);

        goButtonForNav = view.findViewById(R.id.button_go);
        goButtonForNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageSearchInfo(view);
            }
        });

        destInputEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getKeyCode() == KeyEvent.ACTION_DOWN))) {
                    PackageSearchInfo(view);
                }
                return false;
            }
        });

    }

    List<String> listOfNames;

    private void SetAutoFillAdapter() {
        String loadLocationsForSearch = loadJSONFromAsset("points.json");
        listOfNames = JsonPath.read(loadLocationsForSearch, "$.nodes[*].name");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, listOfNames);
        Log.e("arrayadapter is", arrayAdapter + "");
        destInputEditor.setAdapter(arrayAdapter);
        originInputEditor.setAdapter(arrayAdapter);
    }

    List<StopList> listOfAllStops;
    StopList listOfStops;
    List<String> listOfNamesReload;
    List<String> listOfIdsReload;
    List<Double> listOfLat;
    List<Double> listOfLong;

    private void GetBusStopsListOnline() {
        String url = "https://nnextbus.nus.edu.sg/BusStops";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                listOfBusStopsString = response;
                ((MainActivity) getActivity()).setFirstPassStopsList(listOfBusStopsString);
                listOfAllStops = new ArrayList<>();
                listOfNamesReload = JsonPath.read(response, "$.BusStopsResult.busstops[*].caption");
                listOfIdsReload = JsonPath.read(response, "$.BusStopsResult.busstops[*].name");
                listOfLong = JsonPath.read(response, "$.BusStopsResult.busstops[*].longitude");
                listOfLat = JsonPath.read(response, "$.BusStopsResult.busstops[*].latitude");
                for (int i = 0; i < listOfNamesReload.size(); i++) {
                    listOfStops = new StopList();
                    listOfStops.setStopName(listOfNamesReload.get(i));
                    listOfStops.setStopId(listOfIdsReload.get(i));
                    listOfStops.setStopLongitude(listOfLong.get(i));
                    listOfStops.setStopLatitude(listOfLat.get(i));
                    listOfAllStops.add(listOfStops);
                }
                ((MainActivity) getActivity()).setListOfAllStops(listOfAllStops);
                SetAutoFillAdapter();
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
            RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
            requestQueue.add(stringRequest);
        }
    }

    NavigationSearchInfo navigationSearchInfo;

    String originId;
    String destId;

    ProgressBar waitingForDirectionsResultProgressBar;

    String originText;
    String destText;


    private void PackageSearchInfo(View view) {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            destInputEditor.setEnabled(false);
            originInputEditor.setEnabled(false);
        } catch (NullPointerException e) {
            //do nothing, just move on
        }

        originText = originInputEditor.getText().toString();
        destText = destInputEditor.getText().toString();

//        Log.e("originid is", originId);
//        Log.e("destid is", destId);

        if (originText.trim().equals(destText.trim())) {
            Snackbar snackbar = Snackbar.make(view,
                    "Please enter different locations for your start point and destination.",
                    Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.nav_view);
            snackbar.show();
            destInputEditor.setEnabled(true);
            originInputEditor.setEnabled(true);
        } else if (originText.trim().length() > 0 && destText.trim().length() > 0) {

            goButtonForNav.setClickable(false);
            resultRecyclerView.setVisibility(View.INVISIBLE);
            numberOfCompletes = 0;

            navigationSearchInfo = new NavigationSearchInfo();

            navigationSearchInfo.setOrigin(originText);
            navigationSearchInfo.setDest(destText);

            CheckBox sheltered = (CheckBox) view.findViewById(R.id.checkbox_sheltered);
            CheckBox accessible = (CheckBox) view.findViewById(R.id.checkbox_accessible);
            CheckBox walkOnly = (CheckBox) view.findViewById(R.id.checkbox_walkOnly);

            navigationSearchInfo.setSheltered(sheltered.isChecked());
            navigationSearchInfo.setBarrierFree(accessible.isChecked());
            navigationSearchInfo.setWalkOnly(walkOnly.isChecked());

            ((MainActivity) getActivity()).setNavigationSearchInfo(navigationSearchInfo);

            waitingForDirectionsResultProgressBar = view.findViewById(R.id.waiting_for_directions_result_progressBar);
            waitingForDirectionsResultProgressBar.setVisibility(View.VISIBLE);

            NavigationGraph navGraph = new NavigationGraph();
            navGraph.CreateNavGraph(navigationSearchInfo, getContext());
            navGraph.startNavProcess(navigationSearchInfo, getActivity(), getContext(), new NavigationGraph.NavigationResultsFullyComplete() {
                @Override
                public void onNavResultsComplete(List<NavigationResults> navigationResults, int returnCode) {
                    if (navigationResults != null && navigationResults.size() > 0) {
                        savedNavigationResults = navigationResults;
                        customAdapterRecyclerView = new CustomAdapterRecyclerView(
                                getActivity(), savedNavigationResults, originText, destText, navController, dpWidth);
                        resultRecyclerView.setAdapter(customAdapterRecyclerView);
                        goButtonForNav.setClickable(true);
                        waitingForDirectionsResultProgressBar.setVisibility(View.GONE);
                        resultRecyclerView.setVisibility(View.VISIBLE);

                    } else {
                        if (returnCode == 1) {
                            Snackbar snackbar = Snackbar.make(view,
                                    "Check that your origin and destination are correctly selected." +
                                            " Click Help for more info.",
                                    Snackbar.LENGTH_LONG);
                            snackbar.setAnchorView(R.id.nav_view);
                            snackbar.show();
                        } else if (returnCode == 2) {
                            Snackbar snackbar = Snackbar.make(view,
                                    "We couldn't find any directions for your search criteria." +
                                            " Please adjust your criteria and try again.",
                                    Snackbar.LENGTH_LONG);
                            snackbar.setAnchorView(R.id.nav_view);
                            snackbar.show();
                        }
//                        resultRecyclerView.setVisibility(View.GONE);
                        goButtonForNav.setClickable(true);
                        waitingForDirectionsResultProgressBar.setVisibility(View.GONE);
                    }
                    destInputEditor.setEnabled(true);
                    originInputEditor.setEnabled(true);
                }
            });

        } else {
            Snackbar snackbar = Snackbar.make(view,
                    R.string.directions_input_error,
                    Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.nav_view);
            snackbar.show();
            destInputEditor.setEnabled(true);
            originInputEditor.setEnabled(true);
        }
    }

    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;

    private void GetServicesAtStopDetailsOnline(String stopToSearch, final VolleyCallBack callBack) {
        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + stopToSearch;

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
                if (stopToSearch.equals(originId)) {
                    servicesAtOrigin = servicesAtStop;
                } else if (stopToSearch.equals(destId)) {
                    servicesAtDest = servicesAtStop;
                }
                callBack.onSuccess();

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

    }

    List<String> servicesAtOrigin;
    List<String> servicesAtDest;
    List<String> sharedCommonServices;



    List<ServiceRoute> listOfStopsOnSelectedServiceRoute;
    List<ServiceRoute> listOfStopsFromOriginToDest;
    ServiceRoute stopAlongServiceRoute;
    List<String> stopNames;
    List<String> stopIds;
    List<Double> stopLats;
    List<Double> stopLongs;

    private void GetServiceRoute(String service, final VolleyCallBack callBack) {
        String url = "https://nnextbus.nus.edu.sg/PickupPoint?route_code=" + service;

        Log.e("service is", service);

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                listOfStopsFromOriginToDest = new ArrayList<>();
                stopNames = JsonPath.read(response, "$.PickupPointResult.pickuppoint[*].pickupname");
                stopIds = JsonPath.read(response, "$.PickupPointResult.pickuppoint[*].busstopcode");
                stopLongs = JsonPath.read(response, "$.PickupPointResult.pickuppoint[*].lng");
                stopLats = JsonPath.read(response, "$.PickupPointResult.pickuppoint[*].lat");
                Log.e("starting index", stopIds.indexOf(originId) + "");
                Log.e("ending index", stopIds.indexOf(destId) + "");
                for (int i = stopIds.indexOf(originId); i < stopIds.indexOf(destId); i++) {
                    stopAlongServiceRoute = new ServiceRoute();
                    stopAlongServiceRoute.setStopCaption(stopNames.get(i));
                    stopAlongServiceRoute.setStopId(stopIds.get(i));
                    stopAlongServiceRoute.setStopLat(stopLats.get(i));
                    stopAlongServiceRoute.setStopLong(stopLongs.get(i));
                    listOfStopsFromOriginToDest.add(stopAlongServiceRoute);
                }
                if (listOfStopsFromOriginToDest.size() == 0) {
                    Log.e("no route found", "unfortunate");
                } else {
//                    textViewIntermediate.setText(service + " " + listOfStopsFromOriginToDest.size());
                }


//                servicesAllInfoAtStop = new ArrayList<>();
//                Log.e("GetStopInfo response is", response);
//                servicesAtStop = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].name");
//                serviceFirstArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime");
//                serviceSecondArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime");
//                firstArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime_veh_plate");
//                secondArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime_veh_plate");
//                Log.e("servicesAtStop is: ", servicesAtStop.get(0));
//                for (int i = 0; i < servicesAtStop.size(); i++) {
//                    serviceInfoAtStop = new ServiceInStopDetails();
//                    serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
//                    serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
//                    Log.e("first arrival is: ", "" + serviceFirstArrival.get(i));
//                    serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
//                    serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
//                    serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
//                    servicesAllInfoAtStop.add(serviceInfoAtStop);
                callBack.onSuccess();

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
    }

    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    int numberOfCompletes = 0;

    public interface VolleyCallBack {
        void onSuccess();
    }

//    private void StartNavigation() {
//
//        sharedCommonServices =  new ArrayList<>();
//
//        for (int i = 0; i < navigationSearchInfo.getOriginServiceDetails().size(); i++) {
//                String serviceBeingCompared = navigationSearchInfo.getOriginServiceDetails().get(i).getServiceNum();
//                Log.e("serviceBeingCompared i is", serviceBeingCompared);
//            for (int j = 0; j < navigationSearchInfo.getDestServiceDetails().size(); j++) {
//                Log.e("serviceBeingCompared j inner is", navigationSearchInfo.getDestServiceDetails().get(j).getServiceNum());
//                if (serviceBeingCompared.equals(navigationSearchInfo.getDestServiceDetails().get(j).getServiceNum())) {
//                    sharedCommonServices.add(serviceBeingCompared);
//                }
//            }
//        }
//
//        GetServiceRoute("C", new VolleyCallBack() {
//            @Override
//            public void onSuccess() {
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        waitingForDirectionsResultProgressBar.setVisibility(View.GONE);
//                    }
//                }, 300);
//            }
//        });
//
//
//    }


}