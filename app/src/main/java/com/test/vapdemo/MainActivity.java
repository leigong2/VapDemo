package com.test.vapdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text_view).setOnClickListener(v -> {
            VapActivity.start(v.getContext());
        });
        findViewById(R.id.text_view2).setOnClickListener(v -> {
            Vap2Activity.start(v.getContext());
        });
    }
}