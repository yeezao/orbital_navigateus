package com.example.myapptest.data.naviagationdata;

public class NavigationSearchInfo {

    private String origin;
    private String dest;
    private boolean sheltered;
    private boolean barrierFree;
    private boolean internalBusOnly;

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

}
