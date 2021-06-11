package com.example.myapptest.ui.directions;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.naviagationdata.NavigationSearchInfo;
import com.example.myapptest.databinding.FragmentDirectionsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jayway.jsonpath.JsonPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionsFragment extends Fragment {

    String listOfBusStopsString;
    AppCompatAutoCompleteTextView destInputEditor;
    AppCompatAutoCompleteTextView originInputEditor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        destInputEditor = view.findViewById(R.id.destInputEditor);
        originInputEditor = view.findViewById(R.id.originInputEditor);
        listOfBusStopsString = ((MainActivity) getActivity()).getFirstPassStopsList();
        if (listOfBusStopsString != null) {
            SetAutoFillAdapter();
        } else {
            GetBusStopsListOnline();
        }

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        menu.clear();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Directions");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar_menu, menu);
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageSearchInfo(view);
            }
        });

        destInputEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getKeyCode() == KeyEvent.ACTION_DOWN))) {
                    PackageSearchInfo(view);
                }
                return false;
            }
        });

    }

    private void SetAutoFillAdapter() {
        List<String> listOfNames = JsonPath.read(listOfBusStopsString, "$.BusStopsResult.busstops[*].caption");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, listOfNames);
        Log.e("arrayadapter is", arrayAdapter + "");
        destInputEditor.setAdapter(arrayAdapter);
        originInputEditor.setAdapter(arrayAdapter);
    }

    private void GetBusStopsListOnline() {
        String url = "https://nnextbus.nus.edu.sg/BusStops";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                listOfBusStopsString = response;
                ((MainActivity) getActivity()).setFirstPassStopsList(listOfBusStopsString);
                SetAutoFillAdapter();
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
                params.put("Authorization", getActivity().getString(R.string.auth_header));
                return params;
            }
        };

        if (this.getContext() != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
            requestQueue.add(stringRequest);
        }
    }

    private void PackageSearchInfo(View view) {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            //do nothing, just move on
        }

        String originText = originInputEditor.getText().toString();
        String destText = destInputEditor.getText().toString();

        Log.d("originText", originText);
        Log.d("destText", destText);

        if (originText.trim().length() > 0 && destText.trim().length() > 0) {

            NavigationSearchInfo navigationSearchInfo = new NavigationSearchInfo();

            Log.e("origintext is", originText);
            Log.e("destText is", destText);

            navigationSearchInfo.setOrigin(originText);
            navigationSearchInfo.setDest(destText);

            CheckBox sheltered = (CheckBox) view.findViewById(R.id.checkbox_sheltered);
            CheckBox accessible = (CheckBox) view.findViewById(R.id.checkbox_accessible);
            CheckBox internalOnly = (CheckBox) view.findViewById(R.id.checkbox_internalOnly);

            navigationSearchInfo.setSheltered(sheltered.isChecked());
            navigationSearchInfo.setBarrierFree(accessible.isChecked());
            navigationSearchInfo.setInternalBusOnly(internalOnly.isChecked());

            ((MainActivity) getActivity()).setNavigationSearchInfo(navigationSearchInfo);

            ProgressBar waitingForDirectionsResultProgressBar = view.findViewById(R.id.waiting_for_directions_result_progressBar);
            waitingForDirectionsResultProgressBar.setVisibility(View.VISIBLE);

            //TODO: call navigation function by passing in navigationSearchInfo

        } else {
            Snackbar snackbar = Snackbar.make(view,
                    R.string.directions_input_error,
                    Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.nav_view);
            snackbar.show();
        }
    }

}