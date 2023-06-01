package com.test.vapdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.qgame.animplayer.AnimConfig;
import com.tencent.qgame.animplayer.AnimView;
import com.tencent.qgame.animplayer.inter.IAnimListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Vap2Activity extends AppCompatActivity {
    private final List<Object> mData = new ArrayList<>();
    Handler mHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context) {
        Intent intent = new Intent(context, Vap2Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vap2);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false)) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (position != 0) {
                    return;
                }
                AnimView animView = new AnimView(Vap2Activity.this);
                ((ViewGroup) (holder.itemView)).addView(animView);
                animView.setLoop(0);
                File file = new File(getExternalFilesDir(null), "238.mp4");
                animView.startPlay(file);
                animView.setAnimListener(new IAnimListenerImp(animView) {
                    @Override
                    public void onVideoComplete(AnimView animView) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ViewGroup parent = (ViewGroup) animView.getParent();
                                parent.removeView(animView);
                            }
                        });
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mData.size();
            }
        });
        for (int i = 0; i < 3; i++) {
            mData.add(i);
        }
        refreshNotifyItem(recyclerView);
    }

    private void refreshNotifyItem(RecyclerView recyclerView) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.getAdapter().notifyItemChanged(0);
                refreshNotifyItem(recyclerView);
            }
        }, 5000);
    }

    public abstract class IAnimListenerImp implements IAnimListener {
        AnimView animView;

        public IAnimListenerImp(AnimView animView) {
            this.animView = animView;
        }

        @Override
        public boolean onVideoConfigReady(AnimConfig config) {
            return true;
        }

        @Override
        public void onVideoStart() {

        }

        @Override
        public void onVideoRender(int frameIndex, AnimConfig config) {

        }

        @Override
        public void onVideoComplete() {
            onVideoComplete(animView);
        }

        public abstract void onVideoComplete(AnimView animView);

        @Override
        public void onVideoDestroy() {

        }

        @Override
        public void onFailed(int errorType, String errorMsg) {

        }
    }
}
