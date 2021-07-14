package com.example.myapptest.ui.stops_services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SetArrivalNotificationsDialogFragment extends DialogFragment {

    public static String TAG = "SetArrivalNotificationsDialogFragment";

    Spinner selectTimeSpinner;

    ArrivalNotifications singleStopArrivalNotifications;
    Integer timeToWatch;
    ArrivalNotificationsDialogListenerForActivity listenerForActivity;

    float dpWidth;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_set_arrival_notifications, null);

        TextView arrivalNotificationsStopName = view.findViewById(R.id.AnnouncementTitle);
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

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        dpWidth = displayMetrics.widthPixels;

        View containerForSettings = view.findViewById(R.id.containerForChipGroup);
        View containerForFavourites = view.findViewById(R.id.containerForFavouritesChipGroup);
        ChipGroup servicesChipGroup = view.findViewById(R.id.setServicesForAlertsChipGroup);
        ChipGroup favouritesChipGroup = view.findViewById(R.id.setFavouritesChipGroup);

        builder.setView(view)
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
                        if (singleStopArrivalNotifications.isFavourite()) {
                            boolean anyServicesChecked = false;
                            List<String> servicesToFavourite = new ArrayList<>();
                            for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
                                Chip chip = (Chip) favouritesChipGroup.getChildAt(i);
                                if (chip.isChecked()) {
                                    servicesToFavourite.add((String) chip.getText());
                                    anyServicesChecked = true;
                                }
                            }
                            if (!anyServicesChecked) {
                                for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
                                    Chip chip = (Chip) favouritesChipGroup.getChildAt(i);
                                    servicesToFavourite.add((String) chip.getText());
                                }
                            }
                            singleStopArrivalNotifications.setServicesFavourited(servicesToFavourite);
                        }
                        Log.e("timeToWatch is:", timeToWatch + "");

                        listenerForActivity.onDialogPositiveClick(singleStopArrivalNotifications);
//                        listener.onDialogPositiveClick(singleStopArrivalNotifications);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog, therefore do nothing
                    }
                });

        
        SwitchMaterial switchActivateWatch = view.findViewById(R.id.switch_activateWatch);
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

        SwitchMaterial switchActivateFavourites = view.findViewById(R.id.switch_activateFavourites);
        switchActivateFavourites.setChecked(singleStopArrivalNotifications.isFavourite());
        if (!singleStopArrivalNotifications.isFavourite()) {
            containerForFavourites.setVisibility(View.GONE);
        }
        switchActivateFavourites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    singleStopArrivalNotifications.setFavourite(true);
                    containerForFavourites.setVisibility(View.VISIBLE);
                    if (dpWidth > 700) {
                    }
                } else {
                    singleStopArrivalNotifications.setFavourite(false);
                    containerForFavourites.setVisibility(View.GONE);
                }
            }
        });

        addChipsAlerts(servicesChipGroup);
        addChipsFavourites(favouritesChipGroup);

        return builder.create();
    }

    public void setSingleStopArrivalNotifications(ArrivalNotifications singleStopArrivalNotifications) {
        this.singleStopArrivalNotifications = singleStopArrivalNotifications;
    }

    //TODO: combine the 2 methods below into 1 (if feasible)
    private void addChipsFavourites(ChipGroup favouritesChipGroup) {
        for (int i = 0; i < singleStopArrivalNotifications.getServicesAtStop().size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_arrival, favouritesChipGroup, false);
            chip.setText(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum());
//                chip.setCheckedIconVisible(true);
            boolean chipChecked = false;
            for (int j = 0; singleStopArrivalNotifications.getServicesFavourited() != null
                    && j < singleStopArrivalNotifications.getServicesFavourited().size() && !chipChecked; j++) {
                if (singleStopArrivalNotifications.getServicesFavourited().get(j)
                        .equals(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum())) {
                    chip.setChecked(singleStopArrivalNotifications.getServicesFavourited().get(j)
                            .equals(singleStopArrivalNotifications.getServicesAtStop().get(i).getServiceNum()));
                    chipChecked = true;
                }
            }
            chip.setEnsureMinTouchTargetSize(false);
            chip.setCheckable(true);
//                chip.setTextColor(ContextCompat.getColor(this.getContext(), R.color.black));
            chip.setCheckedIconTintResource(R.color.NUS_Orange);
            favouritesChipGroup.addView(chip);
        }
    }

    private void addChipsAlerts(ChipGroup servicesChipGroup) {
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