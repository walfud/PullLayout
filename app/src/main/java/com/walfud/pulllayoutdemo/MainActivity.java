package com.walfud.pulllayoutdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
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

                iv.setScaleX((float) Math.min(py, 1.3));
                iv.setScaleY((float) Math.min(py, 1.3));
                tv.setText(String.format("onPullDown: dy=%d, py=%.2f", dy, py));
                Log.e(TAG, String.format("onPullDown: py=%.2f", py));
            }

            @Override
            public void onRefresh(HeaderViewHolder headerViewHolder, int dy, double py) {
                Log.e(TAG, "onRefresh: ");

                final ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;

                headerAnim(iv);
                tv.setText(String.format("onRefresh"));

                mPl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mHeaderAnim != null) {
                            mHeaderAnim.cancel();
                            mHeaderAnim = null;
                        }
                        mPl.hideHeader();
                    }
                }, 3 * 1000);
            }
        });
    }

    private ViewPropertyAnimator mHeaderAnim;

    private void headerAnim(final View view) {
        mHeaderAnim = view.animate().alpha(0.6f).scaleX(1.3f).scaleY(1.3f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mHeaderAnim = view.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setInterpolator(new BounceInterpolator()).setDuration(600).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        headerAnim(view);
                    }
                });
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
