package com.doublefree.navigateus.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.doublefree.navigateus.MainActivity;
import com.doublefree.navigateus.data.busstopinformation.ArrivalNotifications;

import java.util.List;

public class NotificationBroadcast extends BroadcastReceiver {

    MainActivity mainActivity;
    NotificationManager notificationManager;
    List<ArrivalNotifications> arrivalNotificationsArray;

    public NotificationBroadcast(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = mainActivity.getNotificationManager();

        Log.e("broadcast", notificationManager + "");
        notificationManager.cancel(intent.getIdentifier(), 0);
        for (int i = 0; i < arrivalNotificationsArray.size(); i++) {
            if (arrivalNotificationsArray.get(i).getStopId().equals(intent.getIdentifier())) {
                ArrivalNotifications temp = arrivalNotificationsArray.get(i);
                temp.setWatchingForArrival(false);
                arrivalNotificationsArray.set(i, temp);
            }
        }


//        Log.e("broadcast in class", "received");
//        NotificationBroadcastInterface listener = (NotificationBroadcastInterface) context;
//        listener.cancelMonitoring(intent);
    }

    public void mainSetter(MainActivity mainActivity, NotificationManager notificationManager, List<ArrivalNotifications> arrivalNotificationsArray) {
        this.mainActivity = mainActivity;
        this.notificationManager = notificationManager;
        this.arrivalNotificationsArray = arrivalNotificationsArray;
        Log.e("check nullity", mainActivity + " " + notificationManager + " " + arrivalNotificationsArray);
    }



}
