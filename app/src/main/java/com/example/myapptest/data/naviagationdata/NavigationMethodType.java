package com.example.myapptest.data.naviagationdata;

public class NavigationMethodType {

    private boolean isWalking;
    private NavigationNodes origin;
    private NavigationNodes dest;


    public boolean isWalking() {
        return isWalking;
    }

    public void setWalking(boolean walking) {
        isWalking = walking;
    }

    public NavigationNodes getOrigin() {
        return origin;
    }

    public void setOrigin(NavigationNodes origin) {
        this.origin = origin;
    }

    public NavigationNodes getDest() {
        return dest;
    }

    public void setDest(NavigationNodes dest) {
        this.dest = dest;
    }
}
