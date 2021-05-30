package com.example.myapptest.ui.stops_services;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopsServicesFragment extends Fragment {

    // EXPANDABLE LIST DECLARATIONS
    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;

    private String auth = "Basic TlVTbmV4dGJ1czoxM2RMP3pZLDNmZVdSXiJU";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_stops_services, container, false);

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview_nus_stops);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);
        getListOfGroupStops();

        return view;


    }

    List<StopDetails> listStopsInstance = null;


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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



    }

//    List<String> captions;
//    List<String> internalName;
    List<StopList> listOfAllStops;
    StopList listOfStops;
    List<String> listOfNames;
    List<String> listOfIds;

    private void getListOfGroupStops() {

//        jsonRaw = StopsServicesFragmentArgs.fromBundle(getArguments()).getPassStopsListSecond();
//
//        captions = JsonPath.read(jsonRaw, "$.BusStopsResult.busstops[*].caption");
//        internalName = JsonPath.read(jsonRaw, "$.BusStopsResult.busstops[*].name");
//        initListData();

        String url = "https://nnextbus.nus.edu.sg/BusStops";

        StringRequest stringRequest = new StringRequest (Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("GetBusStopsList response is", response);
                listOfAllStops = new ArrayList<>();
                listOfNames = JsonPath.read(response, "$.BusStopsResult.busstops[*].caption");
                listOfIds = JsonPath.read(response, "$.BusStopsResult.busstops[*].name");
                for (int i = 0; i < listOfNames.size(); i++) {
                    listOfStops = new StopList();
                    listOfStops.setStopName(listOfNames.get(i));
                    listOfStops.setStopId(listOfIds.get(i));
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
                params.put("Authorization", auth);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
        requestQueue.add(stringRequest);

    }

    ServiceInStopDetails list;
    List<ServiceInStopDetails> listList;

    private void initListData() {

//        Log.e("initListData function entered ", "yes");
//        Log.e("preWrapperStops is:", "" + captions);
//        Log.e("sizeof stopCaptions is: ", "" + captions.size());

        for (i = 0; i < listOfAllStops.size(); i++) {
//            Log.e("stop caption:", captions.get(i));
//            Log.e("i is: ", "" + i);
            listGroup.add(listOfAllStops.get(i));
            adapter.notifyDataSetChanged();
        }
    }

    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;

    public void getListOfChildServices(int groupPosition, boolean isOnClick, final VolleyCallBack callback) {


        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + listOfAllStops.get(groupPosition).getStopId();
        String auth = "Basic TlVTbmV4dGJ1czoxM2RMP3pZLDNmZVdSXiJU";

        StringRequest stopStringRequest = new StringRequest (Request.Method.GET, url, new Response.Listener<String>() {

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
                params.put("Authorization", auth);
                return params;
            }
        };

        RequestQueue stopRequestQueue = Volley.newRequestQueue(this.getContext());
        stopRequestQueue.add(stopStringRequest);

//        Log.e("list is: ", list.toString());
//        return list;
    }



    // Dropdown list
    int i = 0;
    int j = 0;

    List<String> finalStringList;



    public interface VolleyCallBack {
        void onSuccess();
    }




//        List<String> list;


//        for (int i = 0; i < listsize; i++) {
//
//        }

//        List<String> list[0] = new ArrayList<>();
//        array = getResources().getStringArray(R.array.CentralLibrary);
//        for(String item : array){
//            list1.add(item);
//        }

//        List<String> list1 = new ArrayList<>();
//        array = getResources().getStringArray(R.array.EA);
//        for(String item : array){
//            list2.add(item);
//        }

//        List<String> list3 = new ArrayList<>();
//        array = getResources().getStringArray(R.array.InformationTechnology);
//        for(String item : array){
//            list3.add(item);
//        }

//        List<String> list4 = new ArrayList<>();
//        array = getResources().getStringArray(R.array.KentVale);
//        for(String item : array){
//            list4.add(item);
//        }

//        List<String> list5 = new ArrayList<>();
//        array = getResources().getStringArray(R.array.Museum);
//        for(String item : array){
//            list5.add(item);
//        }


//        Log.d("listGroup 0", "" + listGroup.get(0));
//        listItem.put(listGroup.get(0), list1);
//        listItem.put(listGroup.get(1), list2);
//        listItem.put(listGroup.get(2), list3);
//        listItem.put(listGroup.get(3), list4);
//        listItem.put(listGroup.get(4), list5);
}

//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_stops_services, container, false);
//    }

//    private StopsServicesViewModel stopsServicesViewModel;
//    private FragmentStopsServicesBinding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        stopsServicesViewModel =
//                new ViewModelProvider(this).get(StopsServicesViewModel.class);
//
//        binding = FragmentStopsServicesBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textStopsServices;
//        stopsServicesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//
//        return root;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }


