package com.doublefree.navigateus.ui.stops_services;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.doublefree.navigateus.data.busrouteinformation.ServiceInfo;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busstopinformation.StopList;

import java.util.ArrayList;
import java.util.List;

public class StopsServicesServicesFragment extends Fragment {

    View rootView;
    RecyclerView nusServicesRecyclerView;
    ProgressBar progressBar;

    List<ServiceInfo> listOfServices = new ArrayList<>();

    SingleServiceCustomAdapterRecyclerView singleServiceCustomAdapterRecyclerView;

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_stops_services_services, container, false);

        navController = NavHostFragment.findNavController(StopsServicesServicesFragment.this);

        progressBar = rootView.findViewById(R.id.ServicesNUSMainLoadingProgressBar);

        nusServicesRecyclerView = rootView.findViewById(R.id.serviceListRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        nusServicesRecyclerView.setLayoutManager(linearLayoutManager);
        singleServiceCustomAdapterRecyclerView = new SingleServiceCustomAdapterRecyclerView(
                getContext(), listOfServices, navController);
        nusServicesRecyclerView.setAdapter(singleServiceCustomAdapterRecyclerView);

        if (listOfServices.size() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NextbusAPIs.callListOfServices(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackServiceList() {
            @Override
            public void onSuccessServiceList(List<ServiceInfo> servicesInfo) {
                listOfServices = servicesInfo;
                NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
                    @Override
                    public void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList) {
                        Log.e("tickertapes", " success");
                        for (int i = 0; i < networkTickerTapesAnnouncementsList.size(); i++) {
                            String[] servicesAffected = networkTickerTapesAnnouncementsList.get(i).getServicesAffected().split(",");
                            for (int j = 0; j < servicesAffected.length; j++) {
                                for (int k = 0; k < listOfServices.size(); k++) {
                                    Log.e("compare service", servicesAffected[j] + " " + listOfServices.get(k).getServiceNum());
                                    String singleServiceAffected = servicesAffected[j].trim();
                                    String serviceToCheck = listOfServices.get(k).getServiceNum();
                                    if (!singleServiceAffected.isEmpty() && !serviceToCheck.isEmpty() &&
                                            (servicesAffected[j].trim().contains(listOfServices.get(k).getServiceNum())
                                            || listOfServices.get(k).getServiceNum().contains(servicesAffected[j].trim())) &&
                                            (!networkTickerTapesAnnouncementsList.get(i).getMessage().contains("testing") &&
                                                    !networkTickerTapesAnnouncementsList.get(i).getMessage().contains("Testing") &&
                                                    !networkTickerTapesAnnouncementsList.get(i).getMessage().contains("maintenance"))) {
                                        ServiceInfo serviceInfo = listOfServices.get(k);
                                        serviceInfo.setServiceStatus(1);
                                        listOfServices.set(k, serviceInfo);
                                        Log.e("compare service matched", servicesAffected[j] + " " + listOfServices.get(k).getServiceNum());
                                    }
                                }
                            }
                        }
                        getServicesRoutesInfo(servicesInfo);
                    }

                    @Override
                    public void onFailureTickerTapesAnnouncements() {
                        Log.e("tickertapes", " failed");
                        for (int i = 0; i < servicesInfo.size(); i++) {
                            ServiceInfo serviceInfo = servicesInfo.get(i);
                            serviceInfo.setServiceStatus(3);
                            servicesInfo.set(i, serviceInfo);
                        }
                        getServicesRoutesInfo(servicesInfo);
                    }
                });
                getServicesRoutesInfo(servicesInfo);
            }

            @Override
            public void onFailureServiceList() {
                //TODO: show failure snackbar
                TextView textView = view.findViewById(R.id.textViewCouldntConnect);
                textView.setText(R.string.failed_to_connect);
            }
        });

    }

    int pullsCompleted = 0;

    private void getServicesRoutesInfo(List<ServiceInfo> servicesInfo) {
        for (int i = 0; i < servicesInfo.size(); i++) {
            int finalI = i;
            NextbusAPIs.callPickupPoint(true, servicesInfo.get(i).getServiceNum(),
                    getActivity(), getContext(), new NextbusAPIs.VolleyCallBackPickupPoint() {
                @Override
                public void OnSuccessPickupPointString(String response) {
                    servicesInfo.get(finalI).setServiceFullRoute(response);
                    pullsCompleted++;
                    if (pullsCompleted == servicesInfo.size()) {
                        loadIntoRecyclerView(servicesInfo);
                    }
                }

                @Override
                public void onSuccessPickupPoint(List<StopList> listOfStopsAlongRoute) {
                    //do nothing
                }

                @Override
                public void onFailurePickupPoint() {
                    loadIntoRecyclerView(servicesInfo);
                }
            });
        }
    }

    private void loadIntoRecyclerView(List<ServiceInfo> servicesInfo) {
        singleServiceCustomAdapterRecyclerView = new SingleServiceCustomAdapterRecyclerView(
                getContext(), servicesInfo, navController);
        nusServicesRecyclerView.setAdapter(singleServiceCustomAdapterRecyclerView);
        progressBar.setVisibility(View.GONE);

    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
}