package com.example.opentoktesting;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.example.opentoktesting.databinding.ActivitySecondBinding;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class SecondActivity extends AppCompatActivity {
    private ActivitySecondBinding binding;
    private NotificationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecondBinding.inflate(getLayoutInflater());

        MyApplication.getMyLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    binding.textView.setVisibility(View.VISIBLE);
                } else {
                    binding.textView.setVisibility(View.GONE);
                }
            }
        });

        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("AppConstants.channelId", "AppConstants.notificationChannelServices", NotificationManager.IMPORTANCE_DEFAULT);

            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            chan.setSound(null, null);
            manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            Intent intent = new Intent(this, SingleTaskActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "AppConstants.channelId");
            mBuilder.setContentTitle("Lybrate").setSmallIcon(R.drawable.ic_launcher_foreground);
            mBuilder.setProgress(0, 0, true);
            mBuilder.setColor(ContextCompat.getColor(this, R.color.container_bg));
            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();

            manager.notify(0, notification);
        }
        binding.textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SingleTaskActivity.class);
            startActivity(intent);
        });

        String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        EasyPermissions.requestPermissions(this, "getString(R.string.rationale_video_app)", 1, perms);

        Location location = new Location(LocationManager.GPS_PROVIDER);
        Log.d("RequestX", "bf lat = " + location.getLatitude());
        location.setLatitude(28.401043);
        location.setLongitude(77.103890);
        getAddressFromLocation(location, this, new GeocoderHandler());
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            // replace by what you need to do
            binding.textView.setText(result);
        }
    }

    public static void getAddressFromLocation(final Location location, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e("RequestX", "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}