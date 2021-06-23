package com.example.myapptest.ui.directions;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.naviagationdata.NavigationNodes;
import com.example.myapptest.data.naviagationdata.NavigationPartialResults;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomAdapterRecyclerView extends RecyclerView.Adapter<CustomAdapterRecyclerView.MyViewHolder> {

    ArrayList<String> busStop;
    ArrayList<String> routeId;
    ArrayList<String> busStopCode;
    Context context;

    List<NavigationResults> resultsList;

    NavigationResults navResultTest;
    List<NavigationPartialResults> navTestResultSegment;

    public CustomAdapterRecyclerView(Context context, List<NavigationResults> resultsList) {
        this.context = context;
        this.resultsList = resultsList;
        navResultTest = resultsList.get(0);
        navTestResultSegment = navResultTest.getResultsConcatenated();
        Log.e("check", navTestResultSegment.toString());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayoutnavresults, parent, false);
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items

        holder.navR_totaltime.setText(navResultTest.getTotalTimeTaken() + " min");

        Log.e("entered", "onbindviewholder");

        holder.navR_firstArrow.setVisibility(View.VISIBLE);
        holder.recyclerviewItemHolder.setVisibility(View.GONE);

        boolean onlyWalk = true;

        for (int i = 0; i < navTestResultSegment.size(); i++) {
            if (navTestResultSegment.get(i) != null) {
                NavigationPartialResults currentSegment = navTestResultSegment.get(i);
                Log.e("current node starts at", currentSegment.getNodesTraversed().get(0).getName());
                switch (i) {
                    case 0:
                        if (currentSegment.getViableBuses1().size() > 0) {
                            onlyWalk = false;
                            holder.navRLayoutContainer2.setVisibility(View.VISIBLE);
                            holder.navR_firstArrow.setVisibility(View.GONE);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int j = 0; j < currentSegment.getViableBuses1().size(); j++) {
                                stringBuilder.append(currentSegment.getViableBuses1().get(j));
                                if (j < currentSegment.getViableBuses1().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                            holder.navR_firstbusservices.setText(stringBuilder.toString());
                            getBusArrivalInfo(currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1), new VolleyCallBack() {
                                @Override
                                public void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo) {
                                    int arrivaltime = 9999;
                                    String service = "";
                                    for (ServiceInStopDetails temp: busStopArrivalInfo) {
                                        for (int i = 0; i < currentSegment.getViableBuses1().size(); i++) {
                                            if (temp.getFirstArrival().charAt(0) != '-'
                                                    && temp.getServiceNum().equals(currentSegment.getViableBuses1().get(i))
                                                    && (temp.getFirstArrival().equals("Arr") || Integer.parseInt(temp.getFirstArrival()) < arrivaltime)) {
                                                arrivaltime = Integer.parseInt(temp.getFirstArrival());
                                                service = temp.getServiceNum();
                                            }
                                        }

                                    }

                                    StringBuilder anotherStringBuilder = new StringBuilder();
                                    if (arrivaltime == 9999) {
                                        arrivaltime = 0;
                                        anotherStringBuilder.append("No bus services are operating from ")
                                                .append(currentSegment.getNodesTraversed().get(0).getName()).append(" now");
                                    } else {
                                        anotherStringBuilder.append(service).append(" will arrive at ")
                                                .append(currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1).getName()).append(" in ")
                                                .append(arrivaltime).append(" min");
                                    }
                                    holder.navR_busArrivalTimingInfo.setText(anotherStringBuilder.toString());
                                    holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
                                    holder.navR_busArrivalTimingInfo.setVisibility(View.VISIBLE);


                                }
                            });
                        } else {
                            holder.navRLayoutContainer1.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(currentSegment.getTimeForSegment());
                            holder.navR_firstwalktime.setText(stringBuilder.toString());
                            Log.e("check time just before set 0", currentSegment.getTimeForSegment() + "");

                        }
                        break;
                    case 1:
                        if (holder.navRLayoutContainer1.getVisibility() == View.GONE
                                && currentSegment.getTransferStop() == null) {
                            holder.navRLayoutContainer4.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(currentSegment.getTimeForSegment());
                            holder.navR_firstwalktime.setText(stringBuilder.toString());
                        } else if (holder.navRLayoutContainer1.getVisibility() == View.VISIBLE) {
                            onlyWalk = false;
                            holder.navRLayoutContainer2.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int j = 0; j < currentSegment.getViableBuses1().size(); j++) {
                                stringBuilder.append(currentSegment.getViableBuses1().get(j));
                                if (j < currentSegment.getViableBuses1().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                            holder.navR_firstbusservices.setText(stringBuilder.toString());
                            getBusArrivalInfo(currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1), new VolleyCallBack() {
                                @Override
                                public void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo) {
                                    int arrivaltime = 9999;
                                    String service = "";
                                    for (ServiceInStopDetails temp: busStopArrivalInfo) {
                                        for (int i = 0; i < currentSegment.getViableBuses1().size(); i++) {
                                            if (temp.getFirstArrival().charAt(0) != '-'
                                                    && temp.getServiceNum().equals(currentSegment.getViableBuses1().get(i))
                                                    && (temp.getFirstArrival().equals("Arr") || Integer.parseInt(temp.getFirstArrival()) < arrivaltime)) {
                                                arrivaltime = Integer.parseInt(temp.getFirstArrival());
                                                service = temp.getServiceNum();
                                            }
                                        }

                                    }

                                    StringBuilder anotherStringBuilder = new StringBuilder();
                                    if (arrivaltime == 9999) {
                                        arrivaltime = 0;
                                        anotherStringBuilder.append("No bus services are operating from ")
                                                .append(currentSegment.getNodesTraversed().get(0).getName()).append(" now");
                                    } else {
                                        anotherStringBuilder.append(service).append(" will arrive at ")
                                                .append(currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1).getName()).append(" in ")
                                                .append(arrivaltime).append(" min");
                                    }
                                    holder.navR_busArrivalTimingInfo.setText(anotherStringBuilder.toString());
                                    holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
                                    holder.navR_busArrivalTimingInfo.setVisibility(View.VISIBLE);

                                }
                            });
                        } else {
                            holder.navRLayoutContainer3.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int j = 0; j < currentSegment.getViableBuses2().size(); j++) {
                                stringBuilder.append(currentSegment.getViableBuses2().get(j));
                                if (j < currentSegment.getViableBuses2().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                            holder.navR_secondbusservices.setText(stringBuilder.toString());
                        }
                        break;
                    case 2:
                        if (currentSegment.getTransferStop() != null) {
                            onlyWalk = false;
                            holder.navRLayoutContainer3.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int j = 0; j < currentSegment.getViableBuses2().size(); j++) {
                                stringBuilder.append(currentSegment.getViableBuses2().get(j));
                                if (j < currentSegment.getViableBuses2().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                            holder.navR_firstbusservices.setText(stringBuilder.toString());
                        } else {
                            holder.navRLayoutContainer4.setVisibility(View.VISIBLE);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(currentSegment.getTimeForSegment());
                            holder.navR_lastwalktime.setText(stringBuilder.toString());
                            Log.e("check time just before set 2", currentSegment.getTimeForSegment() + "");
                        }
                        break;
                    case 3:
                        holder.navRLayoutContainer4.setVisibility(View.VISIBLE);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(currentSegment.getTimeForSegment());
                        holder.navR_firstwalktime.setText(stringBuilder.toString());
                        break;
                }
            }

            if (onlyWalk) {
                holder.navR_busArrivalTimingInfo.setVisibility(View.GONE);
                holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
            }

        }

//        holder.stop.setText(busStop.get(position));
//        holder.route.setText(routeId.get(position));
//        holder.code.setText(busStopCode.get(position));
        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // display a toast with person name on item click
                Toast.makeText(context, busStop.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout navRLayoutContainer1, navRLayoutContainer2, navRLayoutContainer3, navRLayoutContainer4;
        TextView navR_firstwalktime, navR_firstbusservices, navR_secondbusservices,
                navR_lastwalktime, navR_totaltime, navR_firstArrow, navR_busArrivalTimingInfo;
        LinearLayout recyclerviewItemHolder;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            navRLayoutContainer1 = itemView.findViewById(R.id.navRLayoutContainer_1);
            navRLayoutContainer2 = itemView.findViewById(R.id.navRLayoutContainer_2);
            navRLayoutContainer3 = itemView.findViewById(R.id.navRLayoutContainer_3);
            navRLayoutContainer4 = itemView.findViewById(R.id.navRLayoutContainer_4);
            navR_firstwalktime = itemView.findViewById(R.id.navR_firstwalktime);
            navR_firstbusservices = itemView.findViewById(R.id.navR_firstbusServices);
            navR_secondbusservices = itemView.findViewById(R.id.navR_secondbusServices);
            navR_lastwalktime = itemView.findViewById(R.id.navR_lastwalktiming);
            navR_totaltime = itemView.findViewById(R.id.navR_totaltime);
            navR_firstArrow = itemView.findViewById(R.id.navR_nextDirection_1);
            navR_busArrivalTimingInfo = itemView.findViewById(R.id.busArrivalTimingInfo);
            recyclerviewItemHolder = itemView.findViewById(R.id.recyclerviewItemHolder);

//            stop = (TextView) itemView.findViewById(R.id.name);
//            route = (TextView) itemView.findViewById(R.id.email);
//            code = (TextView) itemView.findViewById(R.id.mobileNo);

        }
    }



    private void getBusArrivalInfo(NavigationNodes busNodeToCheck, final VolleyCallBack callback) {

        String url = "https://nnextbus.nus.edu.sg/ShuttleService?busstopname=" + busNodeToCheck.getId();

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

    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo);
    }

}