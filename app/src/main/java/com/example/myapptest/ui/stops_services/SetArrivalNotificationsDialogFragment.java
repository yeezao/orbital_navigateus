package com.example.myapptest.ui.stops_services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetArrivalNotificationsDialogFragment#} factory method to
 * create an instance of this fragment.
 */
public class SetArrivalNotificationsDialogFragment extends DialogFragment {

    public static String TAG = "SetArrivalNotificationsDialogFragment";

    Spinner selectTimeSpinner;
    
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        View view = inflater.inflate(R.layout.fragment_set_arrival_notifications, container, false);
//
//
//
//        return view;
//    }

    ArrivalNotifications singleStopArrivalNotifications;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_set_arrival_notifications, null);
        selectTimeSpinner = (Spinner) view.findViewById(R.id.chooseTimeSpinner);
        TextView arrivalNotificationsStopName = view.findViewById(R.id.arrivalNotificationsStopName);
        arrivalNotificationsStopName.setText(singleStopArrivalNotifications.getStopName());

//        String[] list = this.getResources().getStringArray(R.array.chooseTimeSpinner_array);
        Integer[] list = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Log.e("list is:", list + "");
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Log.e("adapter is", adapter + "");
        selectTimeSpinner.setAdapter(adapter);

        builder.setView(view)
//                .setTitle(singleStopArrivalNotifications.getStopName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //positive button action
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        ChipGroup servicesChipGroup = view.findViewById(R.id.setServicesToWatchChipGroup);
        //TODO: implement listener for MaterialSwitch, to only populate chips when switch is checked.
        addChips(servicesChipGroup);

        return builder.create();
    }

    public void setSingleStopArrivalNotifications(ArrivalNotifications singleStopArrivalNotifications) {
        this.singleStopArrivalNotifications = singleStopArrivalNotifications;
        Log.e("singleStopArrivalNotifications:", singleStopArrivalNotifications + "");
    }

    private void addChips(ChipGroup servicesChipGroup) {
        for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_arrival, servicesChipGroup, false);
                chip.setText(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum());
                chip.setCheckedIconVisible(true);
                chip.setChecked(false);
                chip.setEnsureMinTouchTargetSize(false);
                chip.setCheckable(true);
                chip.setTextColor(ContextCompat.getColor(this.getContext(), R.color.black));
                chip.setCheckedIconTintResource(R.color.NUS_Orange);
                servicesChipGroup.addView(chip);
        }
    }



//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;
//
//    public SetArrivalNotificationsDialogFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment SetArrivalNotificationsDialogFragment.
//     */
    public static SetArrivalNotificationsDialogFragment newInstance(ArrivalNotifications singleStopArrivalNotifications) {
        SetArrivalNotificationsDialogFragment dialogFragment = new SetArrivalNotificationsDialogFragment();
        Bundle args = new Bundle();
//        args.putInt("count", arg);
        dialogFragment.setArguments(args);
        dialogFragment.setSingleStopArrivalNotifications(singleStopArrivalNotifications);
        return dialogFragment;
    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_set_arrival_notifications, container, false);
//    }
}