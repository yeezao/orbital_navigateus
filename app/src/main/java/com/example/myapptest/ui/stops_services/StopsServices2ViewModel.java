package com.example.myapptest.ui.stops_services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopsServices2ViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StopsServices2ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Stops & Services (Services) fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}