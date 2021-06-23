package com.example.myapptest.ui.stops_services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

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
    Integer timeToWatch;
//    ArrivalNotificationsDialogListener listener;
    ArrivalNotificationsDialogListenerForActivity listenerForActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_set_arrival_notifications, null);

        TextView arrivalNotificationsStopName = view.findViewById(R.id.arrivalNotificationsStopName);
        arrivalNotificationsStopName.setText(singleStopArrivalNotifications.getStopName());

        selectTimeSpinner = (Spinner) view.findViewById(R.id.chooseTimeSpinner);
        Integer[] list = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        Log.e("list is:", list + "");
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Log.e("adapter is", adapter + "");
        Log.e("setselection is:", singleStopArrivalNotifications.getTimeToWatch() - 1 + "");
        selectTimeSpinner.setAdapter(adapter);
        selectTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeToWatch = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ChipGroup servicesChipGroup = view.findViewById(R.id.setServicesToWatchChipGroup);

//        String[] list = this.getResources().getStringArray(R.array.chooseTimeSpinner_array);

        builder.setView(view)
//                .setTitle(singleStopArrivalNotifications.getStopName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //positive button action
                        if (singleStopArrivalNotifications.isWatchingForArrival()) {
                            singleStopArrivalNotifications.setTimeToWatch(timeToWatch);
                            List<String> servicesBeingWatched = new ArrayList<>();
                            for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
                                Chip chip = (Chip) servicesChipGroup.getChildAt(i);
                                if (chip.isChecked()) {
                                    servicesBeingWatched.add((String) chip.getText());
                                    Log.e("ChipText is", chip.getText() + "");
                                }
                            }
                            singleStopArrivalNotifications.setServicesBeingWatched(servicesBeingWatched);

                        }
                        Log.e("timeToWatch is:", timeToWatch + "");

                        listenerForActivity.onDialogPositiveClick(singleStopArrivalNotifications);
//                        listener.onDialogPositiveClick(singleStopArrivalNotifications);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        View containerForSettings = view.findViewById(R.id.containerForChipGroup);


        SwitchMaterial switchActivateWatch = view.findViewById(R.id.switch_activateWatch);
        Log.e("ssan watch", singleStopArrivalNotifications.isWatchingForArrival() + "");
        switchActivateWatch.setChecked(singleStopArrivalNotifications.isWatchingForArrival());
        if (!singleStopArrivalNotifications.isWatchingForArrival()) {
            containerForSettings.setVisibility(View.GONE);
        } else {
            selectTimeSpinner.setSelection(singleStopArrivalNotifications.getTimeToWatch() - 1);
        }
        switchActivateWatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    singleStopArrivalNotifications.setWatchingForArrival(true);
                    if (singleStopArrivalNotifications.getTimeToWatch() > 0) {
                        selectTimeSpinner.setSelection(singleStopArrivalNotifications.getTimeToWatch() - 1);
                    }
                    containerForSettings.setVisibility(View.VISIBLE);
                } else {
                    singleStopArrivalNotifications.setWatchingForArrival(false);
                    containerForSettings.setVisibility(View.GONE);
                }
            }
        });

        //TODO: implement listener for MaterialSwitch, to only populate chips when switch is checked.
        addChips(servicesChipGroup);

        return builder.create();
    }

    public void setSingleStopArrivalNotifications(ArrivalNotifications singleStopArrivalNotifications) {
        this.singleStopArrivalNotifications = singleStopArrivalNotifications;
        Log.e("singleStopArrivalNotifications:", singleStopArrivalNotifications + "");
        Log.e("testtest", singleStopArrivalNotifications.getServicesAtStop() + "");
    }

    private void addChips(ChipGroup servicesChipGroup) {
        for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_arrival, servicesChipGroup, false);
                chip.setText(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum());
//                chip.setCheckedIconVisible(true);
                boolean chipChecked = false;
                for (int j = 0; singleStopArrivalNotifications.getServicesBeingWatched() != null
                        && j < singleStopArrivalNotifications.getServicesBeingWatched().size() && !chipChecked; j++) {
                    if (singleStopArrivalNotifications.getServicesBeingWatched().get(j)
                            .equals(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum())) {
                        chip.setChecked(singleStopArrivalNotifications.getServicesBeingWatched().get(j)
                                .equals(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum()));
                        chipChecked = true;
                    }
                }
                chip.setEnsureMinTouchTargetSize(false);
                chip.setCheckable(true);
//                chip.setTextColor(ContextCompat.getColor(this.getContext(), R.color.black));
                chip.setCheckedIconTintResource(R.color.NUS_Orange);
                servicesChipGroup.addView(chip);
        }
    }
//
//    public interface ArrivalNotificationsDialogListener {
//        public void onDialogPositiveClick(ArrivalNotifications singleStopArrivalNotifications);
//        public void onDialogNegativeClick();
//    }

    public interface ArrivalNotificationsDialogListenerForActivity {
        public void onDialogPositiveClick(ArrivalNotifications singleStopArrivalNotifications);
        public void onDialogNegativeClick();
    }

//    public void setArrivalNotificationsDialogListener(ArrivalNotificationsDialogListener listener) {
//        this.listener = listener;
//    }

//     Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
//            listener = (ArrivalNotificationsDialogListener) getParentFragment();
            listenerForActivity = (ArrivalNotificationsDialogListenerForActivity) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DialogListener");
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (getTargetFragment() != null) {
//             = (ArrivalNotificationsDialogListener) getTargetFragment();
//        } else {
//            myInterface = (YourCastInterface) activity;
//        }
//    }

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