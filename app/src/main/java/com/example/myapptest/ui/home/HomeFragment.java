package com.example.myapptest.ui.home;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.google.android.material.appbar.AppBarLayout;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        setRetainInstance(true);
    }

//    private HomeViewModel homeViewModel;
//    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
//        Toolbar homeToolbar = view.findViewById(R.id.home_toolbar);
//        homeToolbar.setTitle("Home");
//        ((AppCompatActivity)getActivity()).setSupportActionBar(homeToolbar);
//        homeToolbar.setTitleTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.white));
//        setHasOptionsMenu(true);
//        homeToolbar.setTitle("Home");

//        ActionBar actionBar = getActivity().getActionBar();
        setHasOptionsMenu(true);

        return view;

    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        menu.clear();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar_menu, menu);
    }

//        homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);
//
//        binding = FragmentHomeBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;


    String firstPassStopsList;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.home_searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText search = (EditText) view.findViewById(R.id.home_search);
                Log.d("search", search.getText().toString());

            }
        });

        getStringOfGroupStops(view);

    }

    private void getStringOfGroupStops(View view) {

        TextView textView = view.findViewById(R.id.textView_timingTest);
        String url = "https://nnextbus.nus.edu.sg/BusStops";
        String auth = "Basic TlVTbmV4dGJ1czoxM2RMP3pZLDNmZVdSXiJU";

        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        firstPassStopsList = response;
                        Log.d("response is", response);
//                        textView.setText("Response: " + response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("volley API error", "" + error);
                    }


                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", auth);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext().getApplicationContext());
        requestQueue.add(stringRequest);

    }


//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
}