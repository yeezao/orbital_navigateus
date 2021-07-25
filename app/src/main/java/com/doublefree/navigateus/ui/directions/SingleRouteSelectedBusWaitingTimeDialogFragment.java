package com.doublefree.navigateus.ui.directions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.StandardCode;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopArrivalInfoForDirections;
import com.doublefree.navigateus.data.naviagationdata.NavigationNodes;
import com.doublefree.navigateus.data.naviagationdata.NavigationPartialResults;
import com.jayway.jsonpath.JsonPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleRouteSelectedBusWaitingTimeDialogFragment extends DialogFragment {

    public static String TAG = "SingleRouteSelectedBusWaitingTimeDialogFragment";

    NavigationNodes stop;
    NavigationPartialResults currentSegment;
    Context context;
    boolean isViableService1;
    List<StopArrivalInfoForDirections> stopArrivalInfoForDirections = new ArrayList<>();
    int timeTillNow;
    View view;

    LinearLayoutManager linearLayoutManager;
    RecyclerView singleResultRecyclerView;

    ProgressBar loadingBar;
    Handler handler = new Handler();


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_see_bus_arrival_for_single_route, null);

        builder.setView(view)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.removeCallbacksAndMessages(null);
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext());
        singleResultRecyclerView = view.findViewById(R.id.announcementRecyclerView);
        singleResultRecyclerView.setClickable(false);
        singleResultRecyclerView.setLayoutManager(linearLayoutManager);

        loadingBar = view.findViewById(R.id.loadingBarForSingleStop);

        checkWhichViableBuses();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("single stop selected waiting", stop.getId());
                NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), stop.getId(), 0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                    @Override
                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                        extractAndSortResponse(servicesAllInfoAtStop);
                    }

                    @Override
                    public void onFailureSingleStop() {

                    }
                });
            }
        }, 0);



        TextView title = view.findViewById(R.id.ServiceTimetableTitle);
        title.setText(stop.getName());

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        super.onCancel(dialog);
    }

    private void extractAndSortResponse(List<ServiceInStopDetails> busStopArrivalInfo) {
        List<ServiceInStopDetails> listOfViableServicesToDisplay = new ArrayList<>();
        for (ServiceInStopDetails temp: busStopArrivalInfo) {
            for (String service: (isViableService1 ? currentSegment.getViableBuses1() : currentSegment.getViableBuses2())) {
                if (temp.getServiceNum().equals(service) || ((temp.getServiceNum().contains("Utown") || temp.getServiceNum().contains("UTown")) //for COM2 bus stop - D1 twd UTown
                        && service.equals("D1") && currentSegment.getNodesTraversed().get(1).getId().equals("LT13-OPP"))
                        || (temp.getServiceNum().equals("D1(To BIZ2)") //for COM2 bus stop - D1 twd BIZ2
                        && service.equals("D1")
                        && currentSegment.getNodesTraversed().get(1).getId().equals("BIZ2"))) {
                    listOfViableServicesToDisplay.add(temp);
                }
            }
        }
        stopArrivalInfoForDirections.clear();
        for (ServiceInStopDetails temp: listOfViableServicesToDisplay) {
            Log.e("timetillnow", timeTillNow + "");
            if (!temp.getFirstArrival().contains("-")) {
                stopArrivalInfoForDirections.add(setInstanceOfStopArrivalInfo(temp, true));
            }
            if (!temp.getSecondArrival().contains("-")) {
                stopArrivalInfoForDirections.add(setInstanceOfStopArrivalInfo(temp, false));
            }
            stopArrivalInfoForDirections.sort(new Comparator<StopArrivalInfoForDirections>() {
                @Override
                public int compare(StopArrivalInfoForDirections o1, StopArrivalInfoForDirections o2) {
                    return o1.getArrivalTime() - o2.getArrivalTime();
                }
            });
            startRecyclerView();

        }

    }

    private StopArrivalInfoForDirections setInstanceOfStopArrivalInfo(ServiceInStopDetails temp, boolean isFirstArrival) {
        StopArrivalInfoForDirections instanceOfStopArrivalInfo = new StopArrivalInfoForDirections();
        String arrivalTime;
        boolean isLive;
        if (isFirstArrival) {
            arrivalTime = temp.getFirstArrival();
            isLive = !(temp.getFirstArrivalLive().isEmpty());
        } else {
            arrivalTime = temp.getSecondArrival();
            isLive = !(temp.getSecondArrivalLive().isEmpty());
        }
        if (temp.getServiceNum().contains("D1")) {
            if (temp.getServiceNum().contains("UTown")) {
                instanceOfStopArrivalInfo.setService("D1");
                instanceOfStopArrivalInfo.setServiceDesc("to UTown");
            } else if (temp.getServiceNum().contains("BIZ")) {
                instanceOfStopArrivalInfo.setService("D1");
                instanceOfStopArrivalInfo.setServiceDesc("to BIZ2");
            } else {
                instanceOfStopArrivalInfo.setService(temp.getServiceNum());
//                instanceOfStopArrivalInfo.setServiceDesc(currentSegment.getNodeSequence().get(currentSegment.getNodeSequence().size() - 1).getAltname());
                instanceOfStopArrivalInfo.setServiceDesc("to " + currentSegment.getNodeSequence().get(currentSegment.getNodeSequence().size() - 1).getAltname());
            }
        } else {
            instanceOfStopArrivalInfo.setService(temp.getServiceNum());
//                instanceOfStopArrivalInfo.setServiceDesc(currentSegment.getNodeSequence().get(currentSegment.getNodeSequence().size() - 1).getAltname());
            instanceOfStopArrivalInfo.setServiceDesc("to " + currentSegment.getNodeSequence().get(currentSegment.getNodeSequence().size() - 1).getAltname());
        }
        instanceOfStopArrivalInfo.setArrivalTime(arrivalTime);
        instanceOfStopArrivalInfo.setLive(isLive);
        instanceOfStopArrivalInfo.setCanCatch(instanceOfStopArrivalInfo.getArrivalTime() >= timeTillNow);

        return instanceOfStopArrivalInfo;
    }

    private void startRecyclerView() {
        SingleStopBusArrivalCustomAdapterRecyclerView singleStopBusArrivalCustomAdapterRecyclerView =
                new SingleStopBusArrivalCustomAdapterRecyclerView(getActivity(), getContext(), stop, stopArrivalInfoForDirections);
        singleResultRecyclerView.setAdapter(singleStopBusArrivalCustomAdapterRecyclerView);
        loadingBar.setVisibility(View.GONE);
    }

    private void checkWhichViableBuses() {
        if (currentSegment.getViableBuses1().size() > 0) {
            isViableService1 = true;
        } else if (currentSegment.getViableBuses2().size() > 0) {
            isViableService1 = false;
        }
    }

    public static SingleRouteSelectedBusWaitingTimeDialogFragment newInstance(NavigationNodes stopNode, NavigationPartialResults currentSegment, Context context, int timeTillNow) {
        SingleRouteSelectedBusWaitingTimeDialogFragment dialogFragment = new SingleRouteSelectedBusWaitingTimeDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        dialogFragment.setStop(stopNode);
        dialogFragment.setCurrentSegment(currentSegment);
        dialogFragment.setContext(context);
        dialogFragment.setTimeTillNow(timeTillNow);
        return dialogFragment;
    }

    public void setStop(NavigationNodes stop) {
        this.stop = stop;
    }

    public void setCurrentSegment(NavigationPartialResults currentSegment) {
        this.currentSegment = currentSegment;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTimeTillNow(int timeTillNow) {
        this.timeTillNow = timeTillNow;
    }


    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo);
    }



}
