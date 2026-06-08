package com.solmaru.ubercallacceptor;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import java.util.List;

public class MainActivity extends Activity {

    private TextView tvServiceStatus;
    private EditText etX, etY;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        etX = findViewById(R.id.etX);
        etY = findViewById(R.id.etY);

        // 저장된 좌표 불러오기
        etX.setText(String.valueOf((int) prefs.getFloat("tap_x", 540f)));
        etY.setText(String.valueOf((int) prefs.getFloat("tap_y", 2100f)));

        // 좌표 저장 버튼
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            try {
                float x = Float.parseFloat(etX.getText().toString());
                float y = Float.parseFloat(etY.getText().toString());
                prefs.edit().putFloat("tap_x", x).putFloat("tap_y", y).apply();
                Toast.makeText(this, "✅ 저장 완료! (" + (int)x + ", " + (int)y + ")", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 테스트 버튼 - 지금 바로 탭
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            if (CallAcceptorService.instance != null) {
                // 현재 입력된 좌표로 바로 탭 (저장 안해도 됨)
                try {
                    float x = Float.parseFloat(etX.getText().toString());
                    float y = Float.parseFloat(etY.getText().toString());
                    CallAcceptorService.instance.tapAt(x, y);
                    Toast.makeText(this, "🚀 탭 실행! (" + (int)x + ", " + (int)y + ")", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "⚠️ 먼저 접근성 서비스를 켜주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 접근성 설정 이동
        findViewById(R.id.btnAccessibility).setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilityServiceEnabled()) {
            tvServiceStatus.setText("● 서비스 켜짐");
            tvServiceStatus.setTextColor(0xFF00D4AA);
        } else {
            tvServiceStatus.setText("● 서비스 꺼짐");
            tvServiceStatus.setTextColor(0xFFFF5555);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo s : services) {
            if (s.getId().contains("com.solmaru.ubercallacceptor")) return true;
        }
        return false;
    }
}
