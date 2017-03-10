package com.walfud.pulllayoutdemo;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.walfud.pulllayout.PullLayout;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private PullLayout mPl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPl = (PullLayout) findViewById(R.id.pl);

        final View header = LayoutInflater.from(this).inflate(R.layout.header_pulllayout, mPl, false);
        mPl.setHeader(new HeaderViewHolder(header));
        mPl.setOnEventListener(new PullLayout.OnPullListener<HeaderViewHolder>() {
            @Override
            public void onPullDown(HeaderViewHolder headerViewHolder, int dy, double py) {
                ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;

                iv.setRotation((float) (py * 360));
                tv.setText(String.format("onPullDown: dy=%d, py=%.2f", dy, py));
                Log.e(TAG, String.format("onPullDown: py=%.2f", py));
            }

            @Override
            public void onRefresh(HeaderViewHolder headerViewHolder, int dy, double py) {
                Log.e(TAG, "onRefresh: ");

                ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;
                final ObjectAnimator rotation = ObjectAnimator.ofFloat(iv, "rotation", iv.getRotation(), iv.getRotation() - 360);
                rotation.setDuration(1000);
                rotation.setRepeatCount(100);
                rotation.start();

                mPl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rotation.end();
                        mPl.hideHeader();
                    }
                }, 1 * 1000);
            }
        });
    }

    public static class HeaderViewHolder extends PullLayout.ViewHolder {

        public ImageView iv;
        public TextView tv;

        public HeaderViewHolder(View view) {
            super(view);
            iv = (ImageView) view.findViewById(R.id.iv);
            tv = (TextView) view.findViewById(R.id.tv);
        }
    }
}
