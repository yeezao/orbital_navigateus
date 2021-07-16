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
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopArrivalInfoForDirections;
import com.doublefree.navigateus.data.naviagationdata.NavigationNodes;
import com.doublefree.navigateus.data.naviagationdata.NavigationPartialResults;
import com.jayway.jsonpath.JsonPath;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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
                getBusArrivalInfo(stop.getId(), new VolleyCallBack() {
                    @Override
                    public void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo) {
                        extractAndSortResponse(busStopArrivalInfo);
                    }
                });
                handler.postDelayed(this, 10000);
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
        for (ServiceInStopDetails temp: busStopArrivalInfo){
            for (String service: (isViableService1 ? currentSegment.getViableBuses1() : currentSegment.getViableBuses2())) {
                if (temp.getServiceNum().equals(service) || (temp.getServiceNum().equals("D1(To UTown)") //for COM2 bus stop - D1 twd UTown
                        && service.equals("D1") && currentSegment.getNodesTraversed().get(1).getId().equals("LT13-OPP"))
                        || (temp.getServiceNum().equals("D1(To BIZ2)") //for COM2 bus stop - D1 twd BIZ2
                        && service.equals("D1")
                        && currentSegment.getNodesTraversed().get(1).getId().equals("BIZ2"))) {
                    listOfViableServicesToDisplay.add(temp);
                }
            }
        }
        int earliestArrivalTime = 1000;
        ServiceInStopDetails earliestArrival;
        boolean isFirstArrivalEarliest = true;
        stopArrivalInfoForDirections.clear();
        for (ServiceInStopDetails temp: listOfViableServicesToDisplay) {
            Log.e("timetillnow", timeTillNow + "");
            if (!temp.getFirstArrival().contains("-")) {
                stopArrivalInfoForDirections.add(setInstanceOfStopArrivalInfo(temp, true));
            }
            if (!temp.getSecondArrival().contains("-")) {
                stopArrivalInfoForDirections.add(setInstanceOfStopArrivalInfo(temp, false));
            }
            //TODO: sort the stopArrivalInfoForDirections
            for (int i = 0; i < stopArrivalInfoForDirections.size(); i++) {
                int smallestIndex = i;
                for (int j = i + 1; j < stopArrivalInfoForDirections.size(); j++) {
                    if (stopArrivalInfoForDirections.get(j).getArrivalTime() < stopArrivalInfoForDirections.get(i).getArrivalTime()) {
                        smallestIndex = j;
                    }
                }
                if (smallestIndex != i) {
                    Collections.swap(stopArrivalInfoForDirections, i, smallestIndex);
                }
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecyclerView();
                }
            }, 800);

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

    private void getBusArrivalInfo(String stopId, final VolleyCallBack callback) {

        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + stopId;

        StringRequest stopStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //variables for service info at a particular stop
                ServiceInStopDetails serviceInfoAtStop;
                List<ServiceInStopDetails> servicesAllInfoAtStop;
                List<String> servicesAtStop;
                List<String> serviceFirstArrival;
                List<String> serviceSecondArrival;
                List<String> firstArrivalLive;
                List<String> secondArrivalLive;
                servicesAllInfoAtStop = new ArrayList<>();
                Log.e("GetStopInfo response is", response);
                servicesAtStop = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].name");
                serviceFirstArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime");
                serviceSecondArrival = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime");
                firstArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].arrivalTime_veh_plate");
                secondArrivalLive = JsonPath.read(response, "$.ShuttleServiceResult.shuttles[*].nextArrivalTime_veh_plate");
                Log.e("servicesAtStop is: ", servicesAtStop.get(0));
                for (int i = 0; i < servicesAtStop.size(); i++) {
                    serviceInfoAtStop = new ServiceInStopDetails();
                    serviceInfoAtStop.setServiceNum(servicesAtStop.get(i));
                    serviceInfoAtStop.setFirstArrival(serviceFirstArrival.get(i));
                    Log.e("first arrival is: ", "" + serviceFirstArrival.get(i));
                    serviceInfoAtStop.setSecondArrival(serviceSecondArrival.get(i));
                    serviceInfoAtStop.setFirstArrivalLive(firstArrivalLive.get(i));
                    serviceInfoAtStop.setSecondArrivalLive(secondArrivalLive.get(i));
                    servicesAllInfoAtStop.add(serviceInfoAtStop);
                }
//                Log.e("servicesAllInfoAtStop is: ", "" + servicesAllInfoAtStop);
//                Log.e("value of j is: ", "" + groupPosition);

                callback.onSuccess(servicesAllInfoAtStop);


            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("volley API error", "" + error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", context.getString(R.string.auth_header));
                return params;
            }
        };

        if (context != null) {
            RequestQueue stopRequestQueue = Volley.newRequestQueue(context);
            stopRequestQueue.add(stopStringRequest);
        }
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
