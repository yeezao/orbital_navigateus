package com.example.myapptest.data.naviagationdata;

import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.ui.stops_services.StopsServicesFragment;

import java.util.List;

public class NavigationSearchInfo {

    private String origin;
    private String dest;
    private boolean sheltered;
    private boolean barrierFree;
    private boolean internalBusOnly;
    private StopList originDetails;
    private StopList destDetails;
    private List<ServiceInStopDetails> originServiceDetails;
    private List<ServiceInStopDetails> destServiceDetails;


    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public boolean isSheltered() {
        return sheltered;
    }

    public void setSheltered(boolean sheltered) {
        this.sheltered = sheltered;
    }

    public boolean isBarrierFree() {
        return barrierFree;
    }

    public void setBarrierFree(boolean barrierFree) {
        this.barrierFree = barrierFree;
    }

    public boolean isInternalBusOnly() {
        return internalBusOnly;
    }

    public void setInternalBusOnly(boolean internalBusOnly) {
        this.internalBusOnly = internalBusOnly;
    }

    public StopList getOriginDetails() {
        return originDetails;
    }

    public void setOriginDetails(StopList originDetails) {
        this.originDetails = originDetails;
    }

    public StopList getDestDetails() {
        return destDetails;
    }

    public void setDestDetails(StopList destDetails) {
        this.destDetails = destDetails;
    }


    public List<ServiceInStopDetails> getOriginServiceDetails() {
        return originServiceDetails;
    }

    public void setOriginServiceDetails(List<ServiceInStopDetails> originServiceDetails) {
        this.originServiceDetails = originServiceDetails;
    }

    public List<ServiceInStopDetails> getDestServiceDetails() {
        return destServiceDetails;
    }

    public void setDestServiceDetails(List<ServiceInStopDetails> destServiceDetails) {
        this.destServiceDetails = destServiceDetails;
    }


}
