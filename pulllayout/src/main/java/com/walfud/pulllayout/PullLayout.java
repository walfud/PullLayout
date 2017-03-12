package com.walfud.pulllayout;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by walfud on 2017/3/4.
 */

public class PullLayout extends ViewGroup implements NestedScrollingParent {

    public static final String TAG = "PullLayout";

    private static final double MAX_THRESHOLD = 2;
    private static final int FLING_SPEED = 20;

    private ViewHolder mHeaderViewHolder, mFooterViewHolder;
    private OnPullListener mOnPullDownListener, mOnPullUpListener;

    public PullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, String.format("onScrollChange: scrollCurr=%d, scrollOld=%d, onStop=%d", scrollY, oldScrollY, getScrollY()));


                int distance = Math.abs(scrollY);
                if (scrollY < 0) {
                    if (mOnPullDownListener != null) {
                        mOnPullDownListener.onPull(mHeaderViewHolder, distance, distance / (double) mHeaderViewHolder.view.getHeight());
                    }
                } else {
                    if (mOnPullUpListener != null) {
                        mOnPullUpListener.onPull(mFooterViewHolder, distance, distance / (double) mFooterViewHolder.view.getHeight());
                    }
                }

            }
        });
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
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

            if (mHeaderViewHolder != null && child == mHeaderViewHolder.view) {
                child.layout(0, -child.getMeasuredHeight(), getMeasuredWidth(), 0);
            } else if (mFooterViewHolder != null && child == mFooterViewHolder.view) {
                child.layout(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
            } else {
                child.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    private int mPointerStart, mPointerOld, mPointerNow;
    private int mStartScroll;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerStart = mPointerOld = (int) ev.getY();
                mStartScroll = getScrollY();
                return false;
            case MotionEvent.ACTION_MOVE:
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mPointerNow = (int) event.getY();
        int distance = mPointerNow - mPointerStart + (-mStartScroll);
        boolean isHandle = false;

        Log.i(TAG, String.format("onTouchEvent(%d): start=%d, curr=%d, distance=%d, scroll=%d",
                event.getAction(), mPointerStart, mPointerNow, distance, getScrollY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(mFlingRunnable);
                isHandle = true;
                break;
            case MotionEvent.ACTION_MOVE: {
                isHandle = onScroll(distance);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onStop();
                isHandle = true;
                break;
            default:
                isHandle = false;
                break;
        }

        mPointerOld = mPointerNow;
        return isHandle;
    }

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private int mScrollDistance;

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        mStartScroll = getScrollY();
        mScrollDistance = 0;
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        onStop();

        Log.i(TAG, String.format("onStopNestedScroll: scrollY=%d", getScrollY()));
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onScroll(-(mScrollDistance += dyUnconsumed));

        Log.i(TAG, String.format("onNestedScroll: consumeY=%d, unconsumeY=%d, distance=%d",
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mScrollDistance));
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.i(TAG, String.format("onNestedPreScroll: dx=%d, dy=%d, consumeX=%d, consumeY=%d", dx, dy, consumed[0], consumed[1]));
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    // Function
    public PullLayout setHeader(ViewHolder header, OnPullListener listener) {
        removeView(header.view);
        addView(header.view);
        mHeaderViewHolder = header;
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
        scrollY(0, null);

        return this;
    }

    public PullLayout setFooter(ViewHolder footer, OnPullListener listener) {
        removeView(footer.view);
        addView(footer.view);
        mFooterViewHolder = footer;
        mOnPullUpListener = listener;

        return this;
    }

    public PullLayout showFooter() {
        if (mFooterViewHolder != null) {
            int footerHeight = mFooterViewHolder.view.getHeight();
            scrollTo(0, footerHeight);
        }

        return this;
    }

    public PullLayout hideFooter() {
        scrollY(0, null);

        return this;
    }

    // internal
    private Runnable mFlingRunnable;
    private boolean scrollY(final int to, final Runnable endWith) {
        int from = getScrollY();
        if (from < to) {
            // from >>> to
            scrollTo(0, Math.min(from + FLING_SPEED, to));
            removeCallbacks(mFlingRunnable);
            postOnAnimation(mFlingRunnable = new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });

            Log.i(TAG, String.format("scrollY: from=%d, next=%d, to=%d", from, Math.min(from + FLING_SPEED, to), to));
        } else if (to < from) {
            // to <<< from
            scrollTo(0, Math.max(from - FLING_SPEED, to));
            removeCallbacks(mFlingRunnable);
            postOnAnimation(mFlingRunnable = new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });

            Log.i(TAG, String.format("scrollY: from=%d, next=%d, to=%d", from, Math.max(from - FLING_SPEED, to), to));
        } else {
            mFlingRunnable = null;
            post(endWith);

            Log.i(TAG, String.format("scrollY: END from=%d, to=%d", from, to));
            return false;
        }

        return true;
    }

    private boolean handleScroll(int distance, int maxDistance) {
        boolean isHandle = false;

        if (maxDistance != 0) {
            if (Math.abs(distance) < Math.abs(maxDistance * MAX_THRESHOLD)) {
                scrollTo(0, -distance);
                isHandle = true;
            } else {
                // Over Drag
                scrollTo(0, (int) (-maxDistance * MAX_THRESHOLD));
            }
        }

        return isHandle;
    }

    private boolean onScroll(int distance) {
        boolean isHandle = false;
        if (distance > 0 && mHeaderViewHolder != null) {
            isHandle = handleScroll(distance, mHeaderViewHolder.view.getHeight());
        }
        if (distance < 0 && mFooterViewHolder != null) {
            isHandle |= handleScroll(distance, -mFooterViewHolder.view.getHeight());
        }
        return isHandle;
    }

    private void onStop() {
        int scrollY = getScrollY();
        if (scrollY < 0 && mHeaderViewHolder != null) {
            handleStop(scrollY, -mHeaderViewHolder.view.getHeight(), new Runnable() {
                @Override
                public void run() {
                    if (mOnPullDownListener != null) {
                        mOnPullDownListener.onRefresh(mHeaderViewHolder);
                    }
                }
            });
        }
        if (scrollY > 0 && mFooterViewHolder != null) {
            handleStop(scrollY, mFooterViewHolder.view.getHeight(), new Runnable() {
                @Override
                public void run() {
                    if (mOnPullUpListener != null) {
                        mOnPullUpListener.onRefresh(mFooterViewHolder);
                    }
                }
            });
        }
    }
    private void handleStop(int scrollY, int threshold, Runnable overScrollRunnable) {
        if (Math.abs(scrollY) < Math.abs(threshold)) {
            // Back to top
            scrollY(0, null);
        } else {
            // Scroll to refresh
            scrollY(threshold, overScrollRunnable);
        }
    }

    //
    public static abstract class ViewHolder {
        public View view;

        public ViewHolder(View view) {
            this.view = view;
        }
    }

    public interface OnPullListener<T extends ViewHolder> {
        void onPull(T headerViewHolder, int dy, double py);

        void onRefresh(T headerViewHolder);
    }
}
