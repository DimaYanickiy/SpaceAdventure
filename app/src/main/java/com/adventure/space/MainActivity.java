package com.adventure.space;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton button_play, button_leave, btn_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_play = (ImageButton)findViewById(R.id.play);
        button_play.setOnClickListener(v -> {
            Intent intent = new Intent(this, Game.class);
            startActivity(intent);
        });

        button_leave = (ImageButton)findViewById(R.id.leave);
        button_leave.setOnClickListener(v -> {
            finish();
        });

        btn_scan = (ImageButton)findViewById(R.id.btn_scanner);
        btn_scan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        });
    }
}