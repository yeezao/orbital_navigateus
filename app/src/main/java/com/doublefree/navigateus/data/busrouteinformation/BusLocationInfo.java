package com.doublefree.navigateus.data.busrouteinformation;

import com.google.android.gms.maps.model.LatLng;

public class BusLocationInfo {

    private String servicePlate;
    private LatLng busLocation;

    public String getServicePlate() {
        return servicePlate;
    }

    public void setServicePlate(String servicePlate) {
        this.servicePlate = servicePlate;
    }

    public void setBusLocation(Double lat, Double lng) {
        this.busLocation = new LatLng(lat, lng);
    }

    public LatLng getBusLocation() {
        return busLocation;
    }


}
