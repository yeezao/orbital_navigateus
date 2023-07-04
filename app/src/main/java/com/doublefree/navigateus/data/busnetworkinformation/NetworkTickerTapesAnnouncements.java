package com.doublefree.navigateus.data.busnetworkinformation;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkTickerTapesAnnouncements {

    private String message;
    private LocalDateTime displayFrom;
    private LocalDateTime displayTo;
    private String servicesAffected;

    public NetworkTickerTapesAnnouncements() {

    }

    public void mainSetterNetworkTickerTapes(String message, String servicesAffected,
                                             String displayFromString, String displayToString) {
        this.message = message;
        this.servicesAffected = servicesAffected;
        if (displayFromString != null) {
            this.displayFrom = LocalDateTime.parse(displayFromString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        if (displayToString != null) {
            this.displayTo = LocalDateTime.parse(displayToString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDisplayFrom() {
        return displayFrom;
    }

    public LocalDateTime getDisplayTo() {
        return displayTo;
    }

    public String getServicesAffected() {
        return servicesAffected;
    }

    public boolean checkIfValid() {
        LocalDateTime now = LocalDateTime.now();
        return displayFrom.isBefore(now) && displayTo.isAfter(now);
    }
}
