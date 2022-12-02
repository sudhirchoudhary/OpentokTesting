package com.example.opentoktesting;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opentok.android.AudioDeviceManager;
import com.opentok.android.BaseAudioDevice;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 124;

    private Session session;
    private Publisher publisher;
    private Subscriber subscriber;

    private FrameLayout subscriberViewContainer;
    private FrameLayout publisherViewContainer;
    private Button pictureInPictureButton;

    private PublisherKit.PublisherListener publisherListener = new PublisherKit.PublisherListener() {
        @Override
        public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
            Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId());
        }

        @Override
        public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
            Log.d(TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId());
        }

        @Override
        public void onError(PublisherKit publisherKit, OpentokError opentokError) {
            finishWithMessage("PublisherKit onError: " + opentokError.getMessage());
        }
    };

    /*private Session.SessionListener sessionListener = new Session.SessionListener() {
        @Override
        public void onConnected(Session session) {
            Log.d(TAG, "Session connected");

            if (publisher == null) {
                publisher = new Publisher.Builder(getApplicationContext()).build();
                session.publish(publisher);

                publisherViewContainer.addView(publisher.getView());

                if (publisher.getView() instanceof GLSurfaceView) {
                    ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
                }
            }
        }

        @Override
        public void onDisconnected(Session session) {
        }

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            if (subscriber == null) {
                subscriber = new Subscriber.Builder(getApplicationContext(), stream).build();
                session.subscribe(subscriber);
                subscriberViewContainer.addView(subscriber.getView());
            } else {
                Log.d(TAG, "This sample supports just one subscriber");
            }
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) {
            subscriberViewContainer.removeAllViews();
            subscriber = null;
        }

        @Override
        public void onError(Session session, OpentokError opentokError) {
            finishWithMessage("Session error: " + opentokError.getMessage());
        }
    };*/

    private SubscriberKit.SubscriberListener subscriberListener = new SubscriberKit.SubscriberListener() {
        @Override
        public void onConnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onConnected: Subscriber connected. Stream: " + subscriberKit.getStream().getStreamId());
        }

        @Override
        public void onDisconnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.getStream().getStreamId());
        }

        @Override
        public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
            finishWithMessage("SubscriberKit onError: " + opentokError.getMessage());
        }
    };

    private Session.SessionListener sessionListener = new Session.SessionListener() {
        @Override
        public void onConnected(Session session) {
            Log.d(TAG, "onConnected: Connected to session: " + session.getSessionId());

            publisher = new Publisher.Builder(MainActivity.this).build();
            publisher.setPublisherListener(publisherListener);
            publisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

            publisherViewContainer.addView(publisher.getView());

            if (publisher.getView() instanceof GLSurfaceView) {
                ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
            }

            session.publish(publisher);
        }

        @Override
        public void onDisconnected(Session session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: " + session.getSessionId());
        }

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            Log.d(TAG, "onStreamReceived: New Stream Received " + stream.getStreamId() + " in session: " + session.getSessionId());

            if (subscriber == null) {
                subscriber = new Subscriber.Builder(MainActivity.this, stream).build();
                subscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                subscriber.setSubscriberListener(subscriberListener);
                session.subscribe(subscriber);
                subscriberViewContainer.addView(subscriber.getView());
            }
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) {
            Log.d(TAG, "onStreamDropped: Stream Dropped: " + stream.getStreamId() + " in session: " + session.getSessionId());

            if (subscriber != null) {
                subscriber = null;
                subscriberViewContainer.removeAllViews();
            }
        }

        @Override
        public void onError(Session session, OpentokError opentokError) {
            finishWithMessage("Session error: " + opentokError.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscriberViewContainer = findViewById(R.id.subscriber_container);
        publisherViewContainer = findViewById(R.id.publisher_container);
        pictureInPictureButton = findViewById(R.id.picture_in_picture_button);

        pictureInPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureInPictureParams params = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    params = new PictureInPictureParams.Builder()
                            .setAspectRatio(new Rational(publisherViewContainer.getWidth(), publisherViewContainer.getHeight())) // Portrait Aspect Ratio
                            .build();
                    enterPictureInPictureMode(params);
                }
            }
        });

        String[] perms;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        } else {
            perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        }
        EasyPermissions.requestPermissions(this, "getString(R.string.rationale_video_app)", PERMISSIONS_REQUEST_CODE, perms);

        if(AudioDeviceManager.getAudioDevice() == null) {
            AdvancedAudioDevice advancedAudioDevice = new AdvancedAudioDevice(this, null);
            AudioDeviceManager.setAudioDevice(advancedAudioDevice);
        }

        if (session == null) {
            session = new Session.Builder(getApplicationContext(), Constants.API_KEY, Constants.SESSION_ID)
                    .build();
        }

        session.setSessionListener(sessionListener);
        session.connect(Constants.TOKEN);
        AudioDeviceManager.getAudioDevice().setOutputMode(BaseAudioDevice.OutputMode.Handset);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        if (isInPictureInPictureMode) {
            Log.d("RequestX", "isInPip");
            pictureInPictureButton.setVisibility(View.GONE);
            publisherViewContainer.setVisibility(View.GONE);
            publisher.getView().setVisibility(View.GONE);
            getActionBar().hide();
        } else {
            Log.d("RequestX", "notInPip");
            pictureInPictureButton.setVisibility(View.VISIBLE);
            publisherViewContainer.setVisibility(View.VISIBLE);
            publisher.getView().setVisibility(View.VISIBLE);

            if (publisher.getView() instanceof GLSurfaceView) {
                ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
            }

            getActionBar().show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("RequestX", "onPause");
        if(session != null)
            session.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RequestX", "onResume");
        if (session != null)
            session.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (subscriber != null) {
            subscriberViewContainer.removeView(subscriber.getView());
        }

        if (publisher != null) {
            publisherViewContainer.removeView(publisher.getView());
        }
    }

    private void finishWithMessage(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ": " + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finishWithMessage("onPermissionsDenied: " + requestCode + ": " + perms);
    }

}