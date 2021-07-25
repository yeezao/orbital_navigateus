package com.doublefree.navigateus.ui.home;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Half;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.Navigation;

import com.doublefree.navigateus.ExpandableListViewStandardCode;
import com.doublefree.navigateus.MainActivity;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.StandardCode;
import com.doublefree.navigateus.data.LocationServices;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busrouteinformation.ServiceInfo;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.doublefree.navigateus.favourites.FavouriteStop;
import com.doublefree.navigateus.ui.AnnouncementTickerTapesDialogFragment;
import com.doublefree.navigateus.ui.BusLocationDisplayDialogFragmentDirections;
import com.doublefree.navigateus.ui.DialogFullRouteCallBack;
import com.doublefree.navigateus.ui.StopsMainAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment implements LocationServices.LocationFound, DialogFullRouteCallBack {

    public HomeFragment() {}

    ExpandableListView expandableListView;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    StopsMainAdapter adapter;

    List<StopList> listOfAllStopsToDisplayInFavourites;

    ProgressBar homeFavouritesProgressBar;
    TextView textViewUpdating;

    boolean isFirstRun = true;

    LocationServices locationServices = new LocationServices();

    View rootView;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.VISIBLE);

        expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandable_listview_home);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getActivity(), getContext(), listGroup, listItem, expandableListView, getChildFragmentManager());
        Log.e("reset", "adapter");
        expandableListView.setAdapter(adapter);

        homeFavouritesProgressBar = rootView.findViewById(R.id.progressBarHomeELV);
        textViewUpdating = rootView.findViewById(R.id.textViewUpdating);

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        menu.clear();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_messages) {
            AnnouncementTickerTapesDialogFragment dialogFragment = AnnouncementTickerTapesDialogFragment.newInstance(false, null, "");
            dialogFragment.show(getChildFragmentManager(), AnnouncementTickerTapesDialogFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.home_searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText search = (EditText) view.findViewById(R.id.home_search);
                Log.d("search", search.getText().toString());

            }
        });

        setExpandableListView();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkServiceStatus();
                handler.postDelayed(this, 60000);
            }
        }, 0);

    }

    private void checkServiceStatus() {

        ProgressBar tickerTapesPB = rootView.findViewById(R.id.serviceStatusProgressBar);
        ImageView tickerTapesStatusIcon = rootView.findViewById(R.id.imageViewServiceStatusIcon);
        TextView tickerTapesText = rootView.findViewById(R.id.textViewServiceStatusDesc);
        ConstraintLayout serviceStatusHomeContainer = rootView.findViewById(R.id.serviceStatusHomeContainer);

        NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
            @Override
            public void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList) {
                tickerTapesPB.setVisibility(View.GONE);
                tickerTapesStatusIcon.setVisibility(View.VISIBLE);
                if (networkTickerTapesAnnouncementsList.size() == 0) {
                    tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_ok_24);
                    tickerTapesText.setText("Good service on all routes. Have a great day! :)");
                } else {
                    tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_disruption_24);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(networkTickerTapesAnnouncementsList.size()).append(" active service alert(s). Tap here for info.");
                    tickerTapesText.setText(stringBuilder.toString());
                    serviceStatusHomeContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AnnouncementTickerTapesDialogFragment dialogFragment =
                                    AnnouncementTickerTapesDialogFragment.newInstance(true, networkTickerTapesAnnouncementsList, "");
                            dialogFragment.show(getChildFragmentManager(), AnnouncementTickerTapesDialogFragment.TAG);
                        }
                    });
                }
            }

            @Override
            public void onFailureTickerTapesAnnouncements() {
                tickerTapesPB.setVisibility(View.GONE);
                tickerTapesStatusIcon.setVisibility(View.VISIBLE);
                tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_unknown_24);
                tickerTapesText.setText("We couldn't connect to NUS servers.");
            }
        });

    }

    private void setExpandableListView() {

        List<FavouriteStop> listOfFavouriteStops = MainActivity.favouriteDatabase.favouriteStopCRUD().getFavoriteData();
        if (listOfFavouriteStops.size() == 0) {
            homeFavouritesProgressBar.setVisibility(View.INVISIBLE);
        }
        listOfAllStopsToDisplayInFavourites = new ArrayList<>();
        for (int i = 0; i < listOfFavouriteStops.size(); i++) {
            FavouriteStop currentFavStop = listOfFavouriteStops.get(i);
            StopList newStopList = new StopList();
            newStopList.setStopName(currentFavStop.getStopName());
            newStopList.setStopId(currentFavStop.getStopId());
            newStopList.setStopLatitude(currentFavStop.getLatitude());
            newStopList.setStopLongitude(currentFavStop.getLongitude());
            newStopList.setListOfServicesFavourited(FavouriteStop.fromString(listOfFavouriteStops.get(i).getServicesFavourited()));
            listOfAllStopsToDisplayInFavourites.add(newStopList);
        }
        initListData(listOfAllStopsToDisplayInFavourites);
    }

    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    boolean isLocationPermissionGranted = false;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    isLocationPermissionGranted = true;
                } else {
                    isLocationPermissionGranted = false;
                }
                locationServices.getUserLocationAndStopList();
                return;
            }
        }
    }

    @Override
    public void onLocationSecured(Location userLocation) {
        initListData(listOfAllStopsToDisplayInFavourites);
    }

    Handler timeRefreshHandler = new Handler(Looper.getMainLooper());

    private void initListData(List<StopList> listOfAllStops) {
        listGroup.clear();
        for (int i = 0; i < listOfAllStops.size(); i++) {
//            Log.e("stop caption:", captions.get(i));
//            Log.e("i is: ", "" + i);
            StopList stopList = new StopList();
            listGroup.add(listOfAllStops.get(i));
        }
        adapter.notifyDataSetChanged();
        expandableListView.setVisibility(View.VISIBLE);
        homeFavouritesProgressBar.setVisibility(View.INVISIBLE);
        ExpandableListViewStandardCode.expandableListViewListeners(expandableListView, listGroup, listItem,
                adapter, listOfAllStopsToDisplayInFavourites, getChildFragmentManager(), getActivity(), getContext(), true, HomeFragment.this);
//        expandableListViewListeners();

        if (!isFirstRun) {
            adapter.notifyDataSetChanged();
            refreshTimingsWithFavourites(true, rootView);
        } else if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            isFirstRun = false;
            timeRefreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshTimingsWithFavourites(false, rootView);
                    timeRefreshHandler.postDelayed(this, 15000);
                }
            }, 15000);
        }

    }

    private void refreshTimingsWithFavourites(boolean isOnClick, @NotNull View view) {
        boolean anyExpanded = false;
        textViewUpdating.setVisibility(View.VISIBLE);
        homeFavouritesProgressBar.setVisibility(View.VISIBLE);

        final int[] numberOfGroupsExpanded = {0};
        for (int i = 0; i < listOfAllStopsToDisplayInFavourites.size(); i++) {
            if (expandableListView.isGroupExpanded(i)) {
                numberOfGroupsExpanded[0]++;
            }
        }
        if (numberOfGroupsExpanded[0] == 0) {
            textViewUpdating.setVisibility(View.INVISIBLE);
            homeFavouritesProgressBar.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; numberOfGroupsExpanded[0] > 0 && i < listOfAllStopsToDisplayInFavourites.size(); i++) {
            if (expandableListView.isGroupExpanded(i)) {
                anyExpanded = true;
                int finalI = i;
                NextbusAPIs.callSingleStopInfo(getActivity(), getContext(),
                        StandardCode.StopIdExceptionsWithReturn(listOfAllStopsToDisplayInFavourites.get(i).getStopId()),
                        i, true, new NextbusAPIs.VolleyCallBackSingleStop() {
                    @Override
                    public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                        List<ServiceInStopDetails> listOfServicesToAdd = new ArrayList<>();
                        for (int j = 0; j < listOfAllStopsToDisplayInFavourites.get(finalI).getListOfServicesFavourited().size(); j++) {
                            for (int k = 0; k < servicesAllInfoAtStop.size(); k++) {
                                if (listOfAllStopsToDisplayInFavourites.get(finalI).getListOfServicesFavourited()
                                        .get(j).equals(servicesAllInfoAtStop.get(k).getServiceNum())) {
                                    listOfServicesToAdd.add(servicesAllInfoAtStop.get(k));
                                }
                            }
                        }
                        listItem.remove(listGroup.get(finalI));
                        listItem.put(listGroup.get(finalI), listOfServicesToAdd);
                        adapter.notifyDataSetChanged();
                        numberOfGroupsExpanded[0]--;
                        if (numberOfGroupsExpanded[0] == 0) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    homeFavouritesProgressBar.setVisibility(View.INVISIBLE);
                                    textViewUpdating.setVisibility(View.INVISIBLE);
                                }
                            }, 1500);
                        }
                    }

                    @Override
                    public void onFailureSingleStop() {
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                                R.string.failed_to_connect,
                                Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(R.id.nav_view);
                        snackbar.show();
                        //TODO: display failed to load snackbar
                    }
                });
            }
        }
    }

    @Override
    public void clickedFullRoute(final String serviceNumToCheck) {
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                "Loading full route for " + serviceNumToCheck + "...",
                Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(R.id.nav_view);
        snackbar.show();
        NextbusAPIs.callPickupPoint(true, serviceNumToCheck, getActivity(), getContext(), new NextbusAPIs.VolleyCallBackPickupPoint() {
            @Override
            public void OnSuccessPickupPointString(String response) {
                NextbusAPIs.callListOfServices(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackServiceList() {
                    @Override
                    public void onSuccessServiceList(List<ServiceInfo> servicesInfo) {
                        for (int i = 0; i < servicesInfo.size(); i++) {
                            if (servicesInfo.get(i).getServiceNum().equals(serviceNumToCheck)) {
                                String serviceNumToCheckDesc = servicesInfo.get(i).getServiceDesc();
                                Navigation.createNavigateOnClickListener(R.id.action_navigation_home_to_navigation_stopsServicesSingleServiceSelectedFragment);
                                HomeFragmentDirections.ActionNavigationHomeToNavigationStopsServicesSingleServiceSelectedFragment action =
                                        HomeFragmentDirections.actionNavigationHomeToNavigationStopsServicesSingleServiceSelectedFragment(
                                                serviceNumToCheck, serviceNumToCheckDesc, 3, response);
                                Navigation.findNavController(rootView).navigate(action);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onFailureServiceList() {

                    }
                });
            }

            @Override
            public void onSuccessPickupPoint(List<StopList> listOfStopsAlongRoute) {
                //DO NOTHING
            }

            @Override
            public void onFailurePickupPoint() {

            }
        });
    }
}