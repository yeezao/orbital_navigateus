package com.example.myapptest.ui.directions;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
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

public class SingleRouteListCustomAdapterRecyclerView extends RecyclerView.Adapter<SingleRouteListCustomAdapterRecyclerView.MyViewHolder> {

    Context context;
    Activity activity;
    NavigationResults singleNavResult;
    List<NavigationPartialResults> navResultInSegments;
    String origin, dest;

    View view;

    float dpWidth;

    FragmentManager childFragmentManager;

    public SingleRouteListCustomAdapterRecyclerView(Activity activity, Context context, NavigationResults singleNavResult, String origin, String dest, FragmentManager childFragmentManager, float dpWidth) {
        this.context = context;
        this.activity = activity;
        this.singleNavResult = singleNavResult;
        this.navResultInSegments = singleNavResult.getResultsConcatenated();
        this.origin = origin;
        this.dest = dest;
        this.childFragmentManager = childFragmentManager;
        this.dpWidth = dpWidth;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayoutsinglenavresult, parent, false);
        view = v;
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items
        

//        holder.navR_firstArrow.setVisibility(View.VISIBLE);
//        holder.recyclerviewItemHolder.setVisibility(View.GONE);

        boolean isThisSegmentWalking;

        if (position > navResultInSegments.size()) {
            holder.mainText.setVisibility(View.GONE);
            holder.takeServiceClickable.setVisibility(View.GONE);
            holder.topText.setVisibility(View.GONE);
            holder.circle.setVisibility(View.GONE);
            holder.stickBottom.setVisibility(View.GONE);
            holder.stickTop.setVisibility(View.GONE);
            holder.elvReplacement.setVisibility(View.GONE);
            holder.takeServiceClickable.setVisibility(View.GONE);
            return;
        }

        if (position == navResultInSegments.size()) {
            holder.stickBottom.setVisibility(View.INVISIBLE);
            holder.mainText.setText(dest);
            holder.mainText.setTypeface(Typeface.DEFAULT_BOLD);
            holder.circle.setImageResource(R.drawable.ic_baseline_trip_origin_36_red_large);
            holder.elvReplacement.setVisibility(View.GONE);
            holder.takeServiceClickable.setVisibility(View.INVISIBLE);
            if (navResultInSegments.get(navResultInSegments.size() - 1).getEdgeSequence().size() == 0) {
                holder.stickTop.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
            }
        } else if (position == 0) {
            holder.stickTop.setVisibility(View.INVISIBLE);
            holder.mainText.setText(origin);
            holder.mainText.setTypeface(Typeface.DEFAULT_BOLD);
            holder.circle.setImageResource(R.drawable.ic_baseline_trip_origin_36_green_large);
        }
        if (position < navResultInSegments.size() && navResultInSegments.get(position) != null) {
            NavigationPartialResults currentSegment = navResultInSegments.get(position);
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
                holder.elvReplacementMaintext.setText(stringBuilder.toString());
                setHolderELVChildren(context, currentSegment, holder, true);
                holder.takeServiceClickable.setVisibility(View.GONE);
            } else if (position > 0 && currentSegment.getViableBuses2().size() > 0) {
                //segment is 2ndbus
                holder.topText.setText("Alight at:");
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Take service");
                for (int i = 0; i < currentSegment.getViableBuses2().size(); i++) {
                    stringBuilder.append(currentSegment.getViableBuses2().get(i));
                    if (currentSegment.getViableBuses2().get(i).contains("D1") && currentSegment.getNodeSequence().get(0).getId().equals("COM2")) {
                        if (currentSegment.getNodeSequence().get(1).getId().equals("LT13-OPP")) {
                            stringBuilder.append(" (to UTown)");
                        } else if (currentSegment.getNodeSequence().get(1).getId().equals("BIZ2")) {
                            stringBuilder.append(" (to BIZ2)");
                        }
                    }
                    if (i < currentSegment.getViableBuses2().size() - 1) {
                        stringBuilder.append(" / ");
                    }
                }
                holder.bottomText.setText(stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("Ride ").append(currentSegment.getNodeSequence().size() - 1);
                if (currentSegment.getNodeSequence().size() - 1 == 1) {
                    stringBuilder.append(" stop");
                } else {
                    stringBuilder.append(" stops");
                }
                stringBuilder.append(" (").append(currentSegment.getTimeForSegment()).append(" min)");
                holder.elvReplacementMaintext.setText(stringBuilder);
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                holder.stickBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
                holder.stickTop.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
                setHolderELVChildren(context, currentSegment, holder, false);
                holder.human.setImageResource(R.drawable.ic_baseline_swap_horiz_20);

                displayBusArrivalInfo(holder, currentSegment.getNodesTraversed().get(0), currentSegment, position);

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
                holder.elvReplacementMaintext.setText(stringBuilder.toString());
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                holder.takeServiceClickable.setVisibility(View.GONE);
                holder.stickTop.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
                setHolderELVChildren(context, currentSegment, holder, true);
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
                holder.takeServiceClickable.setVisibility(View.GONE);
                holder.stickTop.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
                holder.elvReplacementMaintext.setText(stringBuilder.toString());
                holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                holder.human.setVisibility(View.VISIBLE);
                setHolderELVChildren(context, currentSegment, holder, true);
            } else {
                //segment is 1stbus
                holder.mainText.setText(currentSegment.getNodesTraversed().get(0).getName());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Take service");
                for (int i = 0; i < currentSegment.getViableBuses1().size(); i++) {
                    stringBuilder.append(currentSegment.getViableBuses1().get(i));
                    if (currentSegment.getViableBuses1().get(i).contains("D1") && currentSegment.getNodeSequence().get(0).getId().equals("COM2")) {
                        if (currentSegment.getNodeSequence().get(1).getId().equals("LT13-OPP")) {
                            stringBuilder.append(" (to UTown)");
                        } else if (currentSegment.getNodeSequence().get(1).getId().equals("BIZ2")) {
                            stringBuilder.append(" (to BIZ2)");
                        }
                    }
                    if (i < currentSegment.getViableBuses1().size() - 1) {
                        stringBuilder.append(" / ");
                    }
                }
                holder.bottomText.setText(stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("Ride ").append(currentSegment.getNodeSequence().size() - 1);
                if (currentSegment.getNodeSequence().size() - 1 == 1) {
                    stringBuilder.append(" stop");
                } else {
                    stringBuilder.append(" stops");
                }
                stringBuilder.append(" (").append(currentSegment.getTimeForSegment()).append(" min)");
                holder.elvReplacementMaintext.setText(stringBuilder);
                holder.stickBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Orange));
                if (position > 0) {
                    holder.circle.setImageResource(R.drawable.ic_baseline_directions_bus_36_large);
                    holder.human.setVisibility(View.VISIBLE);
                    holder.human.setScaleX(-1);
                }
                setHolderELVChildren(context, currentSegment, holder, false);
                displayBusArrivalInfo(holder, currentSegment.getNodesTraversed().get(0), currentSegment, position);
            }
        }
    }

    private void displayBusArrivalInfo(MyViewHolder holder, NavigationNodes stop,
                                       NavigationPartialResults currentSegment, final int position) {
        getBusArrivalInfo(stop.getId(), new VolleyCallBack() {
            @Override
            public void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo) {
                //TODO: sort the list for the earliest arrival
                //TODO: display the earliest arrival only
            }
        });
        holder.takeServiceClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: open DialogFragment with service arrival info
                Log.e("onclick", "fired");
                SingleRouteSelectedBusWaitingTimeDialogFragment dialogFragment;
                int timeTillThisSegment = 0;
                if (position == 0 || position >= navResultInSegments.size()) {
                    timeTillThisSegment = 0;
                } else if (navResultInSegments.size() > 0) {
                    for (int i = 0; i < position; i++) {
                        timeTillThisSegment += navResultInSegments.get(i).getTimeForSegment();
                    }
                }
                dialogFragment = SingleRouteSelectedBusWaitingTimeDialogFragment.newInstance(stop, currentSegment, context, timeTillThisSegment);
                dialogFragment.show(childFragmentManager, SingleRouteSelectedBusWaitingTimeDialogFragment.TAG);
            }
        });
    }

    private void setHolderELVChildren(Context context, NavigationPartialResults currentSegment, MyViewHolder holder, boolean isWalking) {
        int stopPoint = currentSegment.getNodesTraversed().size();
        int startPoint = 0;
        if (isWalking) {
            holder.modeIcon.setImageResource(R.drawable.ic_baseline_directions_walk_16_navresult_small);
            stopPoint = currentSegment.getEdgeSequence().size();
            startPoint = 0;
        } else {
            holder.modeIcon.setImageResource(R.drawable.ic_baseline_directions_bus_16_navresult);
            stopPoint = currentSegment.getNodeSequence().size() - 1;
            startPoint = 1;
        }
        for (int i = startPoint; i < stopPoint; i++) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (!isWalking) {
                tv.setText(currentSegment.getNodeSequence().get(i).getName());
            } else {
                Log.e("edge is", currentSegment.getEdgeSequence().get(i).getEdgeDesc());
                tv.setText(currentSegment.getEdgeSequence().get(i).getEdgeDesc());
                tv.setLineSpacing(0, (float) 1.15);
            }
            if (i == 1) {
                tv.setPadding(0, 25, 0, 0);
            } else {
                tv.setPadding(0, 35, 0, 0);
            }
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            tv.setLineSpacing(0, (float) 1.1);
            holder.elvChildren.addView(tv);
        }

        holder.elvReplacement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.elvChildren.getVisibility() == View.GONE) {
                    holder.elvChildren.setVisibility(View.VISIBLE);
                    holder.expandIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_16);
                } else {
                    holder.elvChildren.setVisibility(View.GONE);
                    holder.expandIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_16);
                }
            }
        });
        if (!isWalking && currentSegment.getNodeSequence().size() <= 2) {
            holder.elvReplacement.setClickable(false);
            holder.expandIcon.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams layoutParams
                    = (ConstraintLayout.LayoutParams) holder.elvReplacementMaintext.getLayoutParams();
            layoutParams.leftMargin = 90;
            layoutParams.topMargin = 5;
            layoutParams.bottomMargin = 5;
            holder.elvReplacementMaintext.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return navResultInSegments.size() + 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
//        ConstraintLayout navRLayoutContainer1, navRLayoutContainer2, navRLayoutContainer3, navRLayoutContainer4;
//        TextView navR_firstwalktime, navR_firstbusservices, navR_secondbusservices,
//                navR_lastwalktime, navR_totaltime, navR_firstArrow, navR_busArrivalTimingInfo;
//        LinearLayout recyclerviewItemHolder;
        TextView topText, mainText, bottomText, bottomText2;
        View stickTop, stickBottom;
        ImageView circle, human;
        ConstraintLayout constraintLayout10, takeServiceClickable;
        ConstraintLayout elvReplacement;
        LinearLayout elvChildren;
        TextView elvReplacementMaintext;
        ImageView expandIcon, modeIcon;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            topText = itemView.findViewById(R.id.topText);
            mainText = itemView.findViewById(R.id.textView6);
            bottomText = itemView.findViewById(R.id.bottomText);
            circle = itemView.findViewById(R.id.imageView7);
            human = itemView.findViewById(R.id.imageView8);
            stickTop = itemView.findViewById(R.id.view2Top);
            stickBottom = itemView.findViewById(R.id.view2Bottom);
            constraintLayout10 = itemView.findViewById(R.id.constraintLayout10);

            elvReplacement = itemView.findViewById(R.id.elvReplacement);
            elvChildren = itemView.findViewById(R.id.elvChildren);
            elvReplacementMaintext = itemView.findViewById(R.id.elv_replacement_maintext);
            expandIcon = itemView.findViewById(R.id.listExpandImageView);
            modeIcon = itemView.findViewById(R.id.walkingManImageView);

            takeServiceClickable = itemView.findViewById(R.id.constraintLayout23);


//            stop = (TextView) itemView.findViewById(R.id.name);
//            route = (TextView) itemView.findViewById(R.id.email);
//            code = (TextView) itemView.findViewById(R.id.mobileNo);

        }
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

    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo);
    }

}