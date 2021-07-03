package com.example.myapptest.ui.directions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.example.myapptest.data.naviagationdata.NavigationSearchInfo;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link DirectionsResultFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class DirectionsResultFragment extends Fragment {

    NavigationResults singleNavResult;
    String origin;
    String dest;

//    public DirectionsResultFragment() {
//        // Required empty public constructor
//    }
//

//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    NavigationSearchInfo navigationSearchInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_directions_result, container, false);
        TextView originTextBox = view.findViewById(R.id.textView_origin_resultList);
        TextView destTextBox = view.findViewById(R.id.textView_dest_resultList);
        origin = ((MainActivity) getActivity()).getOrigin();
        dest = ((MainActivity) getActivity()).getDest();
        originTextBox.setText(origin);
        destTextBox.setText(dest);
        singleNavResult = ((MainActivity) getActivity()).getNavResultSingle();

        this.setHasOptionsMenu(true);

//        NavigationResults navList = DirectionsResultFragmentArgs.fromBundle(getArguments()).getGetListSingleRoute();

        // Inflate the layout for this fragment
        return view;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float dpWidth = displayMetrics.widthPixels;

        RecyclerView singleResultRecyclerView = view.findViewById(R.id.singleresultrecyclerView);
        singleResultRecyclerView.setClickable(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        singleResultRecyclerView.setLayoutManager(linearLayoutManager);
        SingleRouteListCustomAdapterRecyclerView singleRouteListCustomAdapterRecyclerView =
                new SingleRouteListCustomAdapterRecyclerView(getActivity(), getContext(), singleNavResult, origin, dest, this.getChildFragmentManager(), dpWidth);
        singleResultRecyclerView.setAdapter(singleRouteListCustomAdapterRecyclerView);

//        waitingForDirectionsResultProgressBar.setVisibility(View.GONE);
//        singleResultRecyclerView.setVisibility(View.VISIBLE);


    }

}