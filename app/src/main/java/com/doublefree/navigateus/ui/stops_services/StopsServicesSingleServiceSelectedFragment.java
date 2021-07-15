package com.doublefree.navigateus.ui.stops_services;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.doublefree.navigateus.ExpandableListViewStandardCode;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.StandardCode;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.doublefree.navigateus.ui.StopsMainAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StopsServicesSingleServiceSelectedFragment extends Fragment {

    View rootView;

    TextView serviceStatusDesc, serviceNum, serviceDesc, serviceDisruptedText;
    ImageView serviceStatusIcon;

    String serviceNumString, serviceDescString;
    int serviceStatus;

    String serviceFullRouteInJSON;

    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;
    
    List<StopList> listOfAllStopsAlongRoute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_single_service_selected, container, false);

        serviceNum = rootView.findViewById(R.id.serviceNumInSingle);
        serviceDesc = rootView.findViewById(R.id.serviceDescInSingle);
        serviceStatusDesc = rootView.findViewById(R.id.serviceStatusDescInSingle);
        serviceStatusIcon = rootView.findViewById(R.id.serviceStatusInSingleIcon);
        serviceDisruptedText = rootView.findViewById(R.id.serviceDisruptedText);

        expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandable_listview_nus_stops_for_single_service);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);

        setHasOptionsMenu(true);

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.single_service_route_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_single_service_timetable:
                //TODO: load dialog with service timetable
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        serviceNumString = StopsServicesSingleServiceSelectedFragmentArgs.fromBundle(getArguments()).getServiceNum();
        serviceDescString = StopsServicesSingleServiceSelectedFragmentArgs.fromBundle(getArguments()).getServiceDesc();
        serviceStatus = StopsServicesSingleServiceSelectedFragmentArgs.fromBundle(getArguments()).getServiceStatus();
        serviceFullRouteInJSON = StopsServicesSingleServiceSelectedFragmentArgs.fromBundle(getArguments()).getServiceFullRoute();

        serviceNum.setText(serviceNumString);
        serviceDesc.setText(serviceDescString);

        Log.e("serviceStatus", serviceStatus + "");

        switch (serviceStatus) {
            case 0:
                serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_ok_20);
                serviceStatusDesc.setText("Good Service");
                break;
            case 1:
                serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_disruption_20);
                serviceStatusDesc.setText("Service Disrupted");
                serviceDisruptedText.setVisibility(View.VISIBLE);
                serviceDisruptedText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: load dialog fragment with disruption announcement
                    }
                });
                break;
            case 3:
                serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_unknown_20);
                serviceStatusDesc.setText("Status Unknown");
                break;
        }

        if (serviceFullRouteInJSON.isEmpty()) {
            NextbusAPIs.callPickupPoint(false, serviceNumString, getActivity(), getContext(), new NextbusAPIs.VolleyCallBackPickupPoint() {
                @Override
                public void OnSuccessPickupPointString(String response) { //do nothing
                }
                @Override
                public void onSuccessPickupPoint(List<StopList> listOfStopsAlongRoute) {
                    listOfAllStopsAlongRoute = listOfStopsAlongRoute;
                    initListDataELV(listOfStopsAlongRoute);
                }

                @Override
                public void onFailurePickupPoint() {
                    //TODO: display snackbar
                }
            });
        } else {
            List<StopList> listOfAllStopsOnRoute = StandardCode.packageStopListFromPickupPoint(serviceFullRouteInJSON);
            this.listOfAllStopsAlongRoute = listOfAllStopsOnRoute;
            initListDataELV(listOfAllStopsOnRoute);
        }


    }

    Handler timeRefreshHandler = new Handler(Looper.getMainLooper());

    private void initListDataELV(List<StopList> listOfAllStops) {

        listGroup.clear();
        for (int i = 0; i < listOfAllStops.size(); i++) {
            if (i == 0) {
                listOfAllStops.get(i).setStopName(listOfAllStops.get(i).getStopName() + " (Start)");
            } else if (i == listOfAllStops.size() - 1) {
                listOfAllStops.get(i).setStopName(listOfAllStops.get(i).getStopName() + " (End)");
            }
            listGroup.add(listOfAllStops.get(i));
        }
        adapter.notifyDataSetChanged();
        expandableListView.setVisibility(View.VISIBLE);
        ExpandableListViewStandardCode.expandableListViewListeners(expandableListView, listGroup, listItem,
                adapter, listOfAllStopsAlongRoute, getChildFragmentManager(), getActivity(), getContext(), false);
//        expandableListViewListeners();
        
        timeRefreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshTimings(false, rootView);
                timeRefreshHandler.postDelayed(this, 15000);
            }
        }, 15000);

    }

    private void refreshTimings(boolean isOnClick, @NotNull View view) {

        final int[] numberOfGroupsExpanded = {0};
        for (int i = 0; i < listOfAllStopsAlongRoute.size(); i++) {
            if (expandableListView.isGroupExpanded(i)) {
                numberOfGroupsExpanded[0]++;
            }
        }
        for (int i = 0; numberOfGroupsExpanded[0] > 0 && i < listOfAllStopsAlongRoute.size(); i++) {
            if (expandableListView.isGroupExpanded(i)) {
                int finalI = i;
                NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), listOfAllStopsAlongRoute.get(i).getStopId()
                        , i, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                    @Override
                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                        listItem.remove(listGroup.get(finalI));
                        listItem.put(listGroup.get(finalI), servicesAllInfoAtStop);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailureSingleStop() {
                        //TODO: display failed to load snackbar
                    }
                });
            }
        }

    }

}
