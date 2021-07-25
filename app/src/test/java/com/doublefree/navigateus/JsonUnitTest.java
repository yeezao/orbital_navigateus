package com.doublefree.navigateus;

import com.doublefree.navigateus.data.busstopinformation.StopList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class JsonUnitTest {
    List<StopList> listOfAllStops = new ArrayList<>();
    StopList listOfStops;
    List<String> listOfNames = new ArrayList<>();
    List<String> listOfIds = new ArrayList<>();
    List<Double> listOfLat = new ArrayList<>();
    List<Double> listOfLong = new ArrayList<>();

    @Test
    public void JsonFileLogicTest(){

        listOfNames.add(0, "COM 2");
        listOfIds.add(0, "A1");
        listOfLong.add(0,103.77369);
        listOfLat.add(0,1.29483);

        for (int i = 0; i < listOfNames.size(); i++) {
            listOfStops = new StopList();
            listOfStops.setStopName(listOfNames.get(i));
            listOfStops.setStopId(listOfIds.get(i));
            listOfStops.setStopLongitude(listOfLong.get(i));
            listOfStops.setStopLatitude(listOfLat.get(i));
            listOfAllStops.add(listOfStops);
        }
        assertEquals("COM 2",listOfAllStops.get(0).getStopName());
        assertEquals("A1",listOfAllStops.get(0).getStopId());
        assertEquals("1.29483", listOfAllStops.get(0).getStopLatitude().toString());
        assertEquals("103.77369", listOfAllStops.get(0).getStopLongitude().toString());
    }
}
