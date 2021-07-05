package com.example.myapptest.data;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.busnetworkinformation.NetworkTickerTapes;
import com.example.myapptest.data.busrouteinformation.ServiceInfo;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.ui.stops_services.StopsServicesFragment;
import com.google.android.material.chip.ChipGroup;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NextbusAPIs {

    private static final String mainUrl = "https://nnextbus.nus.edu.sg/";

    public static void callStopsList(Activity activity, Context context, final VolleyCallBackAllStops callback) {
        String url = mainUrl + "BusStops";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //for getting full list of stops (parent only)
                List<StopList> listOfAllStops = new ArrayList<>();
                StopList listOfStops;
                List<String> listOfNames;
                List<String> listOfIds;
                List<Double> listOfLat;
                List<Double> listOfLong;

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
                ((MainActivity) activity).setListOfAllStops(listOfAllStops);

                callback.onSuccessAllStops(listOfAllStops);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureAllStops();
            }


        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
        }
    }

    public static void callSingleStopInfo(Activity activity, Context context, String stopId, int groupPosition, boolean isOnClick, final VolleyCallBackSingleStop callback) {

        String url = mainUrl + "ShuttleService?busstopname=" + stopId;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                ServiceInStopDetails serviceInfoAtStop;
                List<ServiceInStopDetails> servicesAllInfoAtStop;
                List<String> servicesAtStop;
                List<String> serviceFirstArrival;
                List<String> serviceSecondArrival;
                List<String> firstArrivalLive;
                List<String> secondArrivalLive;

                servicesAllInfoAtStop = new ArrayList<>();
                servicesAtStop = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].name");
                serviceFirstArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime");
                serviceSecondArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime");
                firstArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime_veh_plate");
                secondArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime_veh_plate");
                for (int i = 0; i < servicesAtStop.size(); i++) {
                    serviceInfoAtStop = new ServiceInStopDetails();
                    serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
                    serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
                    serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
                    serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
                    serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
                    servicesAllInfoAtStop.add(serviceInfoAtStop);
                }

                callback.onSuccessSingleStop(servicesAllInfoAtStop);

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureAllStops();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(context);
            stopRequestQueue.add(stopStringRequest);
        }

    }

    public static void callListOfServices(Activity activity, Context context, final VolleyCallBackServiceList callback) {

        String url = mainUrl + "ServiceDescription";

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                List<String> serviceNums, serviceDescs;
                ServiceInfo singleServiceInfo;
                List<ServiceInfo> allServicesInfo = new ArrayList<>();

                serviceNums = JsonPath.read(response, "$.ServiceDescriptionResult.ServiceDescription[*].Route");
                serviceDescs = JsonPath.read(response, "$.ServiceDescriptionResult.ServiceDescription[*].RouteDescription");
                for (int i = 0; i < serviceNums.size(); i++) {
                    singleServiceInfo = new ServiceInfo();
                    singleServiceInfo.setServiceNum(serviceNums.get(i));
                    singleServiceInfo.setServiceDesc(serviceDescs.get(i));
                    allServicesInfo.add(singleServiceInfo);
                }
                callback.onSuccessServiceList(allServicesInfo);

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureServiceList();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(context);
            stopRequestQueue.add(stopStringRequest);
        }
    }

    public static void callPickupPoint(boolean returnAsString, String serviceNum, Activity activity, Context context, final VolleyCallBackPickupPoint callback) {

        String url = mainUrl + "PickupPoint?route_code=" + serviceNum;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                if (returnAsString) {
                    callback.OnSuccessPickupPointString(response);
                } else {
                    List<StopList> listOfAllStops = new ArrayList<>();
                    StopList listOfStops;
                    List<String> listOfNames;
                    List<String> listOfIds;
                    List<Double> listOfLat;
                    List<Double> listOfLong;

                    listOfNames = JsonPath.read(response, "$.PickupPointResult.pickuppoint[*].pickupname");
                    listOfIds = JsonPath.read(response, "$.BusStopsResult.busstops[*].busstopcode");
                    listOfLong = JsonPath.read(response, "$.BusStopsResult.busstops[*].lng");
                    listOfLat = JsonPath.read(response, "$.BusStopsResult.busstops[*].lat");
                    for (int i = 0; i < listOfNames.size(); i++) {
                        listOfStops = new StopList();
                        listOfStops.setStopName(listOfNames.get(i));
                        listOfStops.setStopId(listOfIds.get(i));
                        listOfStops.setStopLongitude(listOfLong.get(i));
                        listOfStops.setStopLatitude(listOfLat.get(i));
                        listOfAllStops.add(listOfStops);
                    }
                    callback.onSuccessPickupPoint(listOfAllStops);
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailurePickupPoint();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(context);
            stopRequestQueue.add(stopStringRequest);
        }
    }


    public static void callListOfTickerTapes(Activity activity, Context context, final VolleyCallBackTickerTapesList callback) {

        String url = "https://nnextbus.nus.edu.sg/TickerTapes";

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                List<String> messages = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Message");
                List<String> servicesAffected = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Affected_Service_Ids");
                List<String> displayFrom = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Display_From");
                List<String> displayTo = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Display_To");

                List<NetworkTickerTapes> networkTickerTapesList = new ArrayList<>();

                for (int i = 0; i < messages.size(); i++) {
                    NetworkTickerTapes networkTickerTapes = new NetworkTickerTapes();
                    networkTickerTapes.mainSetterNetworkTickerTapes(
                            messages.get(i), servicesAffected.get(i), displayFrom.get(i), displayTo.get(i));
                    if (networkTickerTapes.checkIfValid()) {
                        networkTickerTapesList.add(networkTickerTapes);
                    }
                }

                callback.onSuccessTickerTapes(networkTickerTapesList);

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureTickerTapes();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(context);
            stopRequestQueue.add(stopStringRequest);
        }
    }

    public interface VolleyCallBackAllStops {
        void onSuccessAllStops(List<StopList> listOfAllStops);
        void onFailureAllStops();
    }

    public interface VolleyCallBackSingleStop {
        void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop);
        void onFailureAllStops();
    }

    public interface VolleyCallBackServiceList {
        void onSuccessServiceList(List<ServiceInfo> servicesInfo);
        void onFailureServiceList();
    }

    public interface VolleyCallBackPickupPoint {
        void OnSuccessPickupPointString(String response);
        void onSuccessPickupPoint(List<StopList> listOfStopsAlongRoute);
        void onFailurePickupPoint();
    }

    public interface VolleyCallBackTickerTapesList {
        void onSuccessTickerTapes(List<NetworkTickerTapes> networkTickerTapesList);
        void onFailureTickerTapes();
    }

}
