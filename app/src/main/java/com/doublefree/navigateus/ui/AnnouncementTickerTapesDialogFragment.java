package com.doublefree.navigateus.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementTickerTapesDialogFragment extends DialogFragment {

    public static String TAG = "AnnouncementTickerTapesDialogFragment";

    boolean isTickerTapes;

    ProgressBar progressBar;
    RecyclerView announcementRecyclerView;
    LinearLayoutManager llm;

    List<NetworkTickerTapesAnnouncements> list;
    String specificService;

    View view;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        if (isTickerTapes) {
            view = inflater.inflate(R.layout.fragment_tickertapes, null);
        } else {
            view = inflater.inflate(R.layout.fragment_annoucement, null);
        }

        builder.setView(view).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        progressBar = view.findViewById(R.id.loadingBarForAnnouncement);
        announcementRecyclerView = view.findViewById(R.id.announcementRecyclerView);
        announcementRecyclerView.setClickable(false);
        llm = new LinearLayoutManager(getContext());
        announcementRecyclerView.setLayoutManager(llm);

        loadAnnouncements(view);

        return builder.create();
    }

    private void setRecyclerView(List<NetworkTickerTapesAnnouncements> list) {
        announcementRecyclerView.setVisibility(View.VISIBLE);
        AnnouncementCustomRecyclerViewAdapter adapter = new AnnouncementCustomRecyclerViewAdapter(getActivity(), getContext(), list);
        announcementRecyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private List<NetworkTickerTapesAnnouncements> filterTickerTapes(List<NetworkTickerTapesAnnouncements> list) {

        List<NetworkTickerTapesAnnouncements> newList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String[] servicesAffected = list.get(i).getServicesAffected().split(",");
            for (int j = 0; j < servicesAffected.length && !servicesAffected[j].isEmpty(); j++) {
                if (servicesAffected[j].trim().contains(specificService) || specificService.contains(servicesAffected[j].trim())) {
                    newList.add(list.get(i));
                }
            }
        }

        return newList;

    }

    private void loadAnnouncements(View view) {

        TextView title = view.findViewById(R.id.ServiceTimetableTitle);

        if (isTickerTapes) {
            title.setText("Service Alerts");
            if (list != null && list.size() > 0) {
                setRecyclerView(list);
            } else {
                NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
                    @Override
                    public void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList) {
                        List<NetworkTickerTapesAnnouncements> newList = filterTickerTapes(networkTickerTapesAnnouncementsList);
                        setRecyclerView(newList);
                    }

                    @Override
                    public void onFailureTickerTapesAnnouncements() {
                        TextView failed = view.findViewById(R.id.textViewAnnoucementConnectionFailed);
                        progressBar.setVisibility(View.GONE);
                        failed.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            title.setText("Announcements");
            NextbusAPIs.callListOfAnnouncements(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
                @Override
                public void onSuccessTickerTapesAnnouncements(List<NetworkTickerTapesAnnouncements> networkTickerTapesAnnouncementsList) {
                    announcementRecyclerView.setVisibility(View.VISIBLE);
                    AnnouncementCustomRecyclerViewAdapter adapter = new AnnouncementCustomRecyclerViewAdapter(getActivity(), getContext(), networkTickerTapesAnnouncementsList);
                    announcementRecyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailureTickerTapesAnnouncements() {
                    TextView failed = view.findViewById(R.id.textViewAnnoucementConnectionFailed);
                    progressBar.setVisibility(View.GONE);
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    public static AnnouncementTickerTapesDialogFragment newInstance(boolean isTickerTapes, List<NetworkTickerTapesAnnouncements> list, String specificService) {
        AnnouncementTickerTapesDialogFragment dialogFragment = new AnnouncementTickerTapesDialogFragment();
        dialogFragment.setTickerTapes(isTickerTapes);
        dialogFragment.setList(list);
        dialogFragment.setSpecificService(specificService);
        return dialogFragment;
    }


    private void setTickerTapes(boolean tickerTapes) {
        isTickerTapes = tickerTapes;
    }

    private void setList(List<NetworkTickerTapesAnnouncements> list) {
        this.list = list;
    }

    private void setSpecificService(String specificService) {
        this.specificService = specificService;
    }


}
