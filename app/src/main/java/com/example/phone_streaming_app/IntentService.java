package com.example.phone_streaming_app;

import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.Nullable;

public class IntentService extends android.app.IntentService {

    private MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    int code;
    Intent data;

    public IntentService(MediaProjectionManager mediaProjectionManager) {
        super("name");
        this.mediaProjectionManager = mediaProjectionManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        code = intent.getIntExtra("code", -1);
        data = intent.getParcelableExtra("data");

        mediaProjection = mediaProjectionManager.getMediaProjection(code, data);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

}
