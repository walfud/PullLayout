package com.walfud.pulllayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by walfud on 2017/3/4.
 */

public class PullLayout extends ViewGroup {

    public static final String TAG = "PullLayout";

    private View mHeader;
    private OnEventListener mOnEventListener;

    public PullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    // Function
    public PullLayout setHeader(View header) {
        mHeader = header;
        return this;
    }
    public PullLayout setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
        return this;
    }

    //
    public static class Config {
        public double maxPullablePercent;
        public double effectPercent;
    }
    public interface OnEventListener {
        void onPullDown(int dy, double percent);
    }
    public static class SimpleOnEventListener implements OnEventListener {
        @Override
        public void onPullDown(int dy, double percent) {

        }
    }
}
