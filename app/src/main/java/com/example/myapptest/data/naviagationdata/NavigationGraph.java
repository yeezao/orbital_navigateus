package com.example.myapptest.data.naviagationdata;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.navigation.Navigation;

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
    boolean isWalkOnly = false;

    public void CreateNavGraph(NavigationSearchInfo navigationSearchInfo, Context context) {
        try {
            JSONObject rawNavNodes = new JSONObject(loadJSONFromAsset("points.json", context));
            JSONObject rawNavEdges = new JSONObject(loadJSONFromAsset("graphedges.json", context));

            JSONArray nodeArray = rawNavNodes.getJSONArray("nodes");
            JSONArray edgeArray = rawNavEdges.getJSONArray("edges");

            mustBeSheltered = navigationSearchInfo.isSheltered();
            mustBeAccessible = navigationSearchInfo.isBarrierFree();
            isWalkOnly = navigationSearchInfo.isWalkOnly();

            Log.e("conditions review", (mustBeSheltered + "" + mustBeAccessible + isWalkOnly + ""));

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
                tempNavNodes.setNavEdgesFromThisNode(new ArrayList<>());
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
                if (checkConditions(edgeDetail)) {
                    int arrayIndex1 = edgeDetail.getInt("fromnumid");
                    String tptType = edgeDetail.getString("by");
                    NavigationEdges tempNavEdge1 = new NavigationEdges();
                    tempNavEdge1.setFrom(edgeDetail.getString("from"));
                    tempNavEdge1.setFromnumid(edgeDetail.getInt("fromnumid"));
                    tempNavEdge1.setTo(edgeDetail.getString("to"));
                    tempNavEdge1.setTonumid(edgeDetail.getInt("tonumid"));
                    tempNavEdge1.setBy(tptType);
                    tempNavEdge1.setDuration(edgeDetail.getInt("duration"));


                    if (!tptType.equals("walk")) {
    //                    JSONArray serviceArray = edgeDetail.getJSONArray("services");
    //                    List<String> serviceArrayToAdd = new ArrayList<>();
    //                    for (int k = 0; k < serviceArray.length(); k++) {
    //                        serviceArrayToAdd.add(serviceArray.getString(k));
    //                    }
    //                    tempNavEdge1.setServices(serviceArrayToAdd);
                        tempNavEdge1.setServices(null);
                        tempNavEdge1.setAccessible(true);
                        tempNavEdge1.setSheltered(true);
                    } else if (tptType.equals("walk")) {
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

    private boolean checkConditions(JSONObject edgeDetail) throws JSONException {
        if ((!mustBeSheltered && !mustBeAccessible & !isWalkOnly)) {
            return true;
        } else if (mustBeSheltered && mustBeAccessible && isWalkOnly && edgeDetail.getString("sheltered").equals("y")
                && edgeDetail.getString("accessible").equals("y") && edgeDetail.getString("by").equals("walk")) {
            return true;
        } else if (mustBeAccessible && mustBeSheltered && !isWalkOnly && edgeDetail.getString("sheltered").equals("y") && edgeDetail.getString("accessible").equals("y")) {
            return true;
        } else if (mustBeSheltered && isWalkOnly && !mustBeAccessible && edgeDetail.getString("sheltered").equals("y") && edgeDetail.getString("by").equals("walk")) {
            return true;
        } else if (mustBeAccessible && isWalkOnly && !mustBeSheltered && edgeDetail.getString("accessible").equals("y") && edgeDetail.getString("by").equals("walk")) {
            return true;
        } else if (mustBeSheltered && !mustBeAccessible && !isWalkOnly && edgeDetail.getString("sheltered").equals("y")) {
            return true;
        } else if (mustBeAccessible && !mustBeSheltered && !isWalkOnly && edgeDetail.getString("accessible").equals("y")) {
            return true;
        } else if (isWalkOnly && !mustBeSheltered && !mustBeAccessible && edgeDetail.getString("by").equals("walk")) {
            return true;
        }
        return false;
    }

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
                    navFinished.onNavResultsComplete(null, 1);
                    return;
                }
            } catch (NullPointerException e) {

            }
        }

        if (origin == null || destination == null || !originSet || !destSet) {
            navFinished.onNavResultsComplete(null, 1);
            return;
        }
//
//        List<NavigationNodes> originStopDistanceList = new ArrayList<>();
//        List<NavigationNodes> destStopDistanceList = new ArrayList<>();
//
//        Log.e("are they the same", (originStopDistanceList == destStopDistanceList) + "");
//
//        //TODO: fix this
//        destStopDistanceList = sortBusStopDistance(destination, listOfAllBusStops);
//        originStopDistanceList = sortBusStopDistance(origin, listOfAllBusStops);
//
//
        boolean isFirstTime = true;

        NavigationResults fullRoute = new NavigationResults();

        NavigationNodes[] navNodesInstance = new NavigationNodes[1000];
        for (int i = 0; i < navNodes.length; i++) {
            navNodesInstance[i] = navNodes[i];
        }

        NavigationPartialResults trialResult = findShortestPath(navNodesInstance, origin, destination, false, false, isFirstTime);
        if (trialResult == null || trialResult.getTimeForSegment() > 900) {
            navFinished.onNavResultsComplete(null, 2);
            return;
        }
        fullRoute.setTotalTimeTaken(trialResult.getTimeForSegment());

        List<NavigationMethodType> isTheMethodWalkingList = new ArrayList<>();
        boolean isTheCurrentMethodWalking = false;
        NavigationMethodType currentMethodType = new NavigationMethodType();

        //determine route walk/bus order
        for (int i = 1; i < trialResult.getNodesTraversed().size(); i++) {
            Log.e("i is", i + "");
            Log.e("path is", trialResult.getNodesTraversed().get(i).getEdgeSelected().getFrom() + " " + trialResult.getNodesTraversed().get(i).getEdgeSelected().getTo());
            if (trialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("walk") && !isTheCurrentMethodWalking) {
                if (i > 1) {
                    Log.e("dest is", trialResult.getNodesTraversed().get(i - 1).getName());
                    currentMethodType.setDest(trialResult.getNodesTraversed().get(i - 1));
                    isTheMethodWalkingList.add(currentMethodType);
                    currentMethodType = new NavigationMethodType();
                }
                isTheCurrentMethodWalking = true;
                currentMethodType.setWalking(true);
                Log.e("origin is", trialResult.getNodesTraversed().get(i - 1).getName());
                currentMethodType.setOrigin(trialResult.getNodesTraversed().get(i - 1));
            } else if (isTheCurrentMethodWalking && !trialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("walk")) {
                Log.e("dest is", trialResult.getNodesTraversed().get(i - 1).getName());
                currentMethodType.setDest(trialResult.getNodesTraversed().get(i - 1));
                isTheMethodWalkingList.add(currentMethodType);
                currentMethodType = new NavigationMethodType();
                isTheCurrentMethodWalking = false;
                currentMethodType.setWalking(false);
                Log.e("origin is", trialResult.getNodesTraversed().get(i - 1).getName());
                currentMethodType.setOrigin(trialResult.getNodesTraversed().get(i - 1));
            } else if (i == 1 && !trialResult.getNodesTraversed().get(i).getEdgeSelected().getBy().equals("walk")) {
                isTheCurrentMethodWalking = false;
                currentMethodType.setWalking(false);
                Log.e("origin is", trialResult.getNodesTraversed().get(0).getName());
                currentMethodType.setOrigin(trialResult.getNodesTraversed().get(0));
            }
        }
        currentMethodType.setDest(trialResult.getNodesTraversed().get(trialResult.getNodesTraversed().size() - 1));
        isTheMethodWalkingList.add(currentMethodType);

        List<NavigationPartialResults> listOfSegments = new ArrayList<>();
        List<List<NavigationPartialResults>> listOfListOfSegments = new ArrayList(listOfSegments);

        int currentIndexToAdd = 0;

        for (int i = 0; i < isTheMethodWalkingList.size(); i++) {
            NavigationMethodType currentInstance = isTheMethodWalkingList.get(i);
            Log.e("list detail is", currentInstance.isWalking() + " "
                    + currentInstance.getOrigin().getName() + " " + currentInstance.getDest().getName());
            if (currentInstance.isWalking()) {
                //this segment is walking
                Log.e("entered wawlking", "yes");
                NavigationNodes navNodesWalking[] = new NavigationNodes[navNodes.length];
                for (int j = 0; j < navNodes.length; j++) {
                    navNodesWalking[j] = navNodes[j];
                }
                NavigationPartialResults segmentResult = findShortestPath(navNodes, currentInstance.getOrigin(),
                        currentInstance.getDest(), false, true, isFirstTime);
                isFirstTime = false;

                listOfSegments.add(segmentResult);

            } else if (!isWalkOnly) {
                //this segment is bus
                List<NavigationPartialResults> busSegmentResult = findBusRoute(navNodes,
                        currentInstance.getOrigin(), currentInstance.getDest(), context, isFirstTime);
                Log.e("bussegmentresult size", busSegmentResult.size() + "");
                isFirstTime = false;
                for (int j = 0; j < busSegmentResult.size(); j++) {
                    listOfSegments.add(busSegmentResult.get(j));
                }
//                if (busSegmentResult.size() == 1) {
////                    listOfListOfSegments.get(0).add(i, busSegmentResult.get(0));
//                } else {
//                    for (int j = 0; j < busSegmentResult.size() / 2; j++) {
//                        if (listOfListOfSegments.size() < j + 1) {
//                            listOfListOfSegments.add(new ArrayList<>());
//                        }
//                        listOfListOfSegments.get(j).add(i, busSegmentResult.get(j * 2));
//                        listOfListOfSegments.get(j).add(i + 1, busSegmentResult.get(j * 2 + 1));
//                    }
//                }
            }
        }

        fullRoute.setResultsConcatenated(listOfSegments);

        for (int a = 0; a < fullRoute.getResultsConcatenated().size(); a++) {
            for (int b = 0; b < fullRoute.getResultsConcatenated().get(a).getNodesTraversed().size(); b++) {
                Log.e("full route is", fullRoute.getResultsConcatenated().get(a).getNodesTraversed().get(b).getName() + " " + a + " " + b + " " + fullRoute.getResultsConcatenated().size());
            }
        }

        listResults.add(fullRoute);

//            //TODO: do stuff
//


        //TODO: sort list according to timings and characteristics

//        Log.e("check inner", listResults.get(0).getTotalTimeTaken() + "");

        navFinished.onNavResultsComplete(listResults, 0);

    }


    private List<NavigationPartialResults> findBusRoute(NavigationNodes[] navNodesBus, NavigationNodes originBusStop,
                                                  NavigationNodes destBusStop, Context context, boolean isFirstTime) {

        NavigationPartialResults routeToTake = findShortestPath(navNodesBus, originBusStop, destBusStop, true, false, isFirstTime);
        Log.e("time before arrival info", routeToTake.getTimeForSegment() + "");

        String jsonBusInfo = loadJSONFromAsset("listOfBusStopsByService_full.json", context);

        List<String> stopId = JsonPath.read(jsonBusInfo, "$.pickuppoint[*].busstopcode");
        List<String> aServiceAtStop = JsonPath.read(jsonBusInfo, "$.pickuppoint[*].routeid");

        int originIndex = -1;
        int destIndex = -1;

        List<NavigationPartialResults> transferConcat = new ArrayList<>();



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

//        if (routeToTake.getViableBuses1().size() == 0) {
//            return null;
//        }

        //no transfer required - a direct bus exists
        if (routeToTake.getViableBuses1().size() > 0) {
            transferConcat.add(routeToTake);
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

            Log.e("entered", " find transfer route");

            List<String> servicesAtDest = new ArrayList<>();
            List<String> servicesAtOrigin = new ArrayList<>();
            for (int i = 0; i < stopId.size(); i++) {
                if (stopId.get(i).equals(originBusStop.getId())) {
                    servicesAtOrigin.add(aServiceAtStop.get(i));
                } else if (stopId.get(i).equals(destBusStop.getId())) {
                    servicesAtDest.add(aServiceAtStop.get(i));
                }
            }
            List<String> viableBuses1 = new ArrayList<>();
            List<String> viableBuses2 = new ArrayList<>();
            List<String> servicesAtIntermediateStop = new ArrayList<>();
            List<NavigationNodes> nodesTraversedBeforeTransfer = new ArrayList<>();
            List<Integer> transferStopNumIds = new ArrayList<>();

//            for (int h = 0; h < 2; h++) {

                for (int i = 0; i < routeToTake.getNodesTraversed().size(); i++) {

                    int timeBeforeTransfer = 0;

                    NavigationNodes currentStop = routeToTake.getNodesTraversed().get(i);

                    boolean hasStopBeenTransferredBefore = false;
                    for (int j = 0; j < transferStopNumIds.size(); j++) {
                        if (currentStop.getNumid() == transferStopNumIds.get(j)) {
                            hasStopBeenTransferredBefore = true;
                        }
                    }

                    nodesTraversedBeforeTransfer.add(currentStop);

                    if (!hasStopBeenTransferredBefore && i != 0 && i != routeToTake.getNodesTraversed().size() - 1) {

                        servicesAtIntermediateStop.clear();
                        viableBuses1.clear();
                        viableBuses2.clear();
                        Log.e("currentstop is", currentStop.getName() + " " + nodesTraversedBeforeTransfer.size());
                        timeBeforeTransfer = currentStop.getWeightTillNow();
                        for (int j = 0; j < stopId.size(); j++) {
                            if (stopId.get(j).equals(currentStop.getId())) {
                                servicesAtIntermediateStop.add(aServiceAtStop.get(j));
                            }
                        }
                        for (int j = 0; j < servicesAtDest.size(); j++) {
                            for (int k = 0; k < servicesAtIntermediateStop.size(); k++) {
                                if (servicesAtDest.get(j).equals(servicesAtIntermediateStop.get(k))) {
                                    viableBuses2.add(servicesAtIntermediateStop.get(k));
                                    Log.e("ViableBuses2Added", servicesAtIntermediateStop.get(k));
                                }
                            }
                        }
                        if (viableBuses2.size() > 0) {
                            //transfer route found
                            int howManyServicesAreTheSame = 0;
                            Boolean areAllTheServicesTheSame = false;
                            for (int a = 0; a < transferConcat.size(); a += 2) {
                                for (int b = 0; b < viableBuses2.size(); b++) {
                                    for (int c = 0; c < transferConcat.get(a + 1).getViableBuses2().size(); c++) {
                                        if (transferConcat.get(a + 1).getViableBuses2().get(c).equals(viableBuses2.get(b))) {
                                            howManyServicesAreTheSame++;
                                        }
                                    }
                                }
                                if (howManyServicesAreTheSame == transferConcat.get(a + 1).getViableBuses2().size()) {
                                    areAllTheServicesTheSame = true;
                                    break;
                                }
                            }
                            if (!areAllTheServicesTheSame) {

                                //for transfer use
                                NavigationPartialResults routeBeforeTransfer = new NavigationPartialResults();
                                NavigationPartialResults routeAfterTransfer = new NavigationPartialResults();

                                transferStopNumIds.add(currentStop.getNumid());
                                routeBeforeTransfer.setBeforeTransferNodesTraversed(nodesTraversedBeforeTransfer);
                                routeBeforeTransfer.setNodesTraversed(nodesTraversedBeforeTransfer);
                                Log.e("routeBeforeTransfer size", routeBeforeTransfer.getNodesTraversed().size() + "");
                                routeAfterTransfer.setTransferStop(currentStop);
                                routeAfterTransfer.setViableBuses2(viableBuses2);
                                Log.e("transfer stop is", currentStop.getName());
                                for (int k = 0; k < servicesAtOrigin.size(); k++) {
                                    for (int m = 0; m < servicesAtIntermediateStop.size(); m++) {
                                        if (servicesAtOrigin.get(k).equals(servicesAtIntermediateStop.get(m))) {
                                            boolean isAlreadyInList = false;
                                            for (int n = 0; n < viableBuses1.size(); n++) {
                                                Log.e("check equality", viableBuses1.get(n) + " " + servicesAtIntermediateStop.get(m));
                                                if (viableBuses1.get(n).equals(servicesAtIntermediateStop.get(m))) {
                                                    isAlreadyInList = true;
                                                }
                                            }
                                            if (!isAlreadyInList) {
                                                viableBuses1.add(servicesAtIntermediateStop.get(m));
                                                Log.e("ViableBuses1Added", servicesAtIntermediateStop.get(m));
                                            }
                                        }
                                    }
                                }
                                routeBeforeTransfer.setViableBuses1(viableBuses1);
                                for (int k = i; k < routeToTake.getNodesTraversed().size(); k++) {
                                    routeAfterTransfer.setAfterTransferNodesTraversedIndiv(routeToTake.getNodesTraversed().get(k));
                                    routeAfterTransfer.setTimeForSegment(routeToTake.getNodesTraversed().get(k).getWeightTillNow()
                                            - routeToTake.getNodesTraversed().get(i).getWeightTillNow());
                                }
                                routeAfterTransfer.setNodesTraversed(routeAfterTransfer.getAfterTransferNodesTraversed());
                                transferConcat.add(routeBeforeTransfer);
                                transferConcat.add(routeAfterTransfer);
                                for (int k = 0; k < currentStop.getNavEdgesFromThisNode().size(); k++) {
                                    if (currentStop.getNavEdgesFromThisNode().get(k).getTonumid() == currentStop.getNumid()) {
                                        navNodesBus[currentStop.getNumid()].getNavEdgesFromThisNode().get(k).setUsable(false);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
//            }



        }

        return transferConcat;

    }

    private void checkIfTransferIsRequired(List<ServiceInStopDetails> originStopInfo, List<ServiceInStopDetails> destStopInfo) {

    }

//    private List<NavigationNodes> sortBusStopDistance(NavigationNodes nodeToSort, List<NavigationNodes> listOfStopsToSort) {
//
//        Location nodeToSortLocation = new Location("");
//        nodeToSortLocation.setLatitude(nodeToSort.getLat());
//        nodeToSortLocation.setLongitude(nodeToSort.getLon());
//        for (int i = 0; i < listOfStopsToSort.size(); i++) {
//            Location stopLocation = new Location("");
//            stopLocation.setLongitude(listOfStopsToSort.get(i).getLon());
//            stopLocation.setLatitude(listOfStopsToSort.get(i).getLat());
//            Float distanceToUser = nodeToSortLocation.distanceTo(stopLocation);
//            listOfStopsToSort.get(i).setDistanceFromSource(distanceToUser);
//        }
//        for (int i = 0; i < listOfStopsToSort.size(); i++) {
//            int nearestToUserIndex = i;
//            for (int j = i + 1; j < listOfStopsToSort.size(); j++) {
//                if (listOfStopsToSort.get(j).getDistanceFromSource() < listOfStopsToSort.get(nearestToUserIndex).getDistanceFromSource()) {
//                    nearestToUserIndex = j;
//                }
//            }
//            if (nearestToUserIndex != i) {
//                Collections.swap(listOfStopsToSort, i, nearestToUserIndex);
//            }
//        }
//        for (int i = 0; i < listOfStopsToSort.size(); i++) {
//            Log.e("inside function is", listOfStopsToSort.get(i).getName());
//        }
//
//        List<NavigationNodes> newList = new ArrayList<>(listOfStopsToSort);
//        return newList;
//    }


    private NavigationPartialResults findShortestPath(NavigationNodes[] navNodesArray, NavigationNodes origin, 
                                                      NavigationNodes destination, boolean isTakingBus, boolean isWalking, boolean isFirstTime) {

        for (int i = 0; i < navNodesArray.length && !isFirstTime; i++) {
            if (navNodesArray[i] != null) {
                navNodesArray[i].setPrevNode(null);
                navNodesArray[i].setDiscovered(false);
            }
        }

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

        try {
            while (!pq.isEmpty()) {
                NavigationNodes extractedNode = pq.poll();
                extractedNode.setDiscovered(true);
                if (extractedNode.getNumid() == destination.getNumid()) {
                    break;
                }
                for (int a = 0; a < extractedNode.getNavEdgesFromThisNode().size(); a++) {
                    NavigationEdges neighbourEdge = extractedNode.getNavEdgesFromThisNode().get(a);
                    NavigationNodes neighbourNode = navNodes[neighbourEdge.getTonumid()];
                    if (!neighbourNode.isDiscovered() && neighbourEdge.isUsable()) {
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
        } catch (NullPointerException e) {
            return null;
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
        void onNavResultsComplete(List<NavigationResults> resultsList, int i);
    }

    public interface BusNavComplete {
        void onBusNavSuccess(NavigationPartialResults busNavResult);
    }

    private interface SingleCombiBusNavComplete {
        void onSingleBusNavSuccess(NavigationResults singleBusNavResult);
    }

}
