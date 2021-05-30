package com.example.myapptest.ui.stops_services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopsServicesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StopsServicesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Stops & Services (Stops) fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}