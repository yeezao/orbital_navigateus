package com.doublefree.navigateus.ui.directions;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.HalfKt;
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
import com.doublefree.navigateus.MainActivity;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.naviagationdata.NavigationNodes;
import com.doublefree.navigateus.data.naviagationdata.NavigationPartialResults;
import com.doublefree.navigateus.data.naviagationdata.NavigationResults;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomAdapterRecyclerView extends RecyclerView.Adapter<CustomAdapterRecyclerView.MyViewHolder> {

    ArrayList<String> busStop;
    ArrayList<String> routeId;
    ArrayList<String> busStopCode;
    Activity activity;
    Context context;
    String origin, dest;

    List<NavigationResults> resultsList;

    NavController navController;
    float dpWidth;

    int firstArrival = -1;
    int secondArrival = -1;
    int firstTimeTillNow = -1;

    boolean afterSearch;

    public CustomAdapterRecyclerView(Activity activity, Context context, List<NavigationResults> resultsList, String origin, String dest, NavController navController, float dpWidth, boolean afterSearch) {
        this.activity = activity;
        this.context = context;
        this.resultsList = resultsList;
        this.origin = origin;
        this.dest = dest;
        this.navController = navController;
        this.dpWidth = dpWidth;
        this.afterSearch = afterSearch;
        Log.e("dpwidth is", dpWidth + "");
//        navResultTest = resultsList.get(0);
//        navTestResultSegment = navResultTest.getResultsConcatenated();
//        Log.e("check", navTestResultSegment.toString());
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

        NavigationResults navResultTest = resultsList.get(position);
        List<NavigationPartialResults> navTestResultSegment = navResultTest.getResultsConcatenated();

        StringBuilder firstStringBuilder = new StringBuilder();
        firstStringBuilder.append(navResultTest.getTotalTimeTaken()).append(" min");
        navResultTest.setDisplayTotalTimeTaken(navResultTest.getTotalTimeTaken());
        holder.navR_totaltime.setText(firstStringBuilder.toString());
        holder.navR_firstArrow.setVisibility(View.VISIBLE);
//        holder.recyclerviewItemHolder.setVisibility(View.GONE);

        boolean onlyWalk = true;

        setVia(holder, navTestResultSegment);

        for (int i = 0; i < navTestResultSegment.size(); i++) {
            if (navTestResultSegment.get(i) != null) {
                holder.navR_busArrivalTimingInfo.setText("Updating bus arrival info & total duration...");
                holder.navR_busArrivalTimingInfo.setVisibility(View.VISIBLE);
                NavigationPartialResults currentSegment = navTestResultSegment.get(i);
                Log.e("current node starts at", currentSegment.getNodesTraversed().get(0).getName());
                Log.e("checkcond", (i > 1 && i < navTestResultSegment.size() - 1
                        && navTestResultSegment.get(i - 1).getViableBuses1().size() == 0
                        && navTestResultSegment.get(i + 1).getViableBuses1().size() == 0) + " " + position);
                if (navTestResultSegment.size() >= 5) {
                    holder.navRLayoutContainer3.setVisibility(View.VISIBLE);
                }
                if (currentSegment.getViableBuses1().size() > 0 && i < 2) {
                    onlyWalk = false;
                    holder.navRLayoutContainer2.setVisibility(View.VISIBLE);
                    if (currentSegment.getNodesTraversed().get(0).getName().equals(origin)) {
                        holder.navR_firstArrow.setVisibility(View.GONE);
                    }
                    if (dpWidth > 600) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < currentSegment.getViableBuses1().size(); j++) {
                            if (j > 1) {
                                stringBuilder.append("...");
                                break;
                            }
                            stringBuilder.append(currentSegment.getViableBuses1().get(j));
                            if (j < currentSegment.getViableBuses1().size() - 1) {
                                stringBuilder.append("/");
                            }
                        }
                        holder.navR_firstbusservices.setText(stringBuilder.toString());
                    }
                    int finalI = i;
                    NextbusAPIs.callSingleStopInfo(activity, context, currentSegment.getNodesTraversed().get(0).getId(),
                            0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                        @Override
                        public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                            int arrivaltime = 9999;
                            String service = "";
                            int timeTillNow = 0;
                            for (int j = 0; j < finalI; j++) {
                                timeTillNow += navTestResultSegment.get(j).getTimeForSegment();
                                timeTillNow += navTestResultSegment.get(j).getBusWaitingTime();
                            }
                            firstTimeTillNow = timeTillNow;
                            boolean isServicesAvailable = false;
                            for (ServiceInStopDetails temp : servicesAllInfoAtStop) {
                                for (int i = 0; i < currentSegment.getViableBuses1().size(); i++) {
                                    if (temp.getServiceNum().equals(currentSegment.getViableBuses1().get(i))
                                            || (temp.getServiceNum().equals("D1(To UTown)") //for COM2 bus stop - D1 twd UTown
                                            && currentSegment.getViableBuses1().get(i).equals("D1")
                                            && currentSegment.getNodesTraversed().get(1).getId().equals("LT13-OPP"))
                                            || (temp.getServiceNum().equals("D1(To BIZ2)") //for COM2 bus stop - D1 twd BIZ2
                                            && currentSegment.getViableBuses1().get(i).equals("D1")
                                            && currentSegment.getNodesTraversed().get(1).getId().equals("BIZ2"))) {
                                        if (temp.getFirstArrival().charAt(0) != '-') {
                                            Log.e("entered", "condition");
                                            if ((temp.getFirstArrival().equals("Arr") && timeTillNow == 0)
                                                    || (!temp.getFirstArrival().equals("Arr") && (Integer.parseInt(temp.getFirstArrival()) < arrivaltime)
                                                    && Integer.parseInt(temp.getFirstArrival()) > timeTillNow)) {
                                                if (temp.getFirstArrival().equals("Arr")) {
                                                    arrivaltime = 0;
                                                } else {
                                                    arrivaltime = Integer.parseInt(temp.getFirstArrival());
                                                }
                                                service = temp.getServiceNum();
                                            } else {
                                                isServicesAvailable = true;
                                            }
                                        }
                                        if (temp.getSecondArrival().charAt(0) != '-') {
                                            if ((temp.getSecondArrival().equals("Arr") && timeTillNow == 0)
                                                    || (!temp.getSecondArrival().equals("Arr") && (Integer.parseInt(temp.getSecondArrival()) < arrivaltime)
                                                    && Integer.parseInt(temp.getSecondArrival()) > timeTillNow)) {
                                                if (temp.getSecondArrival().equals("Arr")) {
                                                    arrivaltime = 0;
                                                } else {
                                                    arrivaltime = Integer.parseInt(temp.getSecondArrival());
                                                }
                                                service = temp.getServiceNum();
                                            } else {
                                                isServicesAvailable = true;
                                            }
                                        }
                                    }
                                }
                            }

                            StringBuilder anotherStringBuilder = new StringBuilder();
                            int newTotalTime = 0;
                            if (arrivaltime == 9999) {
                                if (isServicesAvailable) {
                                    anotherStringBuilder.append("No estimate available for ")
                                            .append(currentSegment.getNodesTraversed().get(0).getAltname())
                                            .append("");
                                } else {
                                    anotherStringBuilder.append("No suitable services from ")
                                            .append(currentSegment.getNodesTraversed().get(0).getAltname()).append(" now");
                                }
                                arrivaltime = 0;
                            } else {
                                anotherStringBuilder.append(service);
                                if (arrivaltime == 0) {
                                    anotherStringBuilder.append(" is arriving at ")
                                            .append(currentSegment.getNodesTraversed().get(0).getAltname()).append(" now");
                                } else {
                                    anotherStringBuilder.append(" arriving at ")
                                            .append(currentSegment.getNodesTraversed().get(0).getAltname())
                                            .append(" in ").append(arrivaltime).append(" min");
                                }
                                newTotalTime = navResultTest.getDisplayTotalTimeTaken() - timeTillNow + arrivaltime;
                                navResultTest.setDisplayTotalTimeTaken(newTotalTime);
                            }
                            currentSegment.setTimeAtEndOfSegment(newTotalTime);
                            String stringToSet = anotherStringBuilder.toString();
                            Handler handler = new Handler();
                            firstArrival = arrivaltime;
                            int finalNewTotalTime = newTotalTime;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.navR_busArrivalTimingInfo.setText(stringToSet);
                                    holder.navR_busArrivalTimingInfo.setVisibility(View.VISIBLE);
                                    if (holder.navRLayoutContainer4.getVisibility() == View.GONE && finalNewTotalTime != 0) {
                                        StringBuilder yetAnotherStringBuilder = new StringBuilder();
                                        yetAnotherStringBuilder.append(finalNewTotalTime).append(" min");
                                        holder.navR_totaltime.setText(yetAnotherStringBuilder.toString());
                                    }
                                }
                            }, 800);
                        }

                        @Override
                        public void onFailureSingleStop() {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.navR_busArrivalTimingInfo.setText("Connection to server failed");
                                    holder.navR_busArrivalTimingInfo.setVisibility(View.VISIBLE);
                                }
                            }, 800);
                        }
                    });
                    holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
                } else if (currentSegment.getViableBuses2().size() > 0 ||
                        (i > 1 && i < navTestResultSegment.size() - 1
                                && navTestResultSegment.get(i - 1).getViableBuses1().size() == 0) ||
                        (currentSegment.getViableBuses1().size() > 0 && currentSegment.getEdgeSequence().size() == 0)) {
                    Log.e("entered", position + " ues " + currentSegment.getViableBuses2().size());
                    onlyWalk = false;
                    holder.navRLayoutContainer4.setVisibility(View.VISIBLE);
                    Log.e("checkvis", holder.navRLayoutContainer4.getVisibility() + " " + dpWidth);
                    List<String> viableListToUse = new ArrayList<>();
                    if (currentSegment.getViableBuses2().size() > 0) {
                        viableListToUse = currentSegment.getViableBuses2();
                    } else if (currentSegment.getViableBuses1().size() > 0) {
                        viableListToUse = currentSegment.getViableBuses1();
                    }
                    if (dpWidth > 800) {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (currentSegment.getViableBuses2().size() > 0) {
                            for (int j = 0; j < currentSegment.getViableBuses2().size(); j++) {
                                if (j > 0 && currentSegment.getViableBuses2().size() > 2) {
                                    stringBuilder.append("...");
                                    break;
                                }
                                stringBuilder.append(currentSegment.getViableBuses2().get(j));
                                if (j < currentSegment.getViableBuses2().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                        } else {
                            for (int j = 0; j < currentSegment.getViableBuses1().size(); j++) {
                                if (j > 0 && currentSegment.getViableBuses1().size() > 1 && navTestResultSegment.size() > 4) {
                                    stringBuilder.append("...");
                                    break;
                                } else if (j > 1 && currentSegment.getViableBuses1().size() > 2) {
                                    stringBuilder.append("...");
                                    break;
                                }
                                stringBuilder.append(currentSegment.getViableBuses1().get(j));
                                if (navTestResultSegment.size() > 4 && j < currentSegment.getViableBuses1().size() - 1) {
                                    stringBuilder.append("/");
                                } else if (j < currentSegment.getViableBuses1().size() - 1) {
                                    stringBuilder.append("/");
                                }
                            }
                        }
                        holder.navR_secondbusservices.setText(stringBuilder.toString());
                    }
                    int finalI = i;
                    List<String> finalViableListToUse = viableListToUse;
                    NextbusAPIs.callSingleStopInfo(activity, context, currentSegment.getNodesTraversed().get(0).getId(),
                            0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                        @Override
                        public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                            int arrivaltime = 9999;
                            String service = "";
                            int timeTillNow = 0;
                            for (int j = 0; j < finalI; j++) {
                                timeTillNow += navTestResultSegment.get(j).getTimeForSegment();
                                timeTillNow += navTestResultSegment.get(j).getBusWaitingTime();
                            }
                            Log.e("timetillbnow", timeTillNow + "");
                            boolean isServicesAvailable = false;
                            for (ServiceInStopDetails temp : servicesAllInfoAtStop) {
                                for (int i = 0; !finalViableListToUse.isEmpty() && i < finalViableListToUse.size(); i++) {
                                    if (temp.getServiceNum().equals(finalViableListToUse.get(i))
                                            || (temp.getServiceNum().equals("D1(To UTown)") //for COM2 bus stop - D1 twd UTown
                                            && finalViableListToUse.get(i).equals("D1")
                                            && currentSegment.getNodesTraversed().get(1).getId().equals("LT13-OPP"))
                                            || (temp.getServiceNum().equals("D1(To BIZ2)") //for COM2 bus stop - D1 twd BIZ2
                                            && finalViableListToUse.get(i).equals("D1")
                                            && currentSegment.getNodesTraversed().get(1).getId().equals("BIZ2"))) {
                                        Log.e("entered", "condition " + timeTillNow);
                                        if (temp.getFirstArrival().charAt(0) != '-') {
                                            if ((temp.getFirstArrival().equals("Arr") && timeTillNow == 0)
                                                    || (!temp.getFirstArrival().equals("Arr") && (Integer.parseInt(temp.getFirstArrival()) < arrivaltime)
                                                    && Integer.parseInt(temp.getFirstArrival()) >= timeTillNow)) {
                                                if (temp.getFirstArrival().equals("Arr")) {
                                                    arrivaltime = 0;
                                                } else {
                                                    arrivaltime = Integer.parseInt(temp.getFirstArrival());
                                                }
                                                service = temp.getServiceNum();
                                            } else {
                                                isServicesAvailable = true;
                                            }
                                        }
                                        Log.e("inner arrivaltime", arrivaltime + "");
                                        if (temp.getSecondArrival().charAt(0) != '-') {
                                            if ((temp.getSecondArrival().equals("Arr") && timeTillNow == 0)
                                                    || (!temp.getSecondArrival().equals("Arr") && (Integer.parseInt(temp.getSecondArrival()) < arrivaltime)
                                                    && Integer.parseInt(temp.getSecondArrival()) >= timeTillNow)) {
                                                if (temp.getSecondArrival().equals("Arr")) {
                                                    arrivaltime = 0;
                                                } else {
                                                    arrivaltime = Integer.parseInt(temp.getSecondArrival());
                                                }
                                                service = temp.getServiceNum();
                                            } else {
                                                isServicesAvailable = true;
                                            }
                                        }
                                    }
                                }
                            }

                            StringBuilder anotherStringBuilder = new StringBuilder();
                            Handler handler = new Handler();
                            int finalArrivaltime = arrivaltime;
                            int finalTimeTillNow = timeTillNow;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int newTotalTime = 0;
                                    if (finalArrivaltime < 9999) {
                                        newTotalTime = navResultTest.getDisplayTotalTimeTaken() - finalTimeTillNow + finalArrivaltime;
                                        navResultTest.setDisplayTotalTimeTaken(newTotalTime);
                                    }
                                    currentSegment.setTimeAtEndOfSegment(newTotalTime);
                                    Log.e("finalnewtotaltime", newTotalTime + " " + finalArrivaltime);
                                    secondArrival = finalArrivaltime;
                                    if (newTotalTime != 0) {
                                        StringBuilder yetAnotherStringBuilder = new StringBuilder();
                                        yetAnotherStringBuilder.append(newTotalTime).append(" min");
                                        holder.navR_totaltime.setText(yetAnotherStringBuilder.toString());
                                    }
                                }
                            }, 800);
                        }

                        @Override
                        public void onFailureSingleStop() {

                        }
                    });
                } else if (currentSegment.getViableBuses1().size() == 0 && currentSegment.getNodesTraversed().get(0).getName().equals(origin)) {
                    holder.navRLayoutContainer1.setVisibility(View.VISIBLE);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(currentSegment.getTimeForSegment());
                    holder.navR_firstwalktime.setText(stringBuilder.toString());
                } else if (currentSegment.getViableBuses1().size() == 0
                        && currentSegment.getNodesTraversed().get(currentSegment.getNodesTraversed().size() - 1).getName().equals(dest)) {
                    holder.navRLayoutContainer5.setVisibility(View.VISIBLE);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(currentSegment.getTimeForSegment());
                    Log.e("string is", stringBuilder.toString());
                    holder.navR_lastwalktime.setText(stringBuilder.toString());
                } else if (currentSegment.getViableBuses1().size() == 0 || navTestResultSegment.size() >= 5) {
                    holder.navRLayoutContainer3.setVisibility(View.VISIBLE);
                }

                if (onlyWalk) {
                    holder.navR_busArrivalTimingInfo.setVisibility(View.GONE);
                    holder.recyclerviewItemHolder.setVisibility(View.VISIBLE);
                }

            }
        }

        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Navigation.createNavigateOnClickListener(R.id.action_navigation_directions_to_directionsResultFragment);
                DirectionsFragmentDirections.ActionNavigationDirectionsToDirectionsResultFragment action =
                        DirectionsFragmentDirections.actionNavigationDirectionsToDirectionsResultFragment(navResultTest, origin, dest);
                navController.navigate(action);

            }
        });

    }

    private void setVia(MyViewHolder holder, List<NavigationPartialResults> navTestResultSegment) {
        if (navTestResultSegment.size() > 2) {
            holder.navR_via.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder = new StringBuilder();
            if (navTestResultSegment.size() > 3 && navTestResultSegment.get(1).getEdgeSequence().size() == 0
                    && navTestResultSegment.get(3).getEdgeSequence().size() == 0) {
                Log.e("case", "1 " + " " + navTestResultSegment.get(1).getNodeSequence().get(0).getName() + " " + navTestResultSegment.get(1).getNodeSequence().get(navTestResultSegment.get(1).getNodeSequence().size() - 1).getAltname() + " " + navTestResultSegment.get(3).getNodesTraversed().get(0).getName());
                stringBuilder.append("via ")
                        .append(navTestResultSegment.get(1).getNodeSequence().get(navTestResultSegment.get(1).getNodeSequence().size() - 1).getAltname())
                        .append(", ")
                        .append(navTestResultSegment.get(3).getNodeSequence().get(0).getAltname());
            } else if (navTestResultSegment.size() >= 3 && navTestResultSegment.get(2).getEdgeSequence().size() == 0
                    && navTestResultSegment.get(0).getEdgeSequence().size() == 0) {
                Log.e("case", "2" + navTestResultSegment.get(0).getNodeSequence().get(navTestResultSegment.get(0).getNodeSequence().size() - 1).getAltname());
                stringBuilder.append("via ")
                        .append(navTestResultSegment.get(0).getNodeSequence().get(navTestResultSegment.get(0).getNodeSequence().size() - 1).getAltname())
                        .append(", ")
                        .append(navTestResultSegment.get(2).getNodeSequence().get(0).getAltname());
            } else if (navTestResultSegment.size() > 3 && navTestResultSegment.get(2).getEdgeSequence().size() == 0
                    && navTestResultSegment.get(1).getEdgeSequence().size() == 0) {
                Log.e("case", "3");
                stringBuilder.append("via ")
                        .append(navTestResultSegment.get(1).getNodeSequence().get(navTestResultSegment.get(1).getNodeSequence().size() - 1).getAltname())
                        .append(", ")
                        .append(navTestResultSegment.get(2).getNodeSequence().get(navTestResultSegment.get(2).getNodeSequence().size() - 1).getAltname());
            } else if (navTestResultSegment.size() > 2 && navTestResultSegment.get(0).getEdgeSequence().size() == 0
                    && navTestResultSegment.get(1).getEdgeSequence().size() == 0) {
                Log.e("case", "4");
                stringBuilder.append("via ")
                        .append(navTestResultSegment.get(0).getNodeSequence().get(navTestResultSegment.get(0).getNodeSequence().size() - 1).getAltname())
                        .append(", ")
                        .append(navTestResultSegment.get(1).getNodeSequence().get(navTestResultSegment.get(1).getNodeSequence().size() - 1).getAltname());
            } else if (navTestResultSegment.get(1).getEdgeSequence().size() == 0) {
                Log.e("case", "5");
                stringBuilder.append("via ")
                        .append(navTestResultSegment.get(1).getNodeSequence().get(navTestResultSegment.get(1).getNodeSequence().size() - 1).getAltname());
//            } else if (navTestResultSegment.size() == 3 && navTestResultSegment.get(1).getNodeSequence().size() > 0) {
//                stringBuilder.append("via ").append(navTestResultSegment.get(1).getNodeSequence().get(0).getName());
            } else {
                holder.navR_via.setVisibility(View.GONE);
            }
            holder.navR_via.setText(stringBuilder.toString());
        } else {
            holder.navR_via.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout navRLayoutContainer1, navRLayoutContainer2, navRLayoutContainer3, navRLayoutContainer4, navRLayoutContainer5;
        TextView navR_firstwalktime, navR_firstbusservices, navR_secondbusservices, navR_via,
                navR_lastwalktime, navR_midwalktime, navR_totaltime, navR_firstArrow, navR_busArrivalTimingInfo;
        LinearLayout recyclerviewItemHolder;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            navRLayoutContainer1 = itemView.findViewById(R.id.navRLayoutContainer_1);
            navRLayoutContainer2 = itemView.findViewById(R.id.navRLayoutContainer_2);
            navRLayoutContainer3 = itemView.findViewById(R.id.navRLayoutContainer_3);
            navRLayoutContainer4 = itemView.findViewById(R.id.navRLayoutContainer_4);
            navRLayoutContainer5 = itemView.findViewById(R.id.navRLayoutContainer_5);

            navR_firstwalktime = itemView.findViewById(R.id.navR_firstwalktime);
            navR_firstbusservices = itemView.findViewById(R.id.navR_firstbusServices);
            navR_secondbusservices = itemView.findViewById(R.id.navR_secondbusServices);
            navR_lastwalktime = itemView.findViewById(R.id.navR_lastwalktiming);
            navR_totaltime = itemView.findViewById(R.id.navR_totaltime);
            navR_firstArrow = itemView.findViewById(R.id.navR_nextDirection_1);
            navR_midwalktime = itemView.findViewById(R.id.navR_midwalktime);
            navR_via = itemView.findViewById(R.id.textView_via);

            navR_busArrivalTimingInfo = itemView.findViewById(R.id.busArrivalTimingInfo);
            recyclerviewItemHolder = itemView.findViewById(R.id.recyclerviewItemHolder);



//            stop = (TextView) itemView.findViewById(R.id.name);
//            route = (TextView) itemView.findViewById(R.id.email);
//            code = (TextView) itemView.findViewById(R.id.mobileNo);

        }
    }
}