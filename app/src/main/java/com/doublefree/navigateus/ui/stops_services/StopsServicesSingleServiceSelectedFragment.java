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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.doublefree.navigateus.ExpandableListViewStandardCode;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.StandardCode;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.doublefree.navigateus.data.busrouteinformation.BusOperatingHours;
import com.doublefree.navigateus.data.busrouteinformation.ServiceInfo;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.doublefree.navigateus.ui.AnnouncementTickerTapesDialogFragment;
import com.doublefree.navigateus.ui.DialogFullRouteCallBack;
import com.doublefree.navigateus.ui.StopsMainAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StopsServicesSingleServiceSelectedFragment extends Fragment implements DialogFullRouteCallBack {

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
    List<BusOperatingHours> busOperatingHoursList = new ArrayList<>();

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

        callOperatingHours();

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
        if (item.getItemId() == R.id.action_single_service_timetable) {
            SingleServiceOperatingHoursDialogFragment dialogFragment =
                    SingleServiceOperatingHoursDialogFragment.newInstance(busOperatingHoursList, serviceNumString);
            dialogFragment.show(getChildFragmentManager(), SingleServiceOperatingHoursDialogFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);

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
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.failed_to_connect,
                            Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(R.id.nav_view);
                    snackbar.show();
                }
            });
        } else {
            List<StopList> listOfAllStopsOnRoute = StandardCode.packageStopListFromPickupPoint(serviceFullRouteInJSON);
            this.listOfAllStopsAlongRoute = listOfAllStopsOnRoute;
            initListDataELV(listOfAllStopsOnRoute);
        }

        setServiceStatus();

    }

    Handler timeRefreshHandler = new Handler(Looper.getMainLooper());

    private void setServiceStatus() {

        switch (serviceStatus) {
            case 0:
                serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_ok_20);
                serviceStatusDesc.setText("Good Service");
                break;
            case 1:
                serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_disruption_20);
                serviceStatusDesc.setText("Service Alert");
                serviceDisruptedText.setVisibility(View.VISIBLE);
                ConstraintLayout disruptedClick = rootView.findViewById(R.id.singleServiceStatusConstraintLayout);
                disruptedClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnnouncementTickerTapesDialogFragment dialogFragment =
                                AnnouncementTickerTapesDialogFragment.newInstance(true, null, serviceNumString);
                        dialogFragment.show(getChildFragmentManager(), AnnouncementTickerTapesDialogFragment.TAG);
                    }
                });
                break;
            case 3:
                NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
                    @Override
                    public void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList) {
                        for (int i = 0; i < networkTickerTapesAnnouncementsList.size(); i++) {
                            String[] servicesAffected = networkTickerTapesAnnouncementsList.get(i).getServicesAffected().split(",");
                            for (int j = 0; j < servicesAffected.length; j++) {
                                Log.e("compare service", servicesAffected[j] + " " + serviceNumString);
                                String singleServiceAffected = servicesAffected[j].trim();
                                if (!singleServiceAffected.isEmpty() && !serviceNumString.isEmpty() &&
                                        (servicesAffected[j].trim().contains(serviceNumString)
                                                || serviceNumString.contains(servicesAffected[j].trim())) &&
                                        (!networkTickerTapesAnnouncementsList.get(i).getMessage().contains("testing") &&
                                                !networkTickerTapesAnnouncementsList.get(i).getMessage().contains("Testing") &&
                                                !networkTickerTapesAnnouncementsList.get(i).getMessage().contains("maintenance"))) {
                                    serviceStatus = 1;
                                    break;
                                }
                            }
                        }
                        serviceStatus = 0;
                        setServiceStatus();
                    }

                    @Override
                    public void onFailureTickerTapesAnnouncements() {
                        serviceStatusIcon.setImageResource(R.drawable.ic_baseline_service_unknown_20);
                        serviceStatusDesc.setText("Status Unknown");
                    }
                });
                break;

        }
    }

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
                adapter, listOfAllStopsAlongRoute, getChildFragmentManager(), getActivity(), getContext(), false, StopsServicesSingleServiceSelectedFragment.this);
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
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                                R.string.failed_to_connect,
                                Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(R.id.nav_view);
                        snackbar.show();
                    }
                });
            }
        }

    }

    private void callOperatingHours() {
        NextbusAPIs.callBusOperatingHours(getActivity(), getContext(), serviceNumString, new NextbusAPIs.VolleyCallBackOperatingHours() {
            @Override
            public void onSuccessOperatingHours(List<BusOperatingHours> list) {
                busOperatingHoursList = list;
            }

            @Override
            public void onFailureOperatingHours() {
                //TODO: display snackbar?
            }
        });
    }

    @Override
    public void clickedFullRoute(String serviceNum) {

    }
}
