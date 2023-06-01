package com.test.vapdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.qgame.animplayer.util.ScaleType;

import java.io.File;


public class VapActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, VapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vap);
        FrameLayout animView = findViewById(R.id.anim_view);
        LinearLayout vapLay = findViewById(R.id.vap_lay);
        File filesDir = getExternalFilesDir(null);
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, VaPlayerUtils.dp2px(this, 50)));
        textView.setGravity(Gravity.CENTER);
        textView.setText("\"pk_tie.mp4\"");
        vapLay.addView(textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VaPlayerUtils.startPlay(animView, "pk_tie.mp4", false, ScaleType.FIT_XY, false);
            }
        });
    }
}
