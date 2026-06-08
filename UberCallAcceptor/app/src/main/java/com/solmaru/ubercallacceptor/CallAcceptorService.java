package com.solmaru.ubercallacceptor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class CallAcceptorService extends AccessibilityService {

    private AudioManager audioManager;
    private int previousVolume = -1;
    private Handler handler = new Handler(Looper.getMainLooper());
    public static CallAcceptorService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        handler.post(volumeChecker);
    }

    private Runnable volumeChecker = new Runnable() {
        @Override
        public void run() {
            if (audioManager != null) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (previousVolume >= 0 && currentVolume > previousVolume) {
                    // 볼륨UP 감지 → 설정된 좌표 탭
                    tapAtSavedCoordinates();
                    // 볼륨 원래대로 복원
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
                } else {
                    previousVolume = currentVolume;
                }
            }
            handler.postDelayed(this, 100);
        }
    };

    public void tapAtSavedCoordinates() {
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        float x = prefs.getFloat("tap_x", 540f);  // 기본값 화면 중앙
        float y = prefs.getFloat("tap_y", 2100f); // 기본값 하단

        tapAt(x, y);
    }

    public void tapAt(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        handler.removeCallbacks(volumeChecker);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        handler.removeCallbacks(volumeChecker);
    }
}
