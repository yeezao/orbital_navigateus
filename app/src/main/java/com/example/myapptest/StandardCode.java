package com.example.myapptest;

import android.content.Context;
import android.view.View;

import com.example.myapptest.data.busstopinformation.StopList;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StandardCode {

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
        if (stopId.contains("PGPE")) {
            return "PGPT";
        } else if (stopId.contains("KTR") || stopId.equals("KR-BTE")) {
            return "KR-BT";
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



}
