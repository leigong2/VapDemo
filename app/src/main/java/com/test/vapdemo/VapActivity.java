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

import com.tencent.qgame.animplayer.AnimView;
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
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 50)));
        textView.setGravity(Gravity.CENTER);
        textView.setText("\"0_8_264_800_big.mp4\"");
        vapLay.addView(textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VaPlayerUtils.startPlay(animView, "lottery_svga.mp4", false, ScaleType.FIT_XY, false);
            }
        });
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
