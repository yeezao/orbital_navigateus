package com.example.myapptest.ui.directions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
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


public class SingleRouteListCustomAdapterRecyclerView extends RecyclerView.Adapter<SingleRouteListCustomAdapterRecyclerView.MyViewHolder> {

    Context context;
    NavigationResults singleNavResult;
    List<NavigationPartialResults> navResultInSegments;
    String origin, dest;

    public SingleRouteListCustomAdapterRecyclerView(Context context, NavigationResults singleNavResult, String origin, String dest) {
        this.context = context;
        this.singleNavResult = singleNavResult;
        this.navResultInSegments = singleNavResult.getResultsConcatenated();
        this.origin = origin;
        this.dest = dest;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayoutsinglenavresult, parent, false);
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items
        
        Log.e("entered", "onbindviewholder");

//        holder.navR_firstArrow.setVisibility(View.VISIBLE);
//        holder.recyclerviewItemHolder.setVisibility(View.GONE);

        Log.e("null?", holder + "");

        if (position > navResultInSegments.size()) {
            holder.lowestText.setVisibility(View.GONE);
            holder.mainText.setVisibility(View.GONE);
            holder.bottomText.setVisibility(View.GONE);
            holder.topText.setVisibility(View.GONE);
            holder.circle.setVisibility(View.GONE);
            holder.stickBottom.setVisibility(View.GONE);
            holder.stickTop.setVisibility(View.GONE);
            return;
        }

        if (position == navResultInSegments.size()) {
            holder.stickBottom.setVisibility(View.INVISIBLE);
            holder.mainText.setText(dest);
            holder.circle.setImageResource(R.drawable.ic_baseline_trip_origin_36_red_large);
        } else if (position == 0) {
            holder.stickTop.setVisibility(View.INVISIBLE);
            holder.mainText.setText(origin);
            holder.circle.setImageResource(R.drawable.ic_baseline_trip_origin_36_green_large);
        }
        if (position < navResultInSegments.size() && navResultInSegments.get(position) != null) {
            NavigationPartialResults currentSegment = navResultInSegments.get(position);
            Log.e("current node starts at", currentSegment.getNodesTraversed().get(0).getName());
            if (currentSegment.getViableBuses1().size() == 0
                    && currentSegment.getNodesTraversed().get(0).getName().equals(origin)) {
                //segment is firstwalk/onlywalk
                holder.topText.setVisibility(View.INVISIBLE);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Walk ").append(currentSegment.getTimeForSegment());
                if (currentSegment.getTimeForSegment() == 1) {
                    stringBuilder.append(" minute");
                } else if (currentSegment.getTimeForSegment() > 1) {
                    stringBuilder.append(" minutes");
                }
                holder.lowestText.setText(stringBuilder.toString());
                holder.walkingMan.setVisibility(View.VISIBLE);
            } else if (position > 0 && currentSegment.getViableBuses2().size() > 0) {
                //segment is 2ndbus
                holder.topText.setText("Alight at:");
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Take service ");
                for (int i = 0; i < currentSegment.getViableBuses2().size(); i++) {
                    stringBuilder.append(currentSegment.getViableBuses2().get(i));
                    if (i < currentSegment.getViableBuses2().size() - 1) {
                        stringBuilder.append("/");
                    }
                }
                stringBuilder.append(" for ").append(currentSegment.getNodesTraversed().size() - 1);
                if (currentSegment.getNodesTraversed().size() - 1 == 1) {
                    stringBuilder.append(" stop");
                } else {
                    stringBuilder.append(" stops");
                }
                holder.bottomText.setText(stringBuilder.toString());
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                holder.human.setImageResource(R.drawable.ic_baseline_swap_horiz_20);

            } else if (currentSegment.getViableBuses1().size() == 0
                    && currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1).getName().equals(dest)) {
                //segment is lastwalk
                holder.topText.setText("Alight at:");
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Walk ").append(currentSegment.getTimeForSegment());
                if (currentSegment.getTimeForSegment() == 1) {
                    stringBuilder.append(" minute");
                } else if (currentSegment.getTimeForSegment() > 1) {
                    stringBuilder.append(" minutes");
                }
                holder.lowestText.setText(stringBuilder.toString());
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                holder.walkingMan.setVisibility(View.VISIBLE);
            } else if (navResultInSegments.size() > 2 && position > 0 && position < navResultInSegments.size() - 1
                    && currentSegment.getViableBuses1().size() == 0) {
                holder.topText.setText("Alight at:");
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Walk ").append(currentSegment.getTimeForSegment());
                if (currentSegment.getTimeForSegment() == 1) {
                    stringBuilder.append(" minute");
                } else if (currentSegment.getTimeForSegment() > 1) {
                    stringBuilder.append(" minutes");
                }
                holder.lowestText.setText(stringBuilder.toString());
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                holder.walkingMan.setVisibility(View.VISIBLE);
            } else {
                //segment is 1stbus
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Take service ");
                for (int i = 0; i < currentSegment.getViableBuses1().size(); i++) {
                    stringBuilder.append(currentSegment.getViableBuses1().get(i));
                    if (i < currentSegment.getViableBuses1().size() - 1) {
                        stringBuilder.append("/");
                    }
                }
                stringBuilder.append(" for ").append(currentSegment.getNodesTraversed().size() - 1);
                if (currentSegment.getNodesTraversed().size() - 1 == 1) {
                    stringBuilder.append(" stop");
                } else {
                    stringBuilder.append(" stops");
                }
                holder.bottomText.setText(stringBuilder.toString());
                if (position > 0) {
                    holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                    holder.human.setVisibility(View.VISIBLE);
                    holder.human.setScaleX(-1);
                }

            }

        }
//
//        if (onlyWalk) {
//            holder.navR_busArrivalTimingInfo.setVisibility(View.GONE);
//            holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
//        }

//        holder.stop.setText(busStop.get(position));
//        holder.route.setText(routeId.get(position));
//        holder.code.setText(busStopCode.get(position));
        // implement setOnClickListener event on item view

    }


    @Override
    public int getItemCount() {
        Log.e("itemSize", navResultInSegments.size() + 1 + "");
        return navResultInSegments.size() + 2;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
//        ConstraintLayout navRLayoutContainer1, navRLayoutContainer2, navRLayoutContainer3, navRLayoutContainer4;
//        TextView navR_firstwalktime, navR_firstbusservices, navR_secondbusservices,
//                navR_lastwalktime, navR_totaltime, navR_firstArrow, navR_busArrivalTimingInfo;
//        LinearLayout recyclerviewItemHolder;
        TextView topText, mainText, bottomText, lowestText;
        View stickTop, stickBottom;
        ImageView circle, human, walkingMan;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            topText = itemView.findViewById(R.id.textView10);
            mainText = itemView.findViewById(R.id.textView6);
            bottomText = itemView.findViewById(R.id.textView7);
            lowestText = itemView.findViewById(R.id.textView11);
            circle = itemView.findViewById(R.id.imageView7);
            human = itemView.findViewById(R.id.imageView8);
            stickTop = itemView.findViewById(R.id.view2Top);
            stickBottom = itemView.findViewById(R.id.view2Bottom);
            walkingMan = itemView.findViewById(R.id.walkingManImageView);
//            navRLayoutContainer1 = itemView.findViewById(R.id.navRLayoutContainer_1);
//            navRLayoutContainer2 = itemView.findViewById(R.id.navRLayoutContainer_2);
//            navRLayoutContainer3 = itemView.findViewById(R.id.navRLayoutContainer_3);
//            navRLayoutContainer4 = itemView.findViewById(R.id.navRLayoutContainer_4);
//            navR_firstwalktime = itemView.findViewById(R.id.navR_firstwalktime);
//            navR_firstbusservices = itemView.findViewById(R.id.navR_firstbusServices);
//            navR_secondbusservices = itemView.findViewById(R.id.navR_secondbusServices);
//            navR_lastwalktime = itemView.findViewById(R.id.navR_lastwalktiming);
//            navR_totaltime = itemView.findViewById(R.id.navR_totaltime);
//            navR_firstArrow = itemView.findViewById(R.id.navR_nextDirection_1);
//            navR_busArrivalTimingInfo = itemView.findViewById(R.id.busArrivalTimingInfo);
//            recyclerviewItemHolder = itemView.findViewById(R.id.recyclerviewItemHolder);

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