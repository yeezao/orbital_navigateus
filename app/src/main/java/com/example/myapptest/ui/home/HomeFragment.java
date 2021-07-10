package com.example.myapptest.ui.home;

import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.myapptest.ExpandableListViewStandardCode;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.LocationServices;
import com.example.myapptest.data.busnetworkinformation.NetworkTickerTapes;
import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopList;
import com.example.myapptest.favourites.FavouriteStop;
import com.example.myapptest.ui.StopsMainAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment implements LocationServices.LocationFound {

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

        expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandable_listview_home);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new StopsMainAdapter(getContext(), listGroup, listItem);
        expandableListView.setAdapter(adapter);

        homeFavouritesProgressBar = rootView.findViewById(R.id.progressBarHomeELV);
        textViewUpdating = rootView.findViewById(R.id.textViewUpdating);

//        List<IsFirstRuns> isFirstRuns = MainActivity.isFirstRunsDatabase.isFirstRunsCRUD().getIsFirstRuns();
//        if (isFirstRuns.get(0).isFirstRunHomeFrag()) {
//            //TODO: load HelpDialogFragment
//        }

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

        checkServiceStatus();
        setExpandableListView();

    }

    private void checkServiceStatus() {

        ProgressBar tickerTapesPB = rootView.findViewById(R.id.serviceStatusProgressBar);
        ImageView tickerTapesStatusIcon = rootView.findViewById(R.id.imageViewServiceStatusIcon);
        TextView tickerTapesText = rootView.findViewById(R.id.textViewServiceStatusDesc);
        ConstraintLayout serviceStatusHomeContainer = rootView.findViewById(R.id.serviceStatusHomeContainer);

        NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesList() {
            @Override
            public void onSuccessTickerTapes(List<NetworkTickerTapes> networkTickerTapesList) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tickerTapesPB.setVisibility(View.GONE);
                        tickerTapesStatusIcon.setVisibility(View.VISIBLE);
                        if (networkTickerTapesList.size() == 0) {
                            tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_ok_24);
                            tickerTapesText.setText("Good service on all routes. Have a great day! :)");
                        } else {
                            tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_disruption_24);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(networkTickerTapesList.size()).append(" active service alert(s). Tap here for info.");
                            tickerTapesText.setText(stringBuilder.toString());
                            serviceStatusHomeContainer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //TODO: open dialog with recyclerview here
                                }
                            });
                        }
                    }
                }, 1000);

            }

            @Override
            public void onFailureTickerTapes() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tickerTapesPB.setVisibility(View.GONE);
                        tickerTapesStatusIcon.setVisibility(View.VISIBLE);
                        tickerTapesStatusIcon.setImageResource(R.drawable.ic_baseline_service_unknown_24);
                        tickerTapesText.setText("We couldn't connect to NUS servers.");
                    }
                }, 1000);
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
                adapter, listOfAllStopsToDisplayInFavourites, getChildFragmentManager(), getActivity(), getContext(), true);
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
                NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), listOfAllStopsToDisplayInFavourites.get(i).getStopId()
                        , i, true, new NextbusAPIs.VolleyCallBackSingleStop() {
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
                        //TODO: display failed to load snackbar
                    }
                });
            }
        }
    }

}