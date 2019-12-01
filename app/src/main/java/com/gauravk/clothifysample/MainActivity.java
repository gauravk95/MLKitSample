package com.gauravk.clothifysample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.clothifysample.FaceDetectionUtil.ScannerActivity;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    Button tvTryNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTryNow = findViewById(R.id.tvTryNow);
        tvTryNow.setOnClickListener(v -> launchScanner());

    }

    /**
     * Capture image from camera
     */

    public void launchScanner() {
        Intent scannerIntent = new Intent(this, ScannerActivity.class);
        startActivity(scannerIntent);
    }
}
