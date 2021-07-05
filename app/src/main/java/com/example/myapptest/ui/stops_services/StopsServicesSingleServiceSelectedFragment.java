package com.example.myapptest.ui.stops_services;

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
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.StandardCode;
import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.favourites.FavouriteStop;
import com.google.android.material.snackbar.Snackbar;

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
        listGroup.addAll(listOfAllStops);
        adapter.notifyDataSetChanged();
        expandableListView.setVisibility(View.VISIBLE);
        expandableListViewListeners();
        
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
                            public void onFailureAllStops() {
                                //TODO: display failed to load snackbar
                            }
                        });
            }
        }
    }

    List<ArrivalNotifications> arrivalNotificationsArray = new ArrayList<>();
    ArrivalNotifications singleStopArrivalNotification;

    private void expandableListViewListeners() {
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    Log.e("stopid is", listOfAllStopsAlongRoute.get(groupPosition).getStopId());
                    String stopId = StandardCode.StopIdExceptionsWithReturn(listOfAllStopsAlongRoute.get(groupPosition).getStopId());
                    //TODO: hardcoded exceptions need to be changed when new ISB network begins
                    NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), stopId,
                            groupPosition, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                                @Override
                                public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                                    if (servicesAllInfoAtStop.size() == 0) {
                                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                                                "No services are available at this terminal stop. Please check the origin stop instead.",
                                                Snackbar.LENGTH_LONG);
                                        snackbar.setAnchorView(R.id.nav_view);
                                        snackbar.show();
                                    } else {
                                        listItem.remove(listGroup.get(groupPosition));
                                        listItem.put(listGroup.get(groupPosition), servicesAllInfoAtStop);
                                        adapter.notifyDataSetChanged();
                                        expandableListView.expandGroup(groupPosition, true);
                                    }
                                }

                                @Override
                                public void onFailureAllStops() {
                                    //TODO: display failed to load snackback
                                }
                            });
                }
                return true;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    SetArrivalNotificationsDialogFragment dialogFragment;
                    boolean isStopBeingWatched = false;
                    arrivalNotificationsArray = ((MainActivity) getActivity()).getArrivalNotificationsArray();
                    for (int i = 0; arrivalNotificationsArray != null && i < arrivalNotificationsArray.size(); i++) {
                        if (arrivalNotificationsArray.get(i).getStopId().equals(listOfAllStopsAlongRoute.get(groupPosition).getStopId())
                                && arrivalNotificationsArray.get(i).isWatchingForArrival()) {
                            Log.e("entered", "yes i  entered");
                            isStopBeingWatched = true;
                            singleStopArrivalNotification = new ArrivalNotifications();
                            singleStopArrivalNotification.setStopId(arrivalNotificationsArray.get(i).getStopId());
                            singleStopArrivalNotification.setStopName(arrivalNotificationsArray.get(i).getStopName());
                            singleStopArrivalNotification.setLatitude(arrivalNotificationsArray.get(i).getLatitude());
                            singleStopArrivalNotification.setLongitude(arrivalNotificationsArray.get(i).getLongitude());
                            singleStopArrivalNotification.setWatchingForArrival(true);
                            singleStopArrivalNotification.setServicesBeingWatched(arrivalNotificationsArray.get(i).getServicesBeingWatched());
                            singleStopArrivalNotification = updateFavouritesInfo(singleStopArrivalNotification);
                            dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                            dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
                            dialogFragment.show(getChildFragmentManager(), SetArrivalNotificationsDialogFragment.TAG);
                            break;
                        }
                    }
                    if (!isStopBeingWatched) {
                        singleStopArrivalNotification = new ArrivalNotifications();
                        singleStopArrivalNotification.setStopId(listOfAllStopsAlongRoute.get(groupPosition).getStopId());
                        singleStopArrivalNotification.setStopName(listOfAllStopsAlongRoute.get(groupPosition).getStopName());
                        singleStopArrivalNotification.setLatitude(listOfAllStopsAlongRoute.get(groupPosition).getStopLatitude());
                        singleStopArrivalNotification.setLongitude(listOfAllStopsAlongRoute.get(groupPosition).getStopLongitude());
                        singleStopArrivalNotification.setWatchingForArrival(false);
                        singleStopArrivalNotification = updateFavouritesInfo(singleStopArrivalNotification);
                        NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), listOfAllStopsAlongRoute.get(groupPosition).getStopId(),
                                groupPosition, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                                    @Override
                                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                                        singleStopArrivalNotification.setServicesAtStop(servicesAllInfoAtStop);
                                        SetArrivalNotificationsDialogFragment dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                                    dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
//                                    dialogFragment.setTargetFragment(StopsServicesFragment.this, 0);
                                        dialogFragment.show(getChildFragmentManager(), SetArrivalNotificationsDialogFragment.TAG);
                                    }

                                    @Override
                                    public void onFailureAllStops() {
                                        //TODO: display failed to load snackback
                                    }
                                });

                    }
                    return true;
                }
                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });
    }

    private ArrivalNotifications updateFavouritesInfo(ArrivalNotifications singleStopArrivalNotification) {

        if (MainActivity.favouriteDatabase.favouriteStopCRUD().isFavorite(singleStopArrivalNotification.getStopId()) == 1) {
            singleStopArrivalNotification.setFavourite(true);
            FavouriteStop favouriteStop = MainActivity.favouriteDatabase.favouriteStopCRUD().getFavoriteDataSingle(singleStopArrivalNotification.getStopId());
            singleStopArrivalNotification.setServicesFavourited(FavouriteStop.fromString(favouriteStop.getServicesFavourited()));
        }
        return singleStopArrivalNotification;
    }

}
