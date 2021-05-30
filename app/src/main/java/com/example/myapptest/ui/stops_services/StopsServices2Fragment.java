package com.example.myapptest.ui.stops_services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapptest.R;
import com.example.myapptest.databinding.FragmentStopsServices2Binding;

public class StopsServices2Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stops_services2, container, false);
    }

//    private StopsServices2ViewModel stopsServices2ViewModel;
//    private FragmentStopsServices2Binding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        stopsServices2ViewModel =
//                new ViewModelProvider(this).get(StopsServices2ViewModel.class);
//
//        binding = FragmentStopsServices2Binding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textStopsServices2;
//        stopsServices2ViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;
//    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
}