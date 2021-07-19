package com.doublefree.navigateus.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doublefree.navigateus.MainActivity;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.doublefree.navigateus.data.busrouteinformation.BusLocationInfo;
import com.doublefree.navigateus.data.busrouteinformation.BusOperatingHours;
import com.doublefree.navigateus.data.busrouteinformation.ServiceInfo;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NextbusAPIs {

    private static final String mainUrl = "https://nnextbus.nus.edu.sg/";

    /**
     * Calls list of all bus stops in NUS, and packages the JSON response string into a {@link List<StopList>}.
     *
     * @param activity
     * @param context
     * @param callback
     */
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

                try {

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

                } catch (PathNotFoundException e) {
                    callback.onFailureAllStops();
                }
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

    /**
     * Calls arrival data for a single stop and packages the JSON response string into a {@link List<ServiceInStopDetails>}.
     *
     * @param activity
     * @param context
     * @param stopId
     * @param groupPosition
     * @param isOnClick
     * @param callback
     */
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
                List<String> stopIds;
                
                final String path = "$.ShuttleServiceResult.shuttles[*].";

                try {

                    servicesAllInfoAtStop = new ArrayList<>();
                    servicesAtStop = JsonPath.read(response, path + "name");
                    serviceFirstArrival = JsonPath.read(response, path + "arrivalTime");
                    serviceSecondArrival = JsonPath.read(response, path + "nextArrivalTime");
                    firstArrivalLive = JsonPath.read(response, path + "arrivalTime_veh_plate");
                    secondArrivalLive = JsonPath.read(response, path + "nextArrivalTime_veh_plate");
                    stopIds = JsonPath.read(response, path + "busstopcode");
                    for (int i = 0; i < servicesAtStop.size(); i++) {
                        if (!stopIds.get(i).contains("-E") || stopIds.get(i).contains("-E-S")) {
                            serviceInfoAtStop = new ServiceInStopDetails();
                            serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
                            serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
                            serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
                            serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
                            serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
                            servicesAllInfoAtStop.add(serviceInfoAtStop);
                        }

                    }

                    callback.onSuccessSingleStop(servicesAllInfoAtStop);

                } catch (PathNotFoundException e) {
                    callback.onFailureSingleStop();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureSingleStop();
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

    /**
     * Calls list of all services and a short description line for each service and packages the JSON response string
     * into a {@link List<ServiceInfo>}.
     *
     * @param activity
     * @param context
     * @param callback
     */
    public static void callListOfServices(Activity activity, Context context, final VolleyCallBackServiceList callback) {

        String url = mainUrl + "ServiceDescription";

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                List<String> serviceNums, serviceDescs;
                ServiceInfo singleServiceInfo;
                List<ServiceInfo> allServicesInfo = new ArrayList<>();

                try {

                    serviceNums = JsonPath.read(response, "$.ServiceDescriptionResult.ServiceDescription[*].Route");
                    serviceDescs = JsonPath.read(response, "$.ServiceDescriptionResult.ServiceDescription[*].RouteDescription");
                    for (int i = 0; i < serviceNums.size(); i++) {
                        Log.e("serviceNums", serviceNums.get(i));
                        singleServiceInfo = new ServiceInfo();
                        singleServiceInfo.setServiceNum(serviceNums.get(i));
                        singleServiceInfo.setServiceDesc(serviceDescs.get(i));
                        allServicesInfo.add(singleServiceInfo);
                    }
                    callback.onSuccessServiceList(allServicesInfo);

                } catch (PathNotFoundException e) {
                    callback.onFailureServiceList();
                }

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

    /**
     * Calls list of all stops along a service's route and packages the JSON response string into a {@link List<StopList>}.
     *
     * @param returnAsString
     * @param serviceNum
     * @param activity
     * @param context
     * @param callback
     */
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

                    try {

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

                    } catch (PathNotFoundException e) {
                        callback.onFailurePickupPoint();
                    }
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

    /**
     * Calls list of TickerTapes (urgent announcements in NUS speak (why??)) and packages it into a {@link List< NetworkTickerTapesAnnouncements >}.
     *
     * @param activity
     * @param context
     * @param callback
     */
    public static void callListOfTickerTapes(Activity activity, Context context, final VolleyCallBackTickerTapesAnnouncementsList callback) {

        String url = mainUrl + "TickerTapes";

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

                    List<String> messages = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Message");
                    List<String> servicesAffected = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Affected_Service_Ids");
                    List<String> displayFrom = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Display_From");
                    List<String> displayTo = JsonPath.read(response, "$.TickerTapesResult.TickerTape[*].Display_To");

                    List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList = new ArrayList<>();

                    for (int i = 0; i < messages.size(); i++) {
                        NetworkTickerTapesAnnouncements networkTickerTapesAnnouncements = new NetworkTickerTapesAnnouncements();
                        networkTickerTapesAnnouncements.mainSetterNetworkTickerTapes(
                                messages.get(i), servicesAffected.get(i), displayFrom.get(i), displayTo.get(i));
                        if (networkTickerTapesAnnouncements.checkIfValid()) {
                            networkTickerTapesAnnouncementsList.add(networkTickerTapesAnnouncements);
                        }
                    }

                    callback.onSuccessTickerTapesAnnouncements(networkTickerTapesAnnouncementsList);

                } catch (PathNotFoundException e) {
                    callback.onFailureTickerTapesAnnouncements();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureTickerTapesAnnouncements();
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

    /**
     * Calls list of Announcements (non-urgent) and packages them into a {@link List<NetworkTickerTapesAnnouncements>}
     *
     * @param activity
     * @param context
     * @param callback
     */
    public static void callListOfAnnouncements(Activity activity, Context context, final VolleyCallBackTickerTapesAnnouncementsList callback) {

        String url = mainUrl + "Announcements";

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

                    List<String> messages = JsonPath.read(response, "$.AnnouncementsResult.Announcement[*].Text");
                    List<String> displayFrom = JsonPath.read(response, "$.AnnouncementsResult.Announcement[*].Created_On");
                    List<String> displayBool = JsonPath.read(response, "$.AnnouncementsResult.Announcement[*].Status");

                    List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList = new ArrayList<>();

                    for (int i = 0; i < messages.size(); i++) {
                        if (displayBool.get(i).equals("Enabled")) {
                            NetworkTickerTapesAnnouncements networkTickerTapesAnnouncements = new NetworkTickerTapesAnnouncements();
                            networkTickerTapesAnnouncements.mainSetterNetworkTickerTapes(messages.get(i), null, displayFrom.get(i), null);
                            networkTickerTapesAnnouncementsList.add(networkTickerTapesAnnouncements);
                        }
                    }

                    callback.onSuccessTickerTapesAnnouncements(networkTickerTapesAnnouncementsList);

                } catch (PathNotFoundException e) {
                    callback.onFailureTickerTapesAnnouncements();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureTickerTapesAnnouncements();
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

    /**
     * Calls buses currently tagged to the specified service and returns a {@link List<BusLocationInfo>}
     *
     * @param serviceNum
     * @param activity
     * @param context
     * @param callback
     */
    public static void callActiveBuses(String serviceNum, Activity activity, Context context, final VolleyCallBackActiveBusList callback) {

        String url = mainUrl + "ActiveBus?route_code=" + serviceNum;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

                    List<String> plateNum = JsonPath.read(response, "$.ActiveBusResult.activebus[*].vehplate");
                    List<Double> lat = JsonPath.read(response, "$.ActiveBusResult.activebus[*].lat");
                    List<Double> lng = JsonPath.read(response, "$.ActiveBusResult.activebus[*].lng");
                    ;

                    List<BusLocationInfo> busLocationInfoList = new ArrayList<>();

                    for (int i = 0; i < plateNum.size(); i++) {
                        BusLocationInfo busLocationInfo = new BusLocationInfo();
                        busLocationInfo.setServicePlate(plateNum.get(i));
                        busLocationInfo.setBusLocation(lat.get(i), lng.get(i));
                        busLocationInfoList.add(busLocationInfo);
                    }

                    callback.onSuccessActiveBus(busLocationInfoList);

                } catch (PathNotFoundException e) {
                    callback.onFailureActiveBus();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
                callback.onFailureActiveBus();
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

    /**
     * Calls the operating hours of the specified service, and returns a {@link List<BusOperatingHours>}
     *
     * @param activity
     * @param context
     * @param serviceNum
     * @param callback
     */
    public static void callBusOperatingHours(Activity activity, Context context, String serviceNum, final VolleyCallBackOperatingHours callback) {

        String url = mainUrl + "RouteMinMaxTime?route_code=" + serviceNum;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                String path = "$.RouteMinMaxTimeResult.RouteMinMaxTime[*].";

                try {

                    List<String> scheduleType = JsonPath.read(response, path + "ScheduleType");
                    List<String> dayType = JsonPath.read(response, path + "DayType");
                    List<String> firstTime = JsonPath.read(response, path + "FirstTime");
                    List<String> lastTime = JsonPath.read(response, path + "LastTime");

                    List<BusOperatingHours> busOperatingHoursList = new ArrayList<>();

                    for (int i = 0; i < scheduleType.size(); i++) {
                        BusOperatingHours temp = new BusOperatingHours(scheduleType.get(i), dayType.get(i), firstTime.get(i), lastTime.get(i));
                        busOperatingHoursList.add(temp);
                    }

                    callback.onSuccessOperatingHours(busOperatingHoursList);

                } catch (PathNotFoundException e) {
                    callback.onFailureOperatingHours();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
//                callback.onFailureActiveBus();
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
        void onFailureSingleStop();
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

    public interface VolleyCallBackTickerTapesAnnouncementsList {
        void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList);
        void onFailureTickerTapesAnnouncements();
    }

    public interface VolleyCallBackActiveBusList {
        void onSuccessActiveBus(List<BusLocationInfo> busLocationInfoList);
        void onFailureActiveBus();
    }

    public interface VolleyCallBackOperatingHours {
        void onSuccessOperatingHours(List<BusOperatingHours> list);
        void onFailureOperatingHours();
    }

}
