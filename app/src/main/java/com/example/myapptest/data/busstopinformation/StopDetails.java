package com.example.myapptest.data.busstopinformation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StopDetails {

    @JsonProperty("caption")
    private String caption;
    @JsonProperty("latitude")
    private String latitude;
    @JsonProperty("longitude")
    private String longitude;
    @JsonProperty("name")
    private String name;
    @JsonProperty("LongName")
    private String longName;
    @JsonProperty("ShortName")
    private String shortName;

    public StopDetails() {

    }

    public String getCaption() {
        return caption;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }



}
