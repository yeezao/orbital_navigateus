package com.example.myapptest.data.busstopinformation;

import android.app.Service;
import android.util.Log;

import java.util.List;

public class ArrivalNotifications {

    public ArrivalNotifications() {
    }

    private boolean isFavourite;
    private List<String> servicesFavourited;
    private boolean watchingForArrival;
    private List<String> servicesBeingWatched;
    private String stopName;
    private String stopId;
    private List<ServiceInStopDetails> servicesAtStop;
    private int timeToWatch;

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public List<String> getServicesFavourited() {
        return servicesFavourited;
    }

    public void setServicesFavourited(List<String> servicesFavourited) {
        this.servicesFavourited = servicesFavourited;
    }

    public boolean isWatchingForArrival() {
        return watchingForArrival;
    }

    public void setWatchingForArrival(boolean watchingForArrival) {
        this.watchingForArrival = watchingForArrival;
    }

    public List<String> getServicesBeingWatched() {
        return servicesBeingWatched;
    }

    public void setServicesBeingWatched(List<String> servicesBeingWatched) {
        this.servicesBeingWatched = servicesBeingWatched;
    }

    public List<ServiceInStopDetails> getServicesAtStop() {
        return servicesAtStop;
    }

    public void setServicesAtStop(List<ServiceInStopDetails> servicesAtStop) {
        this.servicesAtStop = servicesAtStop;
    }


    public int getTimeToWatch() {
        return timeToWatch;
    }

    public void setTimeToWatch(int timeToWatch) {
        this.timeToWatch = timeToWatch;
    }
}
