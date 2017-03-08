package com.walfud.pulllayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by walfud on 2017/3/4.
 */

public class PullLayout extends ViewGroup {

    public static final String TAG = "PullLayout";

    private ViewHolder mHeaderViewHolder;
    private OnPullDownListener mOnPullDownListener;

    public PullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child == mHeaderViewHolder.view) {
                child.layout(0, -child.getMeasuredHeight(), getMeasuredWidth(), 0);
            } else {
                child.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE: {

                return true;
            }
            default:
                return false;
        }
    }

    private int mDownY;
    private boolean mEdgeY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int dy = y - mDownY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = y;
                mEdgeY = false;
                return true;
            case MotionEvent.ACTION_MOVE: {
                if (dy > 0) {
                    if (mHeaderViewHolder != null) {
                        int headerHeight = mHeaderViewHolder.view.getHeight();
                        double percentY = dy / (double) headerHeight;
                        if (dy < mHeaderViewHolder.view.getHeight()) {
                            scrollTo(0, -dy);
                            mOnPullDownListener.onPullDown(mHeaderViewHolder, dy, percentY);

                            Log.e(TAG, String.format("onTouchEvent: ACTION_MOVE y=%d, dy=%d, percent=%.2f, headerHeight=%d", y, dy, percentY, headerHeight));
                        } else {
                            if (!mEdgeY) {
                                mEdgeY = true;

                                scrollTo(0, -dy);
                                mOnPullDownListener.onPullDown(mHeaderViewHolder, headerHeight, 1.0);

                                Log.e(TAG, String.format("onTouchEvent: ACTION_MOVE EDGE y=%d, dy=%d, percent=%.2f, headerHeight=%d", y, dy, 1.0, headerHeight));
                            }
                        }
                    }
                }
                Log.i(TAG, String.format("onTouchEvent: ACTION_MOVE y=%d, dy=%d", y, dy));
                return true;
            }
            case MotionEvent.ACTION_UP:
                if (dy > 0) {
                    if (mHeaderViewHolder != null) {
                        int headerHeight = mHeaderViewHolder.view.getHeight();
                        double percentY = dy / (double) headerHeight;
                        if (mEdgeY) {
                            if (mOnPullDownListener != null) {
                                mOnPullDownListener.onPullRefresh();
                            }
                        } else {
                            if (percentY > 0.75) {
                                showHeader();

                                if (mOnPullDownListener != null) {
                                    mOnPullDownListener.onPullDown(mHeaderViewHolder, headerHeight, 1.0);
                                    mOnPullDownListener.onPullRefresh();
                                }
                            }
                        }
                    }
                }
                return true;
            default:
                return false;
        }
    }

    // Function
    public <H extends ViewHolder> PullLayout setHeader(H header) {
        removeView(header.view);
        addView(header.view);
        mHeaderViewHolder = header;
        return this;
    }
    public PullLayout setOnEventListener(OnPullDownListener listener) {
        mOnPullDownListener = listener;
        return this;
    }

    public PullLayout showHeader() {
        if (mHeaderViewHolder != null) {
            int headerHeight = mHeaderViewHolder.view.getHeight();
            scrollTo(0, -headerHeight);
        }

        return this;
    }
    public PullLayout hideHeader() {
        scrollTo(0, 0);

        return this;
    }

    //
//    public static class Config {
//        public double maxPullablePercent;
//        public double effectPercent;
//    }
    public static abstract class ViewHolder {
        public View view;

        public ViewHolder(View view) {
            this.view = view;
        }
    }
    public interface OnPullDownListener<T extends ViewHolder> {
        void onPullDown(T headerViewHolder, int dy, double py);
        void onPullRefresh();
    }
}
