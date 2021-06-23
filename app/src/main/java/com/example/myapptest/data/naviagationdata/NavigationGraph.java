package com.example.myapptest.data.naviagationdata;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.jayway.jsonpath.JsonPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NavigationGraph extends AsyncTask<Void, Void, NavigationResults> {

    NavigationResultsFullyComplete navFinished;

    public void setNavFinished() {
        this.navFinished = navFinished;
    }

    public NavigationNodes[] navNodes = new NavigationNodes[1000];
//    public LinkedList<List<NavigationEdges>> navEdges = new LinkedList<List<NavigationEdges>>();

    List<NavigationNodes> listOfAllBusStops = new ArrayList<>();

    boolean mustBeSheltered = false;
    boolean mustBeAccessible = false;

    public void CreateNavGraph(NavigationSearchInfo navigationSearchInfo, Context context) {
        try {
            JSONObject rawNavNodes = new JSONObject(loadJSONFromAsset("points.json", context));
            JSONObject rawNavEdges = new JSONObject(loadJSONFromAsset("graphedges.json", context));

            JSONArray nodeArray = rawNavNodes.getJSONArray("nodes");
            JSONArray edgeArray = rawNavEdges.getJSONArray("edges");

            mustBeSheltered = navigationSearchInfo.isSheltered();
            mustBeAccessible = navigationSearchInfo.isBarrierFree();

            Log.e("nodearray len", nodeArray.length() + "");
            for (int i = 0; i < nodeArray.length(); i++) {
                JSONObject nodeDetail = nodeArray.getJSONObject(i);
                int arrayIndex = nodeDetail.getInt("numid");
                NavigationNodes tempNavNodes = new NavigationNodes();
                tempNavNodes.setName(nodeDetail.getString("name"));
                tempNavNodes.setAltname(nodeDetail.getString("altname"));
                tempNavNodes.setNumid(arrayIndex);
                tempNavNodes.setId(nodeDetail.getString("id"));
                tempNavNodes.setLat(nodeDetail.getDouble("lat"));
                tempNavNodes.setLon(nodeDetail.getDouble("lon"));
                navNodes[arrayIndex] = tempNavNodes;
                Log.e("waste", "my time " + i + " " + tempNavNodes.getName());
                if (arrayIndex < 50) {
                    listOfAllBusStops.add(tempNavNodes);
                    Log.e("latlon", tempNavNodes.getName() + " " + tempNavNodes.getLat() + " " + tempNavNodes.getLon());
                }
            }

            Log.e("len", edgeArray.length() + "");
            for (int j = 0; j < edgeArray.length(); j++) {
                Log.e("entered", j + "");
                JSONObject edgeDetail = edgeArray.getJSONObject(j);
                if (checkConditions(edgeDetail, mustBeSheltered, mustBeAccessible)) {
                    int arrayIndex1 = edgeDetail.getInt("fromnumid");
                    String tptType = edgeDetail.getString("by");
                    NavigationEdges tempNavEdge1 = new NavigationEdges();
                    tempNavEdge1.setFrom(edgeDetail.getString("from"));
                    tempNavEdge1.setFromnumid(edgeDetail.getInt("fromnumid"));
                    tempNavEdge1.setTo(edgeDetail.getString("to"));
                    tempNavEdge1.setTonumid(edgeDetail.getInt("tonumid"));
                    tempNavEdge1.setBy(tptType);
                    tempNavEdge1.setDuration(edgeDetail.getInt("duration"));


                    if (tptType.equals("bus")) {
    //                    JSONArray serviceArray = edgeDetail.getJSONArray("services");
    //                    List<String> serviceArrayToAdd = new ArrayList<>();
    //                    for (int k = 0; k < serviceArray.length(); k++) {
    //                        serviceArrayToAdd.add(serviceArray.getString(k));
    //                    }
    //                    tempNavEdge1.setServices(serviceArrayToAdd);
                        tempNavEdge1.setServices(null);
                        tempNavEdge1.setAccessible(true);
                        tempNavEdge1.setSheltered(true);
                    } else {
                        boolean sheltered = false;
                        boolean accessible = false;
                        if (edgeDetail.getString("sheltered").equals("y")) {
                            sheltered = true;
                        }
                        tempNavEdge1.setSheltered(sheltered);
                        if (edgeDetail.getString("accessible").equals("y")) {
                            accessible = true;
                        }
                        tempNavEdge1.setAccessible(accessible);
                        tempNavEdge1.setServices(null);
                        NavigationEdges tempNavEdge2 = new NavigationEdges();
                        int arrayIndex2 = edgeDetail.getInt("tonumid");
                        tempNavEdge2.setTo(edgeDetail.getString("from"));
                        tempNavEdge2.setTonumid(edgeDetail.getInt("fromnumid"));
                        tempNavEdge2.setFrom(edgeDetail.getString("to"));
                        tempNavEdge2.setFromnumid(edgeDetail.getInt("tonumid"));
                        tempNavEdge2.setBy(tptType);
                        tempNavEdge2.setDuration(edgeDetail.getInt("duration"));
                        tempNavEdge2.setSheltered(sheltered);
                        tempNavEdge2.setAccessible(accessible);
                        tempNavEdge2.setServices(null);
                        List<NavigationEdges> tempListNavEdge2 = navNodes[arrayIndex2].getNavEdgesFromThisNode();
                        if (tempListNavEdge2 == null) {
                            tempListNavEdge2 = new ArrayList<>();
                        }
                        Log.e("tempnavedge2", tempNavEdge2.getFrom());
                        tempListNavEdge2.add(tempNavEdge2);
                        navNodes[arrayIndex2].setNavEdgesFromThisNode(tempListNavEdge2);
                    }
                    List<NavigationEdges> tempListNavEdge1 = navNodes[arrayIndex1].getNavEdgesFromThisNode();
                    if (tempListNavEdge1 == null) {
                        tempListNavEdge1 = new ArrayList<>();
                    }
                    Log.e("tempnavedge1", tempNavEdge1.getFrom());
                    tempListNavEdge1.add(tempNavEdge1);
                    navNodes[arrayIndex1].setNavEdgesFromThisNode(tempListNavEdge1);
                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean checkConditions(JSONObject edgeDetail, boolean shelteredCondition, boolean AccessibleCondition) throws JSONException {
        if ((!mustBeSheltered && !mustBeAccessible) || edgeDetail.getString("by").equals("bus")) {
            return true;
        }
        if (mustBeAccessible && mustBeSheltered && edgeDetail.getString("sheltered").equals("y") && edgeDetail.getString("accessible").equals("y")) {
            return true;
        }
        if (mustBeSheltered && edgeDetail.getString("sheltered").equals("y")) {
            return true;
        }
        if (mustBeAccessible && edgeDetail.getString("accessible").equals("y")) {
            return true;
        }
        return false;
    }

    List<NavigationNodes> originStopDistanceList = new ArrayList<>(navNodes.length);
    List<NavigationNodes> destStopDistanceList = new ArrayList<>(navNodes.length);

    List<NavigationResults> listResults = new ArrayList<>();

    public void startNavProcess(NavigationSearchInfo navigationSearchInfo, Activity activity, Context context, final NavigationResultsFullyComplete navFinished) {

        Log.e("entered", "185");

        NavigationNodes origin = new NavigationNodes();
        NavigationNodes destination = new NavigationNodes();
        boolean originSet = false;
        boolean destSet = false;
        for (int i = 0; i < navNodes.length; i++) {
            try {
                if (navigationSearchInfo.getOrigin().equals(navNodes[i].getName())) {
                    origin = navNodes[i];
                    originSet = true;
                } else if (navigationSearchInfo.getDest().equals(navNodes[i].getName())) {
                    destination = navNodes[i];
                    destSet = true;
                }
                if (originSet && destSet && origin != null && destination != null) {
                    Log.e("entered", "findshortestpath");
                    break;
                }
                if (i == navNodes.length - 1) {
                    return;
                }
            } catch (NullPointerException e) {

            }
        }

        //TODO: fix this
        destStopDistanceList = sortBusStopDistance(destination, listOfAllBusStops);
        originStopDistanceList = sortBusStopDistance(origin, listOfAllBusStops);


        NavigationResults fullRoute = new NavigationResults();
        List<NavigationPartialResults> routeSegments = new ArrayList<>();
        NavigationPartialResults segmentResult;

        NavigationPartialResults firstTrialResult = findShortestPath(origin, destination, false, false, true);
        fullRoute.setTotalTimeTaken(firstTrialResult.getTimeForSegment());

        NavigationNodes originBusStop = new NavigationNodes();
        NavigationNodes destBusStop = new NavigationNodes();
        boolean isThereBus = false;
        boolean isThereWalking = false;
        boolean originBusStopFound = false;
        boolean destBusStopFound = false;
        for (int i = 1; i < firstTrialResult.getNodesTraversed().size(); i++) {
            Log.e("i is", i + "");
            if (firstTrialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("bus")) {
                isThereBus = true;
                if (!originBusStopFound) {
                    originBusStop = firstTrialResult.getNodesTraversed().get(i - 1);
                    originBusStopFound = true;
                }
            } else if (i > 1 && firstTrialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("walk")
                    && firstTrialResult.getNodesTraversed().get(i - 1).getEdgeSelected().getBy().equals("bus")) {
                destBusStop = firstTrialResult.getNodesTraversed().get(i - 1);
                destBusStopFound = true;
            } else if (firstTrialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("walk")) {
                isThereWalking = true;
            }
        }
        if (!originBusStopFound) {
            originBusStop = firstTrialResult.getNodesTraversed().get(0);
        }
        if (!destBusStopFound) {
            destBusStop = firstTrialResult.getNodesTraversed().get(firstTrialResult.getNodesTraversed().size() - 1);
        }


        int totalTime = 0;


//        for (int i = 0; i < originStopDistanceList.size(); i++) {
//            Log.e("origin stop is:",i + " " + originStopDistanceList.get(i).getName());
//        }
//
//        for (int i = 0; i < destStopDistanceList.size(); i++) {
//            Log.e("dest stop is:",i + " " + destStopDistanceList.get(i).getName());
//        }
//
//
        if (isThereBus && isThereWalking) {

//            List<NavigationNodes> firstList = firstTrialResult.getNodesTraversed();

//
//            for (int i = 0; i < firstList.size(); i++) {
//                if (firstList.get(i).getNumid() < 50 && !originBusStopFound) {
//                    originBusStop = firstList.get(i);
//                    originBusStopFound = true;
//                } else if (i > 1 && firstList.get(i-1).getNumid() < 50
//                        && firstList.get(i).getNumid() > 50 && !destBusStopFound) {
//                    destBusStop = firstList.get(i-1);
//                    destBusStopFound = true;
//                }
//                if (originBusStopFound && destBusStopFound) {
//                    break;
//                }
//            }

            int timeReq = 0;
//
//            for (int a = 0; a < 10; a++) {
//                for (int b = 0; b < 10; b++) {

//                    if (!(.equals(destStopDistanceList.get(b)))) {
            //walk to first boarding bus stop
            routeSegments.clear();

            boolean firstWalk = true;

            if (firstTrialResult.getNodesTraversed().get(0).getNumid() > 50) {
                Log.e("first walk", "yes");
                Log.e("origin is", origin.getName());
                segmentResult = findShortestPath(origin, originBusStop, false, true, true);
                routeSegments.add(segmentResult);
                timeReq += segmentResult.getTimeForSegment();
                firstWalk = false;

            }

            //on the bus
            Log.e("bus", "yes");
//                        Log.e("indexes are", "a: " + a + " b: " + b);
//                        Log.e("bus start busstop", originStopDistanceList.get(a).getName());
//                        Log.e("bus dest busstop", destStopDistanceList.get(b).getName());
            segmentResult = findBusRoute(originBusStop, destBusStop, timeReq, activity, context, firstWalk);
            Log.e("time is", segmentResult.getTimeForSegment() + "");
            timeReq += segmentResult.getTimeForSegment();
            routeSegments.add(segmentResult);

//                        , new BusNavComplete() {
//                            @Override
//                            public void onBusNavSuccess(NavigationPartialResults busNavResult) {
//
//
//                            }
//                        });
            if (segmentResult != null) {
//                            fullRoute.setTotalTimeTaken(fullRoute.getTotalTimeTaken() + segmentResult.getTimeForSegment());
            }
            //walk from last bus stop to final dest
            if (firstTrialResult.getNodesTraversed().get(firstTrialResult.getNodesTraversed().size() - 1).getNumid() > 50) {
                segmentResult = findShortestPath(destBusStop, destination, false, true, false);
                routeSegments.add(segmentResult);
                Log.e("check time for 2nd walk", segmentResult.getTimeForSegment() + "");
                timeReq += segmentResult.getTimeForSegment();
            }

//                        fullRoute.setTotalTimeTaken(fullRoute.getTotalTimeTaken() + segmentResult.getTimeForSegment());
            fullRoute.setResultsConcatenated(routeSegments);
            fullRoute.setTotalTimeTaken(timeReq);
            listResults.add(fullRoute);

//                    }
//                }
//            }


        } else if (isThereWalking) {

            routeSegments.clear();

            int timeReq = 0;
            routeSegments.add(firstTrialResult);
            timeReq = firstTrialResult.getTimeForSegment();

            fullRoute.setTotalTimeTaken(timeReq);
            fullRoute.setResultsConcatenated(routeSegments);
            listResults.add(fullRoute);

        }

//        routeSegments.add(firstTrialResult);
//        fullRoute.setResultsConcatenated(routeSegments);
//        listResults.add(fullRoute);
//        else if (isThereBus && !isThereWalking) {
//
//            int timeReq = 0;
//            segmentResult = findBusRoute(origin, destination, 0, activity, context);
////            , new BusNavComplete() {
////                @Override
////                public void onBusNavSuccess(NavigationPartialResults segmentResult) {
////                    routeSegments.add(segmentResult);
////                    fullRoute.setTotalTimeTaken(segmentResult.getTimeForSegment());
////                    fullRoute.setResultsConcatenated(routeSegments);
////                    routeSegments.clear();
////                    listResults.add(fullRoute);
////
////                }
////            });
//            //TODO: do stuff
//


        //TODO: sort list according to timings and characteristics

//        Log.e("check inner", listResults.get(0).getTotalTimeTaken() + "");

        navFinished.onNavResultsComplete(listResults);



    }


    private NavigationPartialResults findBusRoute(NavigationNodes originBusStop, NavigationNodes destBusStop,
                                                  int timeSoFar, Activity activity, Context context, boolean isFirstTime) {

        NavigationPartialResults routeToTake = findShortestPath(originBusStop, destBusStop, true, false, isFirstTime);
        int rawTimeTakenOnBus = routeToTake.getTimeForSegment();
        Log.e("time before arrival info", routeToTake.getTimeForSegment() + "");

        String jsonBusInfo = loadJSONFromAsset("listOfBusStopsByService_full.json", context);

        List<String> stopId = JsonPath.read(jsonBusInfo, "$.pickuppoint[*].busstopcode");
        List<String> aServiceAtStop = JsonPath.read(jsonBusInfo, "$.pickuppoint[*].routeid");

        int originIndex = -1;
        int destIndex = -1;

        //check if there exists any direct buses between origin and dest stop
        for (int i = 0; i < stopId.size(); i++) {
            if (stopId.get(i).equals(originBusStop.getId())) {
                originIndex = i;
                Log.e("origin name", stopId.get(i));
            } else if (stopId.get(i).equals(destBusStop.getId())) {
                destIndex = i;
                Log.e("dest name", stopId.get(i));
            }
            if (originIndex >= 0 && destIndex >= 0 && destIndex > originIndex
                    && aServiceAtStop.get(originIndex).equals(aServiceAtStop.get(destIndex))) {
                Log.e("viable service", aServiceAtStop.get(originIndex) + " " + stopId.get(originIndex) + " " + stopId.get(destIndex));
                routeToTake.addViableBuses1(aServiceAtStop.get(originIndex));
                originIndex = -1;
                destIndex = -1;
            }
        }

        if (routeToTake.getViableBuses1().size() == 0) {
            return null;
        }

        //no transfer required - a direct bus exists
        if (routeToTake.getViableBuses1().size() > 0) {
//            getListOfChildServices(activity, originBusStop.getId(), new VolleyCallBack() {
//                @Override
//                public void onSuccess(List<ServiceInStopDetails> servicesAllInfoAtStop) {
//                    List<Integer> serviceWaitTimes = new ArrayList<>();
//                    for (int i = 0; i < servicesAllInfoAtStop.size(); i++) {
//                        for (int j = 0; j < routeToTake.getViableBuses1().size(); j++) {
//                            if (servicesAllInfoAtStop.get(i).getServiceNum().equals(routeToTake.getViableBuses1().get(j))) {
//                                if (servicesAllInfoAtStop.get(i).getFirstArrival().equals("-")) {
//                                    serviceWaitTimes.add(-1);
//                                } else if ((servicesAllInfoAtStop.get(i).getFirstArrival().equals("Arr"))
//                                        && timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival()));
//                                } else {
//                                    serviceWaitTimes.add(-1);
//                                }
//                            }
//                        }
//                    }
//                    for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                        for (int j = i + 1; j < serviceWaitTimes.size(); j++) {
//                            if (serviceWaitTimes.get(i) == -1) {
//
//                            } else if (serviceWaitTimes.get(j) < serviceWaitTimes.get(i)) {
//                                Collections.swap(serviceWaitTimes, i, j);
//                            }
//                        }
//
//                    }
//                    boolean busSet = false;
//                    for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                        if (serviceWaitTimes.get(i) != -1) {
//                            routeToTake.setBusToWaitForIndiv(routeToTake.getViableBuses1().get(i), 0);
//                            routeToTake.setBusWaitingTime1Indiv(serviceWaitTimes.get(i), 0);
//                            routeToTake.setTimeForSegment(routeToTake.getTimeForSegment() + serviceWaitTimes.get(i));
//                            routeToTake.setTransferStop(null);
//                            busSet = true;
//                        }
//                    }
//                    if (!busSet) {
//                        routeToTake.setBusToWaitForIndiv(null, 0);
//                        routeToTake.setBusWaitingTime1Indiv(-1, 0);
//                        routeToTake.setTransferStop(null);
//                    }
//
//                    busNavComplete.onBusNavSuccess(routeToTake);
//
//
//                }
//            });
        } else {
            //check how to transfer

            List<String> servicesAtDest = new ArrayList<>();
            List<String> servicesAtOrigin = new ArrayList<>();
            for (int i = 0; i < stopId.size(); i++) {
                if (stopId.get(i).equals(originBusStop.getId())) {
                    servicesAtOrigin.add(aServiceAtStop.get(i));
                } else if (stopId.get(i).equals(destBusStop.getId())) {
                    servicesAtDest.add(aServiceAtStop.get(i));
                }
            }
            for (int i = 0; i < routeToTake.getNodesTraversed().size(); i++) {
                List<String> servicesAtIntermediateStop = new ArrayList<>();
                NavigationNodes currentStop = routeToTake.getNodesTraversed().get(i);
                routeToTake.setBeforeTransferNodesTraversedIndiv(currentStop);
                for (int j = 0; j < stopId.size(); j++) {
                    if (stopId.get(j).equals(currentStop.getId())) {
                        servicesAtIntermediateStop.add(aServiceAtStop.get(j));
                    }
                }
                for (int j = 0; j < servicesAtDest.size(); j++) {
                    for (int k = 0; k < servicesAtIntermediateStop.size(); k++) {
                        if (servicesAtDest.get(j).equals(servicesAtIntermediateStop.get(k))) {
                            routeToTake.addViableBuses2(servicesAtIntermediateStop.get(k));
                        }
                    }
                }
                if (routeToTake.getViableBuses2().size() > 0) {
                    //transfer route found
                    routeToTake.setTransferStop(currentStop);
                    for (int j = 0; j < servicesAtOrigin.size(); j++) {
                        for (int k = 0; k < servicesAtIntermediateStop.size(); k++) {
                            if (servicesAtOrigin.get(j).equals(servicesAtIntermediateStop.get(k))) {
                                routeToTake.addViableBuses1(servicesAtIntermediateStop.get(k));
                            }
                        }
                    }
                    for (int j = i + 1; j < routeToTake.getNodesTraversed().size(); i++) {
                        routeToTake.setAfterTransferNodesTraversedIndiv(routeToTake.getNodesTraversed().get(j));
                    }
                    break;
                }
            }

//            getListOfChildServices(activity, originBusStop.getId(), new VolleyCallBack() {
//                @Override
//                public void onSuccess(List<ServiceInStopDetails> servicesAllInfoAtStop) {
//                    List<Integer> serviceWaitTimes = new ArrayList<>();
//                    for (int i = 0; i < servicesAllInfoAtStop.size(); i++) {
//                        for (int j = 0; j < routeToTake.getViableBuses1().size(); j++) {
//                            if (servicesAllInfoAtStop.get(i).getServiceNum().equals(routeToTake.getViableBuses1().get(j))) {
//                                if (servicesAllInfoAtStop.get(i).getFirstArrival().equals("-")) {
//                                    serviceWaitTimes.add(-1);
//                                } else if ((servicesAllInfoAtStop.get(i).getFirstArrival().equals("Arr"))
//                                        && timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival())) {
//                                    serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival()));
//                                } else {
//                                    serviceWaitTimes.add(-1);
//                                }
//                            }
//                        }
//                    }
//                    for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                        for (int j = i + 1; j < serviceWaitTimes.size(); j++) {
//                            if (serviceWaitTimes.get(i) == -1) {
//
//                            } else if (serviceWaitTimes.get(j) < serviceWaitTimes.get(i)) {
//                                Collections.swap(serviceWaitTimes, i, j);
//                            }
//                        }
//
//                    }
//                    boolean busSet = false;
//                    for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                        if (serviceWaitTimes.get(i) != -1) {
//                            routeToTake.setBusToWaitForIndiv(routeToTake.getViableBuses1().get(i), 0);
//                            routeToTake.setBusWaitingTime1Indiv(serviceWaitTimes.get(i), 0);
//                            routeToTake.setTimeForSegment(routeToTake.getTimeForSegment() + serviceWaitTimes.get(i));
//                            busSet = true;
//                        }
//                    }
//                    if (!busSet) {
//                        routeToTake.setBusToWaitForIndiv(null, 0);
//                        routeToTake.setBusWaitingTime1Indiv(-1, 0);
//                    }
//
//                    getListOfChildServices(activity, routeToTake.getTransferStop().getId(), new VolleyCallBack() {
//                        @Override
//                        public void onSuccess(List<ServiceInStopDetails> servicesAllInfoAtStop) {
//                            List<Integer> serviceWaitTimes = new ArrayList<>();
//                            for (int i = 0; i < servicesAllInfoAtStop.size(); i++) {
//                                for (int j = 0; j < routeToTake.getViableBuses2().size(); j++) {
//                                    if (servicesAllInfoAtStop.get(i).getServiceNum().equals(routeToTake.getViableBuses2().get(j))) {
//                                        if (servicesAllInfoAtStop.get(i).getFirstArrival().equals("-")) {
//                                            serviceWaitTimes.add(-1);
//                                        } else if ((servicesAllInfoAtStop.get(i).getFirstArrival().equals("Arr"))
//                                                && timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                            serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                        } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival())) {
//                                            serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getSecondArrival()));
//                                        } else if (timeSoFar < Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival())) {
//                                            serviceWaitTimes.add(Integer.parseInt(servicesAllInfoAtStop.get(i).getFirstArrival()));
//                                        } else {
//                                            serviceWaitTimes.add(-1);
//                                        }
//                                    }
//                                }
//                            }
//                            for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                                for (int j = i + 1; j < serviceWaitTimes.size(); j++) {
//                                    if (serviceWaitTimes.get(i) == -1) {
//
//                                    } else if (serviceWaitTimes.get(j) < serviceWaitTimes.get(i)) {
//                                        Collections.swap(serviceWaitTimes, i, j);
//                                    }
//                                }
//
//                            }
//                            boolean busSet = false;
//                            for (int i = 0; i < serviceWaitTimes.size(); i++) {
//                                if (serviceWaitTimes.get(i) != -1) {
//                                    routeToTake.setBusToWaitForIndiv(routeToTake.getViableBuses2().get(i), 1);
//                                    routeToTake.setBusWaitingTime1Indiv(serviceWaitTimes.get(i), 1);
//                                    int[] temp = routeToTake.getBusWaitingTime1();
//                                    routeToTake.setTimeForSegment(rawTimeTakenOnBus + serviceWaitTimes.get(i) - temp[0]);
//                                    busSet = true;
//                                }
//                            }
//                            if (!busSet) {
//                                routeToTake.setBusToWaitForIndiv(null, 0);
//                                routeToTake.setBusWaitingTime1Indiv(-1, 0);
//                            }
//
//                            busNavComplete.onBusNavSuccess(routeToTake);
//
//                        }
//                    });
//
//                }
//            });



        }

        return routeToTake;

    }

    private void checkIfTransferIsRequired(List<ServiceInStopDetails> originStopInfo, List<ServiceInStopDetails> destStopInfo) {

    }

    private List<NavigationNodes> sortBusStopDistance(NavigationNodes nodeToSort, List<NavigationNodes> listOfStopsToSort) {

        Location nodeToSortLocation = new Location("");
        nodeToSortLocation.setLatitude(nodeToSort.getLat());
        nodeToSortLocation.setLongitude(nodeToSort.getLon());
        for (int i = 0; i < listOfStopsToSort.size(); i++) {
            Location stopLocation = new Location("");
            stopLocation.setLongitude(listOfStopsToSort.get(i).getLon());
            stopLocation.setLatitude(listOfStopsToSort.get(i).getLat());
            Float distanceToUser = nodeToSortLocation.distanceTo(stopLocation);
            listOfStopsToSort.get(i).setDistanceFromSource(distanceToUser);
        }
        for (int i = 0; i < listOfStopsToSort.size(); i++) {
            int nearestToUserIndex = i;
            for (int j = i + 1; j < listOfStopsToSort.size(); j++) {
                if (listOfStopsToSort.get(j).getDistanceFromSource() < listOfStopsToSort.get(nearestToUserIndex).getDistanceFromSource()) {
                    nearestToUserIndex = j;
                }
            }
            if (nearestToUserIndex != i) {
                Collections.swap(listOfStopsToSort, i, nearestToUserIndex);
            }
        }
        for (int i = 0; i < listOfStopsToSort.size(); i++) {
            Log.e("inside function is", listOfStopsToSort.get(i).getName());
        }
        return listOfStopsToSort;
    }


    private NavigationPartialResults findShortestPath(NavigationNodes origin, NavigationNodes destination, boolean isTakingBus, boolean isWalking, boolean isFirstTime) {

        NavigationPartialResults routeForSegment = new NavigationPartialResults();

        PriorityQueue<NavigationNodes> pq = new PriorityQueue<NavigationNodes>(new Comparator<NavigationNodes>() {
            @Override
            public int compare(NavigationNodes o1, NavigationNodes o2) {
                return Integer.compare(o1.getWeightTillNow(), o2.getWeightTillNow());
            }
        });
        pq.add(origin);
        Log.e("origin & dest is", origin.getName() + " " + destination.getName());
        origin.setWeightTillNow(0);

        for (int i = 0; i < navNodes.length && !isFirstTime; i++) {
            if (navNodes[i] != null) {
                navNodes[i].setPrevNode(null);
                navNodes[i].setDiscovered(false);
            }
        }

        while (!pq.isEmpty()) {
            NavigationNodes extractedNode = pq.poll();
            extractedNode.setDiscovered(true);
            if (extractedNode.getNumid() == destination.getNumid()) {
                break;
            }
            for (int a = 0; a < extractedNode.getNavEdgesFromThisNode().size(); a++) {
                NavigationEdges neighbourEdge = extractedNode.getNavEdgesFromThisNode().get(a);
                NavigationNodes neighbourNode = navNodes[neighbourEdge.getTonumid()];
                if (!neighbourNode.isDiscovered()) {
                    if (neighbourNode.getWeightTillNow() > extractedNode.getWeightTillNow() + neighbourEdge.getDuration()) {
                        neighbourNode.setWeightTillNow(extractedNode.getWeightTillNow() + neighbourEdge.getDuration());
                        neighbourNode.setPrevNode(extractedNode);
                        neighbourNode.setEdgeSelected(neighbourEdge);
                        pq.remove(neighbourNode);
                        pq.add(neighbourNode);
                    }
                }
            }
        }

//        Stack<NavigationNodes> nodeStack = new Stack<>();
        NavigationNodes currentNode = destination;
        int totalTime = 0;
        List<NavigationNodes> pathToTake = new ArrayList<>();
        while (currentNode != null) {
//            nodeStack.push(currentNode);
            pathToTake.add(0, currentNode);
            Log.e("current node reversed", currentNode.getName());
            currentNode = currentNode.getPrevNode();
        }
        for (int b = 0; b < pathToTake.size(); b++) {
            Log.e("current node is", pathToTake.get(b).getName());
        }
        for (int b = 1; b < pathToTake.size() - 1; b++) {
            Log.e("path is", pathToTake.get(b + 1).getEdgeSelected().getFrom() + " " + pathToTake.get(b + 1).getEdgeSelected().getTo());
        }
        routeForSegment.setNodesTraversed(pathToTake);
        totalTime = destination.getWeightTillNow();
        routeForSegment.setTimeForSegment(totalTime);
        Log.e("total time", totalTime + "");
        return routeForSegment;
//        int findBusStart = 0;
//        while (pathToTake.get(findBusStart).getNumid() > 50) {
//            findBusStart++;
//        }
//        String firstBusStop = pathToTake.get(findBusStart).getId();
//        int findBusEnd = findBusStart;
//        while (pathToTake.get(findBusEnd).getNumid() < 50) {
//            findBusEnd++;
//        }
//        findBusEnd--;
//        String lastBusStop = pathToTake.get(findBusEnd).getId();
//        boolean isIncrementStart = true;
//        while (findBusStart != findBusEnd) {
//            findBusRoute(firstBusStop, lastBusStop);
//            if (isIncrementStart) {
//                findBusStart++;
//            } else {
//                findBusEnd--;
//            }
//        }

    }

    private boolean checkConditions2(boolean isTakingBus, boolean isWalking, NavigationEdges navEdge) {

        if (!isTakingBus && !isWalking) {
            return true;
        }
        if (isTakingBus && navEdge.getBy().equals("bus")) {
            return true;
        }
        if (isWalking && navEdge.getBy().equals("train")) {
            return true;
        }
        return false;

    }


    private void getListOfChildServices(Activity activity, String stopId, final VolleyCallBack callback) {

        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + stopId;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //variables for service info at a particular stop
                ServiceInStopDetails serviceInfoAtStop;
                List<ServiceInStopDetails> servicesAllInfoAtStop;
                List<String> servicesAtStop;
                List<String> serviceFirstArrival;
                List<String> serviceSecondArrival;
                List<String> firstArrivalLive;
                List<String> secondArrivalLive;
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

                callback.onSuccess(servicesAllInfoAtStop);


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
                params.put("Authorization", activity.getString(R.string.auth_header));
                return params;
            }
        };

        if (activity != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(activity);
            stopRequestQueue.add(stopStringRequest);
        }

//        Log.e("list is: ", list.toString());
//        return list;
    }

    private String loadJSONFromAsset(String fileName, Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
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

    @Override
    protected NavigationResults doInBackground(Void... voids) {
        return null;
    }

    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> servicesAllInfoAtStop);
    }

    public interface NavigationResultsFullyComplete {
        void onNavResultsComplete(List<NavigationResults> resultsList);
    }

    public interface BusNavComplete {
        void onBusNavSuccess(NavigationPartialResults busNavResult);
    }

    private interface SingleCombiBusNavComplete {
        void onSingleBusNavSuccess(NavigationResults singleBusNavResult);
    }

}
