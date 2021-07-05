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
import java.util.Iterator;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NavigationGraph extends AsyncTask<Void, Void, NavigationResults> {

    NavigationResultsFullyComplete navFinished;

    public NavigationNodes[] navNodes = new NavigationNodes[1000];

    List<NavigationNodes> listOfAllBusStops = new ArrayList<>();

    boolean mustBeSheltered = false;
    boolean mustBeAccessible = false;
    boolean isWalkOnly = false;

    int numberOfAttempts = 0;

    public void CreateNavGraph(@NotNull NavigationSearchInfo navigationSearchInfo, Context context) {
        try {
            JSONObject rawNavNodes = new JSONObject(loadJSONFromAsset("points.json", context));
            JSONObject rawNavEdges = new JSONObject(loadJSONFromAsset("graphedges.json", context));

            JSONArray nodeArray = rawNavNodes.getJSONArray("nodes");
            JSONArray edgeArray = rawNavEdges.getJSONArray("edges");

            mustBeSheltered = navigationSearchInfo.isSheltered();
            mustBeAccessible = navigationSearchInfo.isBarrierFree();
            isWalkOnly = navigationSearchInfo.isWalkOnly();

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
                if (arrayIndex < 50) {
                    listOfAllBusStops.add(tempNavNodes);
                }
            }

            for (int j = 0; j < edgeArray.length(); j++) {
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
                    tempNavEdge1.setEdgeDesc(edgeDetail.getString("edgeDesc1"));

                    if (!(tptType.equals("walk"))) {
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
                        tempNavEdge2.setEdgeDesc(edgeDetail.getString("edgeDesc2"));
                        List<NavigationEdges> tempListNavEdge2 = navNodes[arrayIndex2].getNavEdgesFromThisNode();
                        if (tempListNavEdge2 == null) {
                            tempListNavEdge2 = new ArrayList<>();
                        }
                        tempListNavEdge2.add(tempNavEdge2);
                        navNodes[arrayIndex2].setNavEdgesFromThisNode(tempListNavEdge2);
                    }
                    List<NavigationEdges> tempListNavEdge1 = navNodes[arrayIndex1].getNavEdgesFromThisNode();
                    if (tempListNavEdge1 == null) {
                        tempListNavEdge1 = new ArrayList<>();
                    }
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
    List<NavigationResults> returnListResults = new ArrayList<>();
    List<NavigationNodes> listOfFirstStopsInSegment = new ArrayList<>();

    int c = 0;
    boolean routeHasNoBus = true;

    boolean deleteLastEdge = true;

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

        for (c = 0; c < 4; c++) {

            boolean isFirstTime = true;
            NavigationResults fullRoute = new NavigationResults();

            if (c > 0 && listResults.size() > 0) {

                for (NavigationNodes navNode : navNodes) {
                    if (navNode != null) {
                        navNode.setPrevNode(null);
                        navNode.setDiscovered(false);
                        navNode.setWeightTillNow(999);
                    }
                }

                if (c == 3 && !routeHasNoBus) {
                    isWalkOnly = true;
                } else if (c == 3) {
                    break;
                } else if (isWalkOnly && !mustBeSheltered) {
//                    mustBeSheltered = true;
                    break;
                } else if (isWalkOnly || listResults.get(listResults.size() - 1).getResultsConcatenated().size() < 1) {
                    break;
                } else if (listResults.get(listResults.size() - 1).getResultsConcatenated().size() == 1 && deleteLastEdge) {
                    NavigationPartialResults segmentToOperate =
                            listResults.get(listResults.size() - 1).getResultsConcatenated().get(0);
                    NavigationNodes nodeToOperate = segmentToOperate.getNodesTraversed().get(segmentToOperate.getNodesTraversed().size() - 1);
                    NavigationNodes nodeToOperateStart = segmentToOperate.getNodesTraversed().get(segmentToOperate.getNodesTraversed().size() - 2);
                    List<NavigationEdges> edgeList = navNodes[nodeToOperateStart.getNumid()].getNavEdgesFromThisNode();
                    Log.e("edges start and end", nodeToOperateStart.getName() + " " + nodeToOperate.getName());
                    for (int d = 0; d < edgeList.size(); d++) {
                        Log.e("check edge", edgeList.get(d).getBy() + " " + edgeList.get(d).getTo() + " " + nodeToOperate.getName());
                        if (edgeList.get(d).getTonumid() == nodeToOperate.getNumid()) {
                            NavigationEdges navEdgeToOperate = edgeList.get(d);
                            navEdgeToOperate.setUsable(false);
                            edgeList.set(d, navEdgeToOperate);
                            navNodes[nodeToOperateStart.getNumid()].setNavEdgesFromThisNode(edgeList);
                            Log.e("edge deleted", navEdgeToOperate.getFrom() + " " + navEdgeToOperate.getTo());
                            break;
                        }
                    }
                } else if (listResults.get(listResults.size() - 1).getResultsConcatenated().size() == 1) {
                    NavigationPartialResults segmentToOperate =
                            listResults.get(listResults.size() - 1).getResultsConcatenated().get(0);
                    NavigationNodes nodeToOperate = segmentToOperate.getNodesTraversed().get(1);
                    NavigationNodes nodeToOperateStart = segmentToOperate.getNodesTraversed().get(0);
                    List<NavigationEdges> edgeList = navNodes[nodeToOperateStart.getNumid()].getNavEdgesFromThisNode();
                    Log.e("edges start and end", nodeToOperateStart.getName() + " " + nodeToOperate.getName());
                    for (int d = 0; d < edgeList.size(); d++) {
                        Log.e("check edge", edgeList.get(d).getBy() + " " + edgeList.get(d).getTo() + " " + nodeToOperate.getName());
                        if (edgeList.get(d).getTonumid() == nodeToOperate.getNumid()) {
                            NavigationEdges navEdgeToOperate = edgeList.get(d);
                            navEdgeToOperate.setUsable(false);
                            edgeList.set(d, navEdgeToOperate);
                            navNodes[nodeToOperateStart.getNumid()].setNavEdgesFromThisNode(edgeList);
                            Log.e("edge deleted", navEdgeToOperate.getFrom() + " " + navEdgeToOperate.getTo());
                            break;
                        }
                    }
                } else if (listResults.get(listResults.size() - 1).getResultsConcatenated().size() > 1 && deleteLastEdge) {
                    NavigationPartialResults segmentToOperate =
                            listResults.get(listResults.size() - 1).getResultsConcatenated().get(listResults.get(listResults.size() - 1).getResultsConcatenated().size() - 2);
                    NavigationNodes nodeToOperate = segmentToOperate.getNodesTraversed().get(segmentToOperate.getNodesTraversed().size() - 1);
                    NavigationNodes nodeToOperateStart = segmentToOperate.getNodesTraversed().get(segmentToOperate.getNodesTraversed().size() - 2);
                    List<NavigationEdges> edgeList = navNodes[nodeToOperateStart.getNumid()].getNavEdgesFromThisNode();
                    Log.e("edges start and end", nodeToOperateStart.getName() + " " + nodeToOperate.getName());
                    for (int d = 0; d < edgeList.size(); d++) {
                        Log.e("check edge", edgeList.get(d).getBy() + " " + edgeList.get(d).getTo() + " " + nodeToOperate.getName());
                        if (edgeList.get(d).getTonumid() == nodeToOperate.getNumid()) {
                            NavigationEdges navEdgeToOperate = edgeList.get(d);
                            navEdgeToOperate.setUsable(false);
                            edgeList.set(d, navEdgeToOperate);
                            navNodes[nodeToOperateStart.getNumid()].setNavEdgesFromThisNode(edgeList);
                            Log.e("edge deleted", navEdgeToOperate.getFrom() + " " + navEdgeToOperate.getTo());
                            break;
                        }
                    }
                } else if (listResults.get(listResults.size() - 1).getResultsConcatenated().size() > 1) {
                    Log.e("entered", "delete first edge");
                    NavigationPartialResults segmentToOperate =
                            listResults.get(listResults.size() - 1).getResultsConcatenated().get(listResults.get(listResults.size() - 1).getResultsConcatenated().size() - 2);
                    NavigationNodes nodeToOperate = segmentToOperate.getNodesTraversed().get(1);
                    NavigationNodes nodeToOperateStart = segmentToOperate.getNodesTraversed().get(0);
                    List<NavigationEdges> edgeList = navNodes[nodeToOperateStart.getNumid()].getNavEdgesFromThisNode();
                    Log.e("edges start and end", nodeToOperateStart.getName() + " " + nodeToOperate.getName());
                    for (int d = 0; d < edgeList.size(); d++) {
                        Log.e("check edge", edgeList.get(d).getBy() + " " + edgeList.get(d).getTo() + " " + nodeToOperate.getName());
                        if (edgeList.get(d).getTonumid() == nodeToOperate.getNumid()) {
                            NavigationEdges navEdgeToOperate = edgeList.get(d);
                            navEdgeToOperate.setUsable(false);
                            edgeList.set(d, navEdgeToOperate);
                            navNodes[nodeToOperateStart.getNumid()].setNavEdgesFromThisNode(edgeList);
                            Log.e("edge deleted", navEdgeToOperate.getFrom() + " " + navEdgeToOperate.getTo());
                            break;
                        }
                    }
                }

            }

            NavigationPartialResults trialResult = findShortestPath(origin, destination, false, false, true);

            if (trialResult == null || trialResult.getTimeForSegment() > 900
                    || trialResult.getNodesTraversed().size() <= 1 || numberOfAttempts >= 10) {
                Log.e("entered null", "331");
                if (listResults.size() == 0) {
                    navFinished.onNavResultsComplete(null, 2);
                } else {
                    for (NavigationResults temp : listResults) {
                        if (temp.isShowResult()) {
                            Log.e("copied over bc route failed", "yes");
                            returnListResults.add(temp);
                        }
                    }

                    navFinished.onNavResultsComplete(returnListResults, 0);
                }
                return;
            }
            if (isWalkOnly && trialResult.getNodesTraversed().size() > 0) {
                List<NavigationPartialResults> listOfSegments = new ArrayList<>();
                for (int j = 1; trialResult != null && j < trialResult.getNodesTraversed().size(); j++) {
                    trialResult.addEdgeSequence(trialResult.getNodesTraversed().get(j).getEdgeSelected());
                }
                listOfSegments.add(trialResult);
                fullRoute.setResultsConcatenated(listOfSegments);
                fullRoute.setTotalTimeTaken(trialResult.getTimeForSegment());
                listResults.add(fullRoute);

                for (NavigationResults temp : listResults) {
                    if (temp.isShowResult()) {
                        Log.e("copied over bc walk only", "yes");
                        returnListResults.add(temp);
                    }
                }

                navFinished.onNavResultsComplete(returnListResults, 0);
                return;
            }
            fullRoute.setTotalTimeTaken(trialResult.getTimeForSegment());

            List<NavigationMethodType> isTheMethodWalkingList = new ArrayList<>();
            boolean isTheCurrentMethodWalking = false;
            NavigationMethodType currentMethodType = new NavigationMethodType();
            routeHasNoBus = true;

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
            boolean addToList = true;

            for (int i = 0; i < isTheMethodWalkingList.size(); i++) {
                NavigationMethodType currentInstance = isTheMethodWalkingList.get(i);
                Log.e("list detail is", currentInstance.isWalking() + " "
                        + currentInstance.getOrigin().getName() + " " + currentInstance.getDest().getName());
                if (currentInstance.isWalking()) {
                    //this segment is walking
                    Log.e("entered wawlking", "yes");
                    NavigationPartialResults segmentResult = findShortestPath(currentInstance.getOrigin(),
                            currentInstance.getDest(), false, true, isFirstTime);
                    for (int j = 1; segmentResult != null && j < segmentResult.getNodesTraversed().size(); j++) {
                        segmentResult.addEdgeSequence(segmentResult.getNodesTraversed().get(j).getEdgeSelected());
                    }
                    isFirstTime = false;

                    listOfSegments.add(segmentResult);

                } else if (!isWalkOnly) {
                    //this segment is bus
                    List<NavigationPartialResults> busSegmentResult = findBusRoute(currentInstance.getOrigin(),
                            currentInstance.getDest(), context, isFirstTime);
                    Log.e("bussegmentresult size", busSegmentResult.size() + "");
                    isFirstTime = false;
                    if (busSegmentResult.size() == 0 || busSegmentResult.get(0) == null) {
                        addToList = false;
                        Log.e("break", "yes");
                        listOfSegments.clear();
                        break;
                    }
                    routeHasNoBus = false;
                    listOfSegments.addAll(busSegmentResult);
                }
            }

            fullRoute.setResultsConcatenated(listOfSegments);
            fullRoute.setFirstRunResult(trialResult);


            for (int a = 0; fullRoute.getResultsConcatenated() != null && a < fullRoute.getResultsConcatenated().size(); a++) {
                for (int b = 0; b < fullRoute.getResultsConcatenated().get(a).getNodesTraversed().size(); b++) {
                    Log.e("full route is", fullRoute.getResultsConcatenated().get(a).getNodesTraversed().get(b).getName() + " " + a + " " + b + " " + fullRoute.getResultsConcatenated().size());
                }
            }



            boolean isStopOrderExactlyTheSame = true;
            boolean operated = false;
            List<NavigationPartialResults> previousResultListOfSegments;
            if (addToList && listResults.size() > 0) {
                previousResultListOfSegments = listResults.get(listResults.size() - 1).getResultsConcatenated();
                if (fullRoute.getResultsConcatenated().size() == previousResultListOfSegments.size()) {
                    for (int a = 0; a < fullRoute.getResultsConcatenated().size(); a++) {
                        if (previousResultListOfSegments.get(a).getNodesTraversed().get(0).getNumid()
                                != fullRoute.getResultsConcatenated().get(a).getNodesTraversed().get(0).getNumid()) {
                            isStopOrderExactlyTheSame = false;
                        }
                    }
                    if (isStopOrderExactlyTheSame) {
                        Log.e("full entered", "stoporderexactlythesame");
                        fullRoute.setShowResult(false);
                        listResults.add(fullRoute);
                        c--;
                        deleteLastEdge = false;
                        operated = true;
                    }
                }
            }
            if (!operated) {
                if (addToList && resultToRemove != c) {
                    deleteLastEdge = true;
                    listResults.add(fullRoute);
                } else if (addToList) {
                    fullRoute.setShowResult(false);
                    deleteLastEdge = false;
                    listResults.add(fullRoute);
                    c--;
                    if (stopsAlreadyTransferred.size() > 0) {
                        stopsAlreadyTransferred.remove(stopsAlreadyTransferred.size() - 1);
                    }
                    Log.e("entered", "setShowResultFalse");
                }
            }
            resultToRemove = -1;
            numberOfAttempts++;
            Log.e("check boolean", deleteLastEdge + "");

        }
        for (NavigationResults temp : listResults) {
            if (temp.isShowResult()) {
                Log.e("copied over for all passed", "yes");
                returnListResults.add(temp);
            }
        }

        navFinished.onNavResultsComplete(returnListResults, 0);

    }

    List<Integer> stopsAlreadyTransferred = new ArrayList<>();
    int resultToRemove = -1;
    List<String> directServicesAlreadyEntered = new ArrayList<>();

    private List<NavigationPartialResults> findBusRoute(NavigationNodes originBusStop,
                                                  NavigationNodes destBusStop, Context context, boolean isFirstTime) {

        NavigationPartialResults routeToTake = findShortestPath(originBusStop, destBusStop, true, false, isFirstTime);
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
            } else if (checkDestCondition(stopId.get(i), destBusStop.getId(), originIndex >= 0)) {
                destIndex = i;
                Log.e("dest name", stopId.get(i));
            }
            if (originIndex >= 0 && destIndex >= 0 && destIndex > originIndex) {
                Log.e("services compared are",aServiceAtStop.get(originIndex) + " " + aServiceAtStop.get(destIndex) + " " + originIndex + " " + destIndex);
            }
            if (originIndex >= 0 && destIndex >= 0 && destIndex > originIndex
                    && aServiceAtStop.get(originIndex).equals(aServiceAtStop.get(destIndex))
                    && routeToTake.getNodesTraversed().size() - 1 <= destIndex - originIndex + 2
                    && routeToTake.getNodesTraversed().size() - 1 >= destIndex - originIndex - 2) { //TODO: +-2 is hardcoded due to route detours (D1/D2/A1/A2)
                Log.e("viable service", aServiceAtStop.get(originIndex) + " " + stopId.get(originIndex) + " " + stopId.get(destIndex));
                routeToTake.addViableBuses1(aServiceAtStop.get(originIndex));
                originIndex = -1;
                destIndex = -1;
            }
        }

        boolean checkAlreadyEntered = false;

        //no transfer required - a direct bus exists
        if (routeToTake.getViableBuses1().size() > 0 && directServicesAlreadyEntered.size() > 0) {
//            for (int i = 0; i < routeToTake.getViableBuses1().size(); i++) {
//                for (int j = 0; j < directServicesAlreadyEntered.size(); j++) {
//                    if (routeToTake.getViableBuses1().get(i).equals(directServicesAlreadyEntered.get(j))) {
//                        checkAlreadyEntered = true;
//                    }
//                }
//            }
        } else if (routeToTake.getViableBuses1().size() > 0 && directServicesAlreadyEntered.size() == 0) {
            checkAlreadyEntered = false;
        }

        Log.e("checkAlreadyEntered", checkAlreadyEntered + "");
//        if (checkAlreadyEntered) {
//            transferConcat.add(null);
//            Log.e("null", "added");
//            return transferConcat;
//        }
        if (routeToTake.getViableBuses1().size() > 0 && !checkAlreadyEntered) {

            Log.e("direct route exists", "yes");

            boolean stopAlreadyTransfered = false;
            for (Integer temp: stopsAlreadyTransferred) {
                if (c > 1 && temp == routeToTake.getNodesTraversed().get(0).getNumid()) {
                    resultToRemove = c;
                    stopAlreadyTransfered = true;
                    break;
                }
            }
            if (!stopAlreadyTransfered) {
                stopsAlreadyTransferred.add(routeToTake.getNodesTraversed().get(0).getNumid());
            }
            directServicesAlreadyEntered.addAll(routeToTake.getViableBuses1());
            routeToTake.setNodeSequence(remakeStopList(stopId,
                    aServiceAtStop, routeToTake.getViableBuses1(), originBusStop, destBusStop));

            transferConcat.add(routeToTake);

        } else {
            //check how to transfer

            int viableBuses2size = 0;
            int viableBuses1size = 0;

            Log.e("entered", " find transfer route");

            List<String> servicesAtDest = new ArrayList<>();
            List<String> servicesAtOrigin = new ArrayList<>();
            for (int i = 0; i < stopId.size(); i++) {
                if (stopId.get(i).equals(originBusStop.getId())) {
                    servicesAtOrigin.add(aServiceAtStop.get(i));
                    Log.e("origin service added", aServiceAtStop.get(i));
                } else if (checkDestCondition(stopId.get(i), destBusStop.getId(), true)) {
                    servicesAtDest.add(aServiceAtStop.get(i));
                    Log.e("dest service added", aServiceAtStop.get(i));
                }
            }


            List<Integer> transferStopNumIds = new ArrayList<>();
            List<NavigationNodes> nodesTraversedBeforeTransfer = new ArrayList<>();

            boolean transferPointAlreadySet = false;

            for (int i = 0; i < routeToTake.getNodesTraversed().size(); i++) {

                int timeBeforeTransfer = 0;

                NavigationNodes currentStop = routeToTake.getNodesTraversed().get(i);

                boolean hasStopBeenTransferredBefore = false;
                for (int j = 0; j < transferStopNumIds.size(); j++) {
                    if (currentStop.getNumid() == transferStopNumIds.get(j)) {
                        hasStopBeenTransferredBefore = true;
                    }
                }
                List<String> servicesAtIntermediateStop = new ArrayList<>();
                nodesTraversedBeforeTransfer.add(currentStop);

                if (!hasStopBeenTransferredBefore && i != 0 && i != routeToTake.getNodesTraversed().size() - 1) {

                    servicesAtIntermediateStop.clear();
                    List<String> viableBuses1 = new ArrayList<>();
                    List<String> viableBuses2 = new ArrayList<>();
//                    Log.e("currentstop is", currentStop.getName() + " " + nodesTraversedBeforeTransfer.size());
                    timeBeforeTransfer = currentStop.getWeightTillNow();
                    for (int j = 0; j < stopId.size(); j++) {
                        if (stopId.get(j).equals(currentStop.getId())) {
                            servicesAtIntermediateStop.add(aServiceAtStop.get(j));
//                            Log.e("transfer checking aServiceAtStop", aServiceAtStop.get(j));
                        }
                    }
                    for (int j = 0; j < servicesAtDest.size(); j++) {
                        for (int k = 0; k < servicesAtIntermediateStop.size(); k++) {
                            if (servicesAtDest.get(j).equals(servicesAtIntermediateStop.get(k))) {
                                int transferOriginIndex = -1;
                                int transferDestIndex = -1;
//                                Log.e("transfer 2nd check", servicesAtDest.get(j) + " " + servicesAtIntermediateStop.get(k));
                                for (int m = 0; m < stopId.size(); m++) {
//                                    Log.e("transfer 3rd check", currentStop.getId() + " " + destBusStop.getId() + " " + stopId.get(m) + " " + servicesAtDest.get(j) + " " + aServiceAtStop.get(m));
                                    if (currentStop.getId().equals(stopId.get(m)) && aServiceAtStop.get(m).equals(servicesAtDest.get(j))) {
                                        transferOriginIndex = m;
//                                        Log.e("origin name", stopId.get(m) + " " + transferOriginIndex);
                                    } else if (checkDestCondition(stopId.get(m), destBusStop.getId(), transferOriginIndex >= 0) && aServiceAtStop.get(m).equals(servicesAtDest.get(j))) {
                                        transferDestIndex = m;
//                                        Log.e("dest name", stopId.get(m) + " " + transferDestIndex);
                                    }
                                    if (transferOriginIndex >= 0 && transferDestIndex >= 0 && transferDestIndex > transferOriginIndex) {
                                        break;
                                    }
                                }
//                                Log.e("sizes", (routeToTake.getNodesTraversed().size() - i + " ") + (transferDestIndex - transferOriginIndex + ""));
                                if (transferOriginIndex < transferDestIndex
//                                        && (!transferPointAlreadySet || !servicesAtIntermediateStop.get(k).contains("BTC"))
                                        && routeToTake.getNodesTraversed().size() - i >= transferDestIndex - transferOriginIndex - 2 &&
                                        routeToTake.getNodesTraversed().size() - i <= transferDestIndex - transferOriginIndex + 2) {
                                    boolean isServiceAlreadyInside = false;
                                    for (int m = 0; m < viableBuses2.size(); m++) {
                                        if (servicesAtIntermediateStop.get(k).equals(viableBuses2.get(m))) {
                                            isServiceAlreadyInside = true;
                                        }
                                    }
                                    if (!isServiceAlreadyInside) {
                                        viableBuses2.add(servicesAtIntermediateStop.get(k));
//                                        Log.e("ViableBuses2Added", servicesAtIntermediateStop.get(k));
                                    }

                                }
                            }
                        }
                    }

                    if (viableBuses2.size() > 0 && viableBuses2.size() > viableBuses2size) {

                        for (int j = 0; j < servicesAtOrigin.size(); j++) {
                            for (int k = 0; k < servicesAtIntermediateStop.size(); k++) {
                                Log.e("match", servicesAtOrigin.get(j) + " " + servicesAtIntermediateStop.get(k));
                                if (servicesAtOrigin.get(j).equals(servicesAtIntermediateStop.get(k))) {
                                    int transferOriginIndex = -1;
                                    int transferDestIndex = -1;
                                    Log.e("transfer 2nd2nd check", servicesAtOrigin.get(j) + " " + servicesAtIntermediateStop.get(k));
                                    for (int m = 0; m < stopId.size(); m++) {
                                        Log.e("transfer 3rd3rd check", currentStop.getId() + " " + originBusStop.getId() + " " + stopId.get(m) + " " + servicesAtOrigin.get(j) + " " + aServiceAtStop.get(m));
                                        if (checkDestCondition(stopId.get(m), currentStop.getId(), transferOriginIndex >= 0) && aServiceAtStop.get(m).equals(servicesAtOrigin.get(j))) {
                                            transferDestIndex = m;
                                            Log.e("transfer dest name", stopId.get(m));
                                        } else if (originBusStop.getId().equals(stopId.get(m)) && aServiceAtStop.get(m).equals(servicesAtOrigin.get(j))) {
                                            transferOriginIndex = m;
                                            Log.e("transfer origin name", stopId.get(m));
                                        }
                                        if (transferOriginIndex >= 0 && transferDestIndex >= 0 && transferDestIndex > transferOriginIndex) {
                                            break;
                                        }
                                    }
                                    if (transferOriginIndex < transferDestIndex && i + 1 >= transferDestIndex - transferOriginIndex - 2 &&
                                            i + 1 <= transferDestIndex - transferOriginIndex + 2) {
                                        boolean isServiceAlreadyInside = false;
                                        for (int m = 0; m < viableBuses1.size(); m++) {
                                            if (servicesAtIntermediateStop.get(k).equals(viableBuses1.get(m))) {
                                                isServiceAlreadyInside = true;
//                                                Log.e("ViableBuses1AlreadyInside", servicesAtIntermediateStop.get(k));
                                            }
                                        }
                                        if (!isServiceAlreadyInside) {
                                            viableBuses1.add(servicesAtIntermediateStop.get(k));
//                                            Log.e("ViableBuses1Added", servicesAtIntermediateStop.get(k));
                                        }
                                    }
                                }
                            }
                        }
                        Log.e("vB1 + vB2", currentStop.getName() + " " + viableBuses1.size() + " " + viableBuses2.size() + " / " + viableBuses1size + " " + viableBuses2size);

                        if (viableBuses1.size() > 0 && viableBuses1.size() >= viableBuses1size) {
                            for (int j = 0; j < stopsAlreadyTransferred.size(); j++) {
                                if (stopsAlreadyTransferred.get(j) == currentStop.getNumid()) {
                                    resultToRemove = c;
                                    break;
                                }
                            }
                            //transfer route found
                            transferPointAlreadySet = true;
                            stopsAlreadyTransferred.add(currentStop.getNumid());
                            Log.e("transfer route found at", currentStop.getName());
                            viableBuses1size = viableBuses1.size();
                            viableBuses2size = viableBuses2.size();

                            //for transfer use
                            transferConcat.clear();
                            NavigationPartialResults routeBeforeTransfer = new NavigationPartialResults();
                            NavigationPartialResults routeAfterTransfer = new NavigationPartialResults();

                            transferStopNumIds.add(currentStop.getNumid());

                            List<NavigationNodes> nodesBeforeTransferToInsert = new ArrayList<>(nodesTraversedBeforeTransfer);
                            routeBeforeTransfer.setBeforeTransferNodesTraversed(nodesBeforeTransferToInsert);
                            routeBeforeTransfer.setNodesTraversed(nodesBeforeTransferToInsert);
                            routeBeforeTransfer.setTimeForSegment(currentStop.getWeightTillNow());
                            routeBeforeTransfer.setViableBuses1(viableBuses1);
                            routeBeforeTransfer.setNodeSequence(remakeStopList(stopId,
                                    aServiceAtStop, routeBeforeTransfer.getViableBuses1(), originBusStop, currentStop));

                            routeAfterTransfer.setTransferStop(currentStop);
                            routeAfterTransfer.setViableBuses2(viableBuses2);
                            for (int k = i; k < routeToTake.getNodesTraversed().size(); k++) {
                                routeAfterTransfer.setAfterTransferNodesTraversedIndiv(routeToTake.getNodesTraversed().get(k));
                                routeAfterTransfer.setTimeForSegment(routeToTake.getNodesTraversed().get(k).getWeightTillNow()
                                        - routeToTake.getNodesTraversed().get(i).getWeightTillNow());
                            }
                            routeAfterTransfer.setNodesTraversed(routeAfterTransfer.getAfterTransferNodesTraversed());
                            routeAfterTransfer.setNodeSequence(remakeStopList(stopId,
                                    aServiceAtStop, routeAfterTransfer.getViableBuses2(), currentStop, destBusStop));

                            transferConcat.add(routeBeforeTransfer);
                            transferConcat.add(routeAfterTransfer);
                        }

//                        int howManyServicesAreTheSame = 0;
//                        Boolean areAllTheServicesTheSame = false;
//                        for (int a = 0; a < transferConcat.size(); a += 2) {
//                            for (int b = 0; b < viableBuses2.size(); b++) {
//                                for (int c = 0; c < transferConcat.get(a + 1).getViableBuses2().size(); c++) {
//                                    if (transferConcat.get(a + 1).getViableBuses2().get(c).equals(viableBuses2.get(b))) {
//                                        howManyServicesAreTheSame++;
//                                    }
//                                }
//                            }
//                            if (howManyServicesAreTheSame == transferConcat.get(a + 1).getViableBuses2().size()) {
//                                areAllTheServicesTheSame = true;
//                                break;
//                            }
//                        }
//                        if (!areAllTheServicesTheSame) {


//                                for (int k = 0; k < currentStop.getNavEdgesFromThisNode().size(); k++) {
//                                    if (currentStop.getNavEdgesFromThisNode().get(k).getTonumid() == currentStop.getNumid()) {
//                                        navNodes[currentStop.getNumid()].getNavEdgesFromThisNode().get(k).setUsable(false);
//                                    }
//                                }
//                                break;
//                        }
                    }
                }
            }

        }

        return transferConcat;

    }

    private void checkIfTransferIsRequired(List<ServiceInStopDetails> originStopInfo, List<ServiceInStopDetails> destStopInfo) {

    }

    //TODO: hardcoded exceptions need to be changed when new ISB network begins
    private boolean checkDestCondition(String stopId, String fixedStop, boolean originSet) {
        if (originSet && (fixedStop.equals(stopId)
                || (fixedStop.equals("KR-BT") && (stopId.contains("KTR") || stopId.equals("KR-BTE")))
                || (fixedStop.equals("PGPT") && stopId.contains("PGPE")))) {
            return true;
        }
        return false;
    }

    private List<NavigationNodes> remakeStopList(List<String> stopId, List<String> aServiceAtStop, List<String> busList, NavigationNodes firstStop, NavigationNodes lastStop) {

        List<NavigationNodes> maxNodesTraversed = new ArrayList<>();
        int maxNodesTraversedSize = 0;
        for (int a = 0; a < busList.size(); a++) {
            List<NavigationNodes> checkThisService = new ArrayList<>();
            boolean startAdding = false;
            for (int j = 0; j < stopId.size(); j++) {
                if (busList.get(a).equals(aServiceAtStop.get(j))) {
                    Log.e("remaking service being checked", busList.get(a));
                    if (stopId.get(j).equals(firstStop.getId())) {
                        Log.e("start adding here", stopId.get(j) + " " + firstStop.getId());
                        startAdding = true;
                        checkThisService.clear();
                    } else if (j > 0 && aServiceAtStop.get(j - 1).equals(aServiceAtStop.get(j))
                            && checkDestCondition(stopId.get(j), lastStop.getId(), startAdding)) {
                        startAdding = false;
                        for (int k = 0; k < listOfAllBusStops.size(); k++) {
                            if (checkDestCondition(stopId.get(j), listOfAllBusStops.get(k).getId(), true)) {
                                Log.e("remaking final stop being added", listOfAllBusStops.get(k).getName());
                                checkThisService.add(listOfAllBusStops.get(k));
                                break;
                            }
                        }
                        break;
                    }
                    if (startAdding) {
                        for (int k = 0; k < listOfAllBusStops.size(); k++) {
                            if (checkDestCondition(stopId.get(j), listOfAllBusStops.get(k).getId(), true)) {
//                                                    Log.e("remaking stop being added", listOfAllBusStops.get(k).getName());
                                checkThisService.add(listOfAllBusStops.get(k));
                                break;
                            }
                        }
                    }
                }
            }
            if (checkThisService.size() > maxNodesTraversedSize) {
                maxNodesTraversed = checkThisService;
                maxNodesTraversedSize = checkThisService.size();
            }
        }

        return maxNodesTraversed;

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


    private NavigationPartialResults findShortestPath(NavigationNodes origin,
                                                      NavigationNodes destination, boolean isTakingBus, boolean isWalking, boolean isFirstTime) {

        for (int i = 0; i < navNodes.length && !isFirstTime; i++) {
            if (navNodes[i] != null) {
                navNodes[i].setPrevNode(null);
                navNodes[i].setDiscovered(false);
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
                    if (!neighbourNode.isDiscovered() && neighbourEdge.isUsable() && (!isWalkOnly || (neighbourEdge.getBy().equals("walk")))) {
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
//            Log.e("current node reversed", currentNode.getName());
            currentNode = currentNode.getPrevNode();
        }
        for (int b = 0; b < pathToTake.size(); b++) {
            Log.e("current node is", pathToTake.get(b).getName());
        }
        for (int b = 1; b < pathToTake.size() - 1; b++) {
//            Log.e("path is", pathToTake.get(b).getEdgeSelected().getFrom() + " " + pathToTake.get(b).getEdgeSelected().getTo());
        }
        routeForSegment.setNodesTraversed(pathToTake);
        totalTime = destination.getWeightTillNow();
        routeForSegment.setTimeForSegment(totalTime);
        Log.e("total time", totalTime + "");

        return routeForSegment;
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
}
