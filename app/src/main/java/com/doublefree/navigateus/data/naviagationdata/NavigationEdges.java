package com.doublefree.navigateus.data.naviagationdata;

import java.util.List;

public class NavigationEdges {

    private String from;
    private int fromnumid;
    private String to;
    private int tonumid;
    private String by;
    private int duration;
    private boolean sheltered;
    private boolean accessible;
    private List<String> services;
    private boolean usable = true;
    private String edgeDesc;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getFromnumid() {
        return fromnumid;
    }

    public void setFromnumid(int fromnumid) {
        this.fromnumid = fromnumid;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getTonumid() {
        return tonumid;
    }

    public void setTonumid(int tonumid) {
        this.tonumid = tonumid;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean getSheltered() {
        return sheltered;
    }

    public void setSheltered(boolean sheltered) {
        this.sheltered = sheltered;
    }

    public boolean getAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public String getEdgeDesc() {
        return edgeDesc;
    }

    public void setEdgeDesc(String edgeDesc) {
        this.edgeDesc = edgeDesc;
    }


}
