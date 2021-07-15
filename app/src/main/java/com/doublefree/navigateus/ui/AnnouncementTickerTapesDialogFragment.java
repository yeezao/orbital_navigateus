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

import java.util.List;

public class AnnouncementTickerTapesDialogFragment extends DialogFragment {

    public static String TAG = "AnnouncementTickerTapesDialogFragment";

    boolean isTickerTapes;

    ProgressBar progressBar;
    RecyclerView announcementRecyclerView;
    LinearLayoutManager llm;

    View view;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_annoucement_tickertapes, null);

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

    private void loadAnnouncements(View view) {

        TextView title = view.findViewById(R.id.AnnouncementTitle);

        if (isTickerTapes) {
            title.setText("Service Alerts");
            NextbusAPIs.callListOfTickerTapes(getActivity(), getContext(), new NextbusAPIs.VolleyCallBackTickerTapesAnnouncementsList() {
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

    public static AnnouncementTickerTapesDialogFragment newInstance(boolean isTickerTapes) {
        AnnouncementTickerTapesDialogFragment dialogFragment = new AnnouncementTickerTapesDialogFragment();
        dialogFragment.setTickerTapes(isTickerTapes);
        return dialogFragment;
    }


    public void setTickerTapes(boolean tickerTapes) {
        isTickerTapes = tickerTapes;
    }


}
