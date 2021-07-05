package com.example.myapptest;

import android.view.View;

import com.example.myapptest.data.busstopinformation.StopList;
import com.jayway.jsonpath.JsonPath;

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

    public static String StopIdExceptionsWithReturn(String stopId) {
        if (stopId.contains("PGPE")) {
            return "PGPT";
        } else if (stopId.contains("KTR") || stopId.equals("KR-BTE")) {
            return "KR-BT";
        }
        return stopId;
    }

}
