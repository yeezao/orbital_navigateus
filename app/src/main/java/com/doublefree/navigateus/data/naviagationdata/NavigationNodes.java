package com.doublefree.navigateus.data.naviagationdata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class NavigationNodes implements Parcelable {

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

    public NavigationNodes() {

    }

    protected NavigationNodes(Parcel in) {
        name = in.readString();
        altname = in.readString();
        id = in.readString();
        numid = in.readInt();
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }
        if (in.readByte() == 0) {
            lon = null;
        } else {
            lon = in.readDouble();
        }
        discovered = in.readByte() != 0;
        prevNode = in.readParcelable(NavigationNodes.class.getClassLoader());
        weightTillNow = in.readInt();
        navEdgesFromThisNode = in.createTypedArrayList(NavigationEdges.CREATOR);
        edgeSelected = in.readParcelable(NavigationEdges.class.getClassLoader());
    }

    public static final Creator<NavigationNodes> CREATOR = new Creator<NavigationNodes>() {
        @Override
        public NavigationNodes createFromParcel(Parcel in) {
            return new NavigationNodes(in);
        }

        @Override
        public NavigationNodes[] newArray(int size) {
            return new NavigationNodes[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(altname);
        dest.writeString(id);
        dest.writeInt(numid);
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }
        if (lon == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lon);
        }
        dest.writeByte((byte) (discovered ? 1 : 0));
        dest.writeParcelable(prevNode, flags);
        dest.writeInt(weightTillNow);
        dest.writeTypedList(navEdgesFromThisNode);
        dest.writeParcelable(edgeSelected, flags);
    }
}
