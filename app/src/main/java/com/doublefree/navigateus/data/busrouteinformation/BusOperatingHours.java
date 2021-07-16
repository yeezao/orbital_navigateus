package com.doublefree.navigateus.data.busrouteinformation;

public class BusOperatingHours {

    private String scheduleType = "";
    private String dayType = "";
    private String firstTime = "";
    private String lastTime = "";

    public BusOperatingHours(String scheduleType, String dayType, String firstTime, String lastTime) {
        this.scheduleType = scheduleType;
        this.dayType = dayType;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getDayType() {
        return dayType;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public String getLastTime() {
        return lastTime;
    }




}
