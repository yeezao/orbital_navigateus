package com.example.myapptest.data.busstopinformation;

public class StopList {

    private String stopName;
    private String stopLongName;
    private String stopDescription;
    private String stopId;
    private Double stopLatitude;
    private Double stopLongitude;
    private String stopLinkage;
    private Float distanceFromUser;

    public StopList() {
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopLongName() {
        return stopLongName;
    }

    public void setStopLongName(String stopLongName) {
        this.stopLongName = stopLongName;
    }

    public String getStopDescription() {
        return stopDescription;
    }

    public void setStopDescription(String stopDescription) {
        this.stopDescription = stopDescription;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public Double getStopLatitude() {
        return stopLatitude;
    }

    public void setStopLatitude(Double stopLatitude) {
        this.stopLatitude = stopLatitude;
    }

    public Double getStopLongitude() {
        return stopLongitude;
    }

    public void setStopLongitude(Double stopLongitude) {
        this.stopLongitude = stopLongitude;
    }

    public String getStopLinkage() {
        return stopLinkage;
    }

    public void setStopLinkage(String stopLinkage) {
        this.stopLinkage = stopLinkage;
    }

    public Float getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Float distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }


}
