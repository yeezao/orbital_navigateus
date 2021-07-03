package com.example.myapptest.data.naviagationdata;

import java.util.List;

public class NavigationNodes {

    private String name;
    private String altname;
    private String id;
    private int numid;
    private Double lat;
    private Double lon;
    private boolean discovered = false;
    private NavigationNodes prevNode;
    private int weightTillNow = 999;
    private List<NavigationEdges> navEdgesFromThisNode;
    private NavigationEdges edgeSelected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAltname() {
        return altname;
    }

    public void setAltname(String altname) {
        this.altname = altname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumid() {
        return numid;
    }

    public void setNumid(int numid) {
        this.numid = numid;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public NavigationNodes getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(NavigationNodes prevNode) {
        this.prevNode = prevNode;
    }

    public int getWeightTillNow() {
        return weightTillNow;
    }

    public void setWeightTillNow(int weightTillNow) {
        this.weightTillNow = weightTillNow;
    }

    public List<NavigationEdges> getNavEdgesFromThisNode() {
        return navEdgesFromThisNode;
    }

    public void setNavEdgesFromThisNode(List<NavigationEdges> navEdgesFromThisNode) {
        this.navEdgesFromThisNode = navEdgesFromThisNode;
    }

    public NavigationEdges getEdgeSelected() {
        return edgeSelected;
    }

    public void setEdgeSelected(NavigationEdges edgeSelected) {
        this.edgeSelected = edgeSelected;
    }

    public void removePreviousNode() {
        this.prevNode = null;
    }


}
