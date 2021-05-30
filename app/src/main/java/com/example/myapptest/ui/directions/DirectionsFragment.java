package com.example.myapptest.ui.directions;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapptest.R;
import com.example.myapptest.databinding.FragmentDirectionsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class DirectionsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_directions, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                EditText originView = (EditText) view.findViewById(R.id.originInputEditor);
                EditText destView = (EditText) view.findViewById(R.id.destInputEditor);

                String originText = originView.getText().toString();
                String destText = destView.getText().toString();

                Log.d("originText", originText);
                Log.d("destText", destText);

                if (originText.trim().length() > 0 && destText.trim().length() > 0) {

                    CheckBox sheltered = (CheckBox) view.findViewById(R.id.checkbox_sheltered);
                    CheckBox accessible = (CheckBox) view.findViewById(R.id.checkbox_accessible);
                    CheckBox internalOnly = (CheckBox) view.findViewById(R.id.checkbox_internalOnly);

                    //continue whatever to pass to DirectionsResultFragment

                } else {
//                    Toast directionsInputEmptyToast = Toast.makeText(getActivity(), "Please check that you've entered both the origin and destination", Toast.LENGTH_LONG);
//                    directionsInputEmptyToast.show();
                    Snackbar snackbar = Snackbar.make(view,
                            R.string.directions_input_error,
                            Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(R.id.nav_view);
                    snackbar.show();
                }


            }
        });

    }



}