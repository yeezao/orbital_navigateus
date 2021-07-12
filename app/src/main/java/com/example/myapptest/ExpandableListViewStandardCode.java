package com.example.myapptest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import androidx.fragment.app.FragmentManager;

import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.favourites.FavouriteStop;
import com.example.myapptest.ui.BusLocationDisplayDialogFragment;
import com.example.myapptest.ui.stops_services.SetArrivalNotificationsDialogFragment;
import com.example.myapptest.ui.StopsMainAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListViewStandardCode {

    private static ArrivalNotifications singleStopArrivalNotification;
    private static List<ArrivalNotifications> arrivalNotificationsArray;

    /**
     * Sets the 3 listeners that are used across all ExpandableListViews in this app:
     * GroupClick (expand/collapse), GroupLongClick (Alerts/Favourites Dialog) & ItemClick (Bus Locations)
     *
     *
     * @param expandableListView
     * @param listGroup
     * @param listItem
     * @param adapter
     * @param listOfStops
     * @param childFragmentManager
     * @param activity
     * @param context
     * @param accountFavourites - to determine if filtering of retrieved services needs to be done based on favourites
     */
    public static void expandableListViewListeners(ExpandableListView expandableListView,
                                                   List<StopList> listGroup,
                                                   HashMap<StopList, List<ServiceInStopDetails>> listItem,
                                                   StopsMainAdapter adapter,
                                                   List<StopList> listOfStops, FragmentManager childFragmentManager,
                                                   Activity activity, Context context, boolean accountFavourites) {
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    Log.e("stopid is", listOfStops.get(groupPosition).getStopId());
                    String stopId = StandardCode.StopIdExceptionsWithReturn(listOfStops.get(groupPosition).getStopId());
                    //TODO: hardcoded exceptions need to be changed when new ISB network begins
                    NextbusAPIs.callSingleStopInfo(activity, context, stopId,
                            groupPosition, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                                @Override
                                public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                                    if (servicesAllInfoAtStop.size() == 0) {
                                        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                                                "No services are available at this terminal stop. Please check the origin stop instead.",
                                                Snackbar.LENGTH_LONG);
                                        snackbar.setAnchorView(R.id.nav_view);
                                        snackbar.show();
                                    } else {
                                        List<ServiceInStopDetails> listOfServicesToAdd = new ArrayList<>();
                                        if (accountFavourites) {
                                            for (int j = 0; j < listOfStops.get(groupPosition).getListOfServicesFavourited().size(); j++) {
                                                for (int i = 0; i < servicesAllInfoAtStop.size(); i++) {
                                                    if (listOfStops.get(groupPosition).getListOfServicesFavourited()
                                                            .get(j).equals(servicesAllInfoAtStop.get(i).getServiceNum())) {
                                                        listOfServicesToAdd.add(servicesAllInfoAtStop.get(i));
                                                    }
                                                }
                                            }
                                        } else {
                                            listOfServicesToAdd.addAll(servicesAllInfoAtStop);
                                        }
                                        listItem.remove(listGroup.get(groupPosition));
                                        listItem.put(listGroup.get(groupPosition), listOfServicesToAdd);
                                        adapter.notifyDataSetChanged();
                                        expandableListView.expandGroup(groupPosition, true);
                                    }
                                }

                                @Override
                                public void onFailureSingleStop() {
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
                    arrivalNotificationsArray = ((MainActivity) activity).getArrivalNotificationsArray();
                    for (int i = 0; arrivalNotificationsArray != null && i < arrivalNotificationsArray.size(); i++) {
                        if (arrivalNotificationsArray.get(i).getStopId().equals(listOfStops.get(groupPosition).getStopId())
                                && arrivalNotificationsArray.get(i).isWatchingForArrival()) {
                            Log.e("entered", "yes i  entered");
                            isStopBeingWatched = true;
                            singleStopArrivalNotification = new ArrivalNotifications();
                            singleStopArrivalNotification.setStopId(arrivalNotificationsArray.get(i).getStopId());
                            singleStopArrivalNotification.setStopName(arrivalNotificationsArray.get(i).getStopName());
                            singleStopArrivalNotification.setLatitude(arrivalNotificationsArray.get(i).getLatitude());
                            singleStopArrivalNotification.setLongitude(arrivalNotificationsArray.get(i).getLongitude());
                            singleStopArrivalNotification.setWatchingForArrival(true);
                            singleStopArrivalNotification.setServicesAtStop(arrivalNotificationsArray.get(i).getServicesAtStop());
                            singleStopArrivalNotification.setServicesBeingWatched(arrivalNotificationsArray.get(i).getServicesBeingWatched());
                            singleStopArrivalNotification = updateFavouritesInfo(singleStopArrivalNotification);
                            dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                            dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
                            dialogFragment.show(childFragmentManager, SetArrivalNotificationsDialogFragment.TAG);
                            break;
                        }
                    }
                    if (!isStopBeingWatched) {
                        singleStopArrivalNotification = new ArrivalNotifications();
                        singleStopArrivalNotification.setStopId(listOfStops.get(groupPosition).getStopId());
                        singleStopArrivalNotification.setStopName(listOfStops.get(groupPosition).getStopName());
                        singleStopArrivalNotification.setLatitude(listOfStops.get(groupPosition).getStopLatitude());
                        singleStopArrivalNotification.setLongitude(listOfStops.get(groupPosition).getStopLongitude());
                        singleStopArrivalNotification.setWatchingForArrival(false);
                        singleStopArrivalNotification = updateFavouritesInfo(singleStopArrivalNotification);
                        NextbusAPIs.callSingleStopInfo(activity, context, listOfStops.get(groupPosition).getStopId(),
                                groupPosition, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                                    @Override
                                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                                        singleStopArrivalNotification.setServicesAtStop(servicesAllInfoAtStop);
                                        SetArrivalNotificationsDialogFragment dialogFragment = SetArrivalNotificationsDialogFragment.newInstance(singleStopArrivalNotification);
//                                    dialogFragment.setArrivalNotificationsDialogListener(StopsServicesFragment.this);
//                                    dialogFragment.setTargetFragment(StopsServicesFragment.this, 0);
                                        dialogFragment.show(childFragmentManager, SetArrivalNotificationsDialogFragment.TAG);
                                    }

                                    @Override
                                    public void onFailureSingleStop() {
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

                Log.e("clicklistener", "yes");

                if (listItem.get(listGroup.get(groupPosition)) != null) {
                    String serviceNum = listItem.get(listGroup.get(groupPosition)).get(childPosition).getServiceNum();
                    String stopName = listGroup.get(groupPosition).getStopName();
                    BusLocationDisplayDialogFragment dialogFragment = BusLocationDisplayDialogFragment.newInstance(serviceNum, stopName, listGroup.get(groupPosition));
                    dialogFragment.show(childFragmentManager, BusLocationDisplayDialogFragment.TAG);
                }

                return true;
            }
        });
    }

    /**
     * Updates the favourites info in the ArrivalNotifications instance that is passed in
     *
     * @param singleStopArrivalNotification
     * @return singleStopArrivalNotifications
     */
    public static ArrivalNotifications updateFavouritesInfo(ArrivalNotifications singleStopArrivalNotification) {

        if (MainActivity.favouriteDatabase.favouriteStopCRUD().isFavorite(singleStopArrivalNotification.getStopId()) == 1) {
            singleStopArrivalNotification.setFavourite(true);
            FavouriteStop favouriteStop = MainActivity.favouriteDatabase.favouriteStopCRUD().getFavoriteDataSingle(singleStopArrivalNotification.getStopId());
            singleStopArrivalNotification.setServicesFavourited(FavouriteStop.fromString(favouriteStop.getServicesFavourited()));
        }
        return singleStopArrivalNotification;
    }

}
