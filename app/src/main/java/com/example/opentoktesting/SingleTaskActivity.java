package com.example.opentoktesting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.example.opentoktesting.databinding.ActivitySingleTaskBinding;

public class SingleTaskActivity extends AppCompatActivity {
    private CountDownTimer timer;
    private ActivitySingleTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getMyLiveData().postValue(true);
        Log.d("RequestX", "onCreate");
        binding = ActivitySingleTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvTimer.setText(String.valueOf(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                Log.d("RequestX", "onFinish ofd timer");
            }
        };
        timer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(isInPictureInPictureMode())
                Log.d("RequestX", "apuse = in pip");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(isInPictureInPictureMode())
                Log.d("RequestX", "res = in pip");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getMyLiveData().postValue(false);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("RequestX", "onNewIntent");
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        Log.d("RequestX", "onPip");
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }
}