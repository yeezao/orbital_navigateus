package com.example.myapptest.data.busstopinformation;

public class StopArrivalInfoForDirections {

    private String service;
    private int arrivalTime;
    private String serviceDesc;
    private boolean isLive;
    private boolean canCatch;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        if (arrivalTime.equals("Arr")) {
            this.arrivalTime = 0;
        } else {
            this.arrivalTime = Integer.parseInt(arrivalTime);
        }
    }

    public String getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public boolean isCanCatch() {
        return canCatch;
    }

    public void setCanCatch(boolean canCatch) {
        this.canCatch = canCatch;
    }


}
