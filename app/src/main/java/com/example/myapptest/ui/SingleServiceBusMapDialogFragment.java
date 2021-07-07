package com.example.myapptest.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.data.naviagationdata.NavigationNodes;
import com.example.myapptest.data.naviagationdata.NavigationPartialResults;
import com.example.myapptest.ui.directions.SingleRouteSelectedBusWaitingTimeDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class displays the position of buses for a specified service on a map
 */
public class SingleServiceBusMapDialogFragment extends DialogFragment {

    public static String TAG = "SingleServiceBusMapDialogFragment";

    private String stopId;
    private ServiceInStopDetails serviceDetails;

    View rootView;

    Handler timingRefreshHandler;

    public static SingleServiceBusMapDialogFragment newInstance(String stopId, String service) {
        SingleServiceBusMapDialogFragment dialogFragment = new SingleServiceBusMapDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        dialogFragment.setStopId(stopId);
        return dialogFragment;
    }


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.fragment_see_bus_arrival_for_single_route, null);

        builder.setView(rootView)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timingRefreshHandler.removeCallbacksAndMessages(null);
                    }
                });


//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getBusArrivalInfo(stop.getId(), new SingleRouteSelectedBusWaitingTimeDialogFragment.VolleyCallBack() {
//                    @Override
//                    public void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo) {
//                        extractAndSortResponse(busStopArrivalInfo);
//                    }
//                });
//                handler.postDelayed(this, 10000);
//            }
//        }, 0);


        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        timingRefreshHandler.removeCallbacksAndMessages(null);
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        timingRefreshHandler.removeCallbacksAndMessages(null);
        super.onCancel(dialog);
    }


    public void setStopId(String stopId) {
        this.stopId = stopId;
    }


}