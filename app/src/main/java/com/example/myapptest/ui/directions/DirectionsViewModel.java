package com.example.myapptest.ui.directions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DirectionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DirectionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Results will appear here");
    }

    public LiveData<String> getText() {
        return mText;
    }
}