package com.example.myapptest.ui.directions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.naviagationdata.NavigationSearchInfo;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link DirectionsResultFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class DirectionsResultFragment extends Fragment {

    private String origin;
    private String dest;

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

        navigationSearchInfo = ((MainActivity) getActivity()).getNavigationSearchInfo();
        Log.e("origin - dest is", navigationSearchInfo.getOrigin() + " " + navigationSearchInfo.getDest());

        View view = inflater.inflate(R.layout.fragment_directions_result, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }
}