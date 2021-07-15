package com.doublefree.navigateus.data.busrouteinformation;

public class ServiceInfo {

    private String serviceNum;
    private String serviceDesc;
    private int serviceStatus = 0;
    private String serviceFullRoute = "";
    private String routeMinMax = "";

    public String getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(String serviceNum) {
        this.serviceNum = serviceNum;
    }

    public String getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    public int getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(int serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getServiceFullRoute() {
        return serviceFullRoute;
    }

    public void setServiceFullRoute(String serviceFullRoute) {
        this.serviceFullRoute = serviceFullRoute;
    }

    public String getRouteMinMax() {
        return routeMinMax;
    }

    public void setRouteMinMax(String routeMinMax) {
        this.routeMinMax = routeMinMax;
    }






}
