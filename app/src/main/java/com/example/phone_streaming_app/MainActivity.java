package com.example.phone_streaming_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import org.webrtc.CapturerObserver;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    private PeerConnectionFactory peerConnectionFactory;
    private Intent screepCapturerIntent;
    private int mMediaProjectionPermissionResultCode;
    private EglBase.Context eglBaseContext;
    private MediaProjectionManager mediaProjectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, PackageManager.PERMISSION_GRANTED);
        }
        eglBaseContext = EglBase.create().getEglBaseContext();

        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);

        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();


        startActivity();
    }

    void startActivity() {

        mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);


        screepCapturerIntent = mediaProjectionManager.createScreenCaptureIntent();

//        startCapture();

        startActivityForResult(screepCapturerIntent, CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            return;
        }
        mMediaProjectionPermissionResultCode = resultCode;
        screepCapturerIntent = data;

//        IntentService intentService = new IntentService(mediaProjectionManager);
//        Intent intent = new Intent(intentService);
//        startForegroundService(data);
//        startService(screepCapturerIntent);

//        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, screepCapturerIntent);

        startCapture();

    }

    void startCapture() {

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);

        ScreenCapturerAndroid screenCapturer = screenCapturer();
        boolean screencast = screenCapturer.isScreencast();

        System.out.println("IS SCREENCAST: " + screencast);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(screencast);

        CapturerObserver capturerObserver = videoSource.getCapturerObserver();

        screenCapturer.initialize(surfaceTextureHelper, getApplicationContext(), capturerObserver);
        screenCapturer.startCapture(1080, 1920, 30);


        SurfaceViewRenderer localView = findViewById(R.id.svRemoteView);
        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        // display in localView
        videoTrack.addSink(localView);
    }


    private ScreenCapturerAndroid screenCapturer() {
        return new ScreenCapturerAndroid(screepCapturerIntent, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                System.out.println("STOP SCREEN CAPTURER");
            }
        });
    }
}
