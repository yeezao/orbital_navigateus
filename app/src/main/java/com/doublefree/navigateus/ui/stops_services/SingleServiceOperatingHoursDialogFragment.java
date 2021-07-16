package com.doublefree.navigateus.ui.stops_services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.doublefree.navigateus.data.busrouteinformation.BusOperatingHours;
import com.doublefree.navigateus.ui.AnnouncementCustomRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SingleServiceOperatingHoursDialogFragment extends DialogFragment {

    public static String TAG = "SingleServiceOperatingHoursDialogFragment";

    List<BusOperatingHours> list;
    String service;
    ConstraintLayout timetable;

    View view;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_single_service_timetable, null);

        timetable = view.findViewById(R.id.timetable_master_layout);
        timetable.setVisibility(View.INVISIBLE);

        TextView title = view.findViewById(R.id.ServiceTimetableTitle);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Service ").append(service);
        title.setText(stringBuilder.toString());


        builder.setView(view).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        checkTimetableLoaded();

        return builder.create();
    }

    private void checkTimetableLoaded() {
        if (list.isEmpty()) {
            NextbusAPIs.callBusOperatingHours(getActivity(), getContext(), service, new NextbusAPIs.VolleyCallBackOperatingHours() {
                @Override
                public void onSuccessOperatingHours(List<BusOperatingHours> pulledList) {
                    list = pulledList;
                    displayTimetable(pulledList);
                }

                @Override
                public void onFailureOperatingHours() {

                }
            });
        } else {
            displayTimetable(list);
        }
    }

    private void displayTimetable(List<BusOperatingHours> list) {
        timetable.setVisibility(View.VISIBLE);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getScheduleType().equals("Term")) {
                String dayType = list.get(i).getDayType();
                if (dayType.equals("Mon-Fri")) {
                    TextView firstBus = view.findViewById(R.id.termweekdayfirstbus);
                    TextView lastBus = view.findViewById(R.id.termweekdaylastbus);
                    TextView noBus = view.findViewById(R.id.termweekdaynobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());
                } else if (dayType.equals("Sat")) {
                    TextView firstBus = view.findViewById(R.id.termsatfirstbus);
                    TextView lastBus = view.findViewById(R.id.termsatlastbus);
                    TextView noBus = view.findViewById(R.id.termsatnobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());                    
                } else if (dayType.contains("Sun") && dayType.contains("PH")) {
                    TextView firstBus = view.findViewById(R.id.termsunfirstbus);
                    TextView lastBus = view.findViewById(R.id.termsunlastbus);
                    TextView noBus = view.findViewById(R.id.termsunnobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());
                }
            } else if (list.get(i).getScheduleType().equals("Vacation")) {
                String dayType = list.get(i).getDayType();
                if (dayType.equals("Mon-Fri")) {
                    TextView firstBus = view.findViewById(R.id.vacayweekdayfirstbus);
                    TextView lastBus = view.findViewById(R.id.vacayweekdaylastbus);
                    TextView noBus = view.findViewById(R.id.vacayweekdaynobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());
                } else if (dayType.equals("Sat")) {
                    TextView firstBus = view.findViewById(R.id.vacaysatfirstbus);
                    TextView lastBus = view.findViewById(R.id.vacaysatlastbus);
                    TextView noBus = view.findViewById(R.id.vacaysatnobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());
                } else if (dayType.contains("Sun") && dayType.contains("PH")) {
                    TextView firstBus = view.findViewById(R.id.vacaysunfirstbus);
                    TextView lastBus = view.findViewById(R.id.vacaysunlastbus);
                    TextView noBus = view.findViewById(R.id.vacaysunnobus);
                    noBus.setVisibility(View.INVISIBLE);
                    firstBus.setText(list.get(i).getFirstTime());
                    lastBus.setText(list.get(i).getLastTime());
                }
            }
        }
    }


    public static SingleServiceOperatingHoursDialogFragment newInstance(List<BusOperatingHours> list, String service) {
        SingleServiceOperatingHoursDialogFragment dialogFragment = new SingleServiceOperatingHoursDialogFragment();
        dialogFragment.setService(service);
        dialogFragment.setList(list);
        return dialogFragment;
    }

    private void setService(String service) {
        this.service = service;
    }

    private void setList(List<BusOperatingHours> list) {
        this.list = list;
    }


}
