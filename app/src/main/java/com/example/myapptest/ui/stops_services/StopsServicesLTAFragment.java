package com.example.myapptest.ui.stops_services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

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
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.ui.StopsMainAdapter;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopsServicesLTAFragment extends Fragment {

    // EXPANDABLE LIST DECLARATIONS
    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;

    private String auth = "Basic b18PsNPdTIioqhY5yggKsA==";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_stops_services_lta, container, false);

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview_lta_stops);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);

        return view;


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListOfGroupStops();

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    getListOfChildServices(groupPosition, true, new StopsServicesLTAFragment.VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            expandableListView.expandGroup(groupPosition);
                        }
                    });
                }
                return true;
            }
        });

    }

    String jsonRaw;
    List<String> list;
//    List<String> servicesAtStop;

    List<StopList> listOfAllStops;
    StopList listOfStops;
    List<String> listOfNames;
    List<String> listOfIds;
    List<String> listOfDescriptions;

    private void getListOfGroupStops() {

        listOfAllStops = new ArrayList<>();
        listOfIds = new ArrayList<>();
        listOfIds.add("16159");
        listOfIds.add("18329");
        listOfNames = new ArrayList<>();
        listOfNames.add("NUS Fac of Engrg");
        listOfNames.add("University Health Ctr");
        listOfDescriptions = new ArrayList<>();
        listOfDescriptions.add("Clementi Rd");
        listOfDescriptions.add("Lower Kent Ridge Rd");
        for (int i = 0; i < listOfIds.size(); i++) {
            listOfStops = new StopList();
            listOfStops.setStopId(listOfIds.get(i));
            listOfStops.setStopName(listOfNames.get(i));
            listOfStops.setStopDescription(listOfDescriptions.get(i));
            listOfAllStops.add(listOfStops);
        }


//        jsonRaw = StopsServicesFragmentArgs.fromBundle(getArguments()).getPassStopsListSecond();
//
//        captions = JsonPath.read(jsonRaw, "$.BusStopsResult.busstops[*].caption");
//        internalName = JsonPath.read(jsonRaw, "$.BusStopsResult.busstops[*].name");
//        initListData();

        initListData();

    }

    ServiceInStopDetails serviceInfoAtStop;
    List<ServiceInStopDetails> servicesAllInfoAtStop;
    List<String> servicesAtStop;
    List<String> serviceFirstArrival;
    List<String> serviceSecondArrival;
    List<String> firstArrivalLive;
    List<String> secondArrivalLive;

    private void getListOfChildServices(int groupPosition, boolean isOnClick, final VolleyCallBack callback) {


        String url = "http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=" + listOfAllStops.get(groupPosition).getStopId();

        StringRequest stopStringRequest = new StringRequest (Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                servicesAllInfoAtStop = new ArrayList<>();
                Log.e("GetStopInfo response is", response);
                servicesAtStop = JsonPath.read(response, "$.Services[*].ServiceNo");
                serviceFirstArrival = JsonPath.read(response, "$.Services[*].NextBus.EstimatedArrival");
                serviceSecondArrival = JsonPath.read(response, "$.Services[*].NextBus2.EstimatedArrival");
//                firstArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime_veh_plate");
//                secondArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime_veh_plate");
                Log.e("servicesAtStop is: ", servicesAtStop.get(0));
                for (int i = 0; i < servicesAtStop.size(); i++) {
                    serviceInfoAtStop = new ServiceInStopDetails();
                    serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
                    //TODO: manipulate LTA's return string into int to find difference
                    serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
//                    Log.e("first arrival is: ", "" + serviceFirstArrival.get(i));
                    serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
//                    serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
//                    serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
                    servicesAllInfoAtStop.add(serviceInfoAtStop);
                }
                Log.e("servicesAllInfoAtStop is: ", "" + servicesAllInfoAtStop);
                Log.e("value of j is: ", "" + groupPosition);
                listItem.put(listGroup.get(groupPosition), servicesAllInfoAtStop);

                adapter.notifyDataSetChanged();
                Log.e("listItem is: ", "" + listItem);

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
                params.put("AccountKey", auth);
                params.put("accept", "application/json");
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

    private void initListData() {

        for (i = 0; i < listOfAllStops.size(); i++) {
//            Log.e("stop caption:", captions.get(i));
//            Log.e("i is: ", "" + i);
            listGroup.add(listOfAllStops.get(i));
            adapter.notifyDataSetChanged();
        }
    }

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


