package com.example.opentoktesting;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class MyApplication extends Application {
    public static MutableLiveData<Boolean> myLiveData;

    @Override
    public void onCreate() {
        super.onCreate();
        myLiveData = new MutableLiveData<>();
    }


    public static MutableLiveData<Boolean> getMyLiveData() {
        if(myLiveData == null) {
            myLiveData = new MutableLiveData<>();
        }
        return myLiveData;
    }
}
