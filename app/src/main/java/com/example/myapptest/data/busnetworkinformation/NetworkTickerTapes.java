package com.example.myapptest.data.busnetworkinformation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class NetworkTickerTapes {

    private String message;
    private LocalDateTime displayFrom;
    private LocalDateTime displayTo;
    private String servicesAffected;

    public void mainSetterNetworkTickerTapes(String message, String servicesAffected, String displayFromString, String displayToString) {
        this.message = message;
        this.servicesAffected = servicesAffected;
        this.displayFrom = LocalDateTime.parse(displayFromString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.displayTo = LocalDateTime.parse(displayToString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
