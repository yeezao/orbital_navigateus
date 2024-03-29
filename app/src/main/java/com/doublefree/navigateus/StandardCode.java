package com.doublefree.navigateus;

import android.app.Activity;
import android.content.Context;

import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.google.android.material.snackbar.Snackbar;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kotlin.text.Regex;

public class StandardCode {

    /**
     * Static method to package JSON response for list of all bus stops into {@link List<StopList>} object
     *
     * @param jsonResponse JSON response string from API call
     * @return {@link List<StopList>} object from the JSON response
     */
    public static List<StopList> packageStopListFromPickupPoint(String jsonResponse) {

        List<StopList> listOfAllStops = new ArrayList<>();
        StopList listOfStops;
        List<String> listOfNames;
        List<String> listOfIds;
        List<Double> listOfLat;
        List<Double> listOfLong;

        listOfNames = JsonPath.read(jsonResponse, "$.PickupPointResult.pickuppoint[*].pickupname");
        listOfIds = JsonPath.read(jsonResponse, "$.PickupPointResult.pickuppoint[*].busstopcode");
        listOfLong = JsonPath.read(jsonResponse, "$.PickupPointResult.pickuppoint[*].lng");
        listOfLat = JsonPath.read(jsonResponse, "$.PickupPointResult.pickuppoint[*].lat");
        for (int i = 0; i < listOfNames.size(); i++) {
            listOfStops = new StopList();
            listOfStops.setStopName(listOfNames.get(i));
            listOfStops.setStopId(listOfIds.get(i));
            listOfStops.setStopLongitude(listOfLong.get(i));
            listOfStops.setStopLatitude(listOfLat.get(i));
            listOfAllStops.add(listOfStops);
        }
        return listOfAllStops;

    }

    /**
     * Method to check: if stopId is a terminal stopId, modify it to the starting stopId
     *
     * NOTE: this is hardcoded, and needs to be modified whenever the ISB network changes
     *
     * @param stopId stopId to be checked
     * @return stopId - modified (if conditions true) or unmodified stopId
     */
    //TODO: this needs to be modified when new bus network is up
    public static String StopIdExceptionsWithReturn(String stopId) {
        if (stopId.contains("COM2")) {
            return "COM2";
        } else if (stopId.contains("KRB")) {
            return "KRB";
        } else if (stopId.contains("OTH")) {
            return "OTH";
        } else if (stopId.contains("UTOWN")) {
            return "UTOWN";
        }
        return stopId;
    }


    /**
     * Method to retrieve local JSON file
     *
     * @param context
     * @param fileName
     * @return json - Retrieved file in JSON format
     */
    public static String loadJSONFromAsset(Context context,  String fileName) {
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

    /**
     * Displays a failed to connect snackbar to inform the user that the app
     * was not able to connect to NUS servers.
     *
     * @param activity
     */
    public static void showFailedToLoadSnackbar(Activity activity) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                R.string.failed_to_connect,
                Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(R.id.nav_view);
        snackbar.show();
    }

    public static String splitPubFromServiceNum(String s) {

        if (s.contains("PUB")) {
            String[] splitString = s.split(":");
            return splitString[1];
        }
        return s;

    }




}
