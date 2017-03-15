package com.walfud.pulllayout;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
                    // Header
                    if (mOnPullDownListener != null) {
                        if (oldScrollY * scrollY <= 0) {
                            mOnPullDownListener.onStart(mHeaderViewHolder);
                        }
                        mOnPullDownListener.onPull(mHeaderViewHolder, distance, distance / (double) mHeaderViewHolder.view.getHeight());
                    }
                } else if (scrollY > 0) {
                    // Footer
                    if (mOnPullUpListener != null) {
                        if (oldScrollY * scrollY <= 0) {
                            mOnPullUpListener.onStart(mFooterViewHolder);
                        }
                        mOnPullUpListener.onPull(mFooterViewHolder, distance, distance / (double) mFooterViewHolder.view.getHeight());
                    }
                }

            }
        });
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mEnable = true;
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

    private int mPointerStart, mPointerNow;
    private int mStartScroll;

    private int mTouchSlop;
    private boolean mInterceptMoveAction = false;
    private boolean mStopFling = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnable) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStopFling = true;
                mPointerStart = (int) ev.getY();
                mStartScroll = getScrollY();
                mTouchTarget = getTouchTarget(this, (int) ev.getRawX(), (int) ev.getRawY());
                mInterceptMoveAction = !(mTouchTarget instanceof NestedScrollingChild);
                return false;
            case MotionEvent.ACTION_MOVE:
                return mInterceptMoveAction;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }

        mPointerNow = (int) event.getY();
        int distance = mPointerNow - mPointerStart + (-mStartScroll);
        boolean isHandle;

        Log.i(TAG, String.format("onTouchEvent(%d): start=%d, curr=%d, distance=%d, scroll=%d",
                event.getAction(), mPointerStart, mPointerNow, distance, getScrollY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // See `onInterceptTouchEvent#ACTION_DOWN`
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

        return isHandle;
    }

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private int mScrollDistance;

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (!mEnable) {
            return false;
        }

        mStopFling = true;
        mStartScroll = getScrollY();
        mScrollDistance = 0;

        Log.i(TAG, String.format("onStartNestedScroll: startScroll=%d, distance=%d", mStartScroll, mScrollDistance));
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
        if (getScrollY() != 0) {
            consumed[0] = dx;
            consumed[1] = dy;
            mScrollDistance += dy;
            onScroll(-(mStartScroll + mScrollDistance));
        }
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

    public PullLayout hideFooter() {
        scrollY(0, null);

        return this;
    }

    private boolean mEnable;
    public PullLayout setEnable(boolean enable) {
        mEnable = enable;

        return this;
    }

    // internal
    private boolean scrollY(final int to, final Runnable endWith) {
        if (mStopFling) {
            return false;
        }

        int from = getScrollY();
        if (from < to) {
            // Go Up
            scrollTo(0, Math.min(from + FLING_SPEED, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });

            Log.i(TAG, String.format("scrollY: from=%d, next=%d, to=%d", from, Math.min(from + FLING_SPEED, to), to));
        } else if (to < from) {
            // Go Down
            scrollTo(0, Math.max(from - FLING_SPEED, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });

            Log.i(TAG, String.format("scrollY: from=%d, next=%d, to=%d", from, Math.max(from - FLING_SPEED, to), to));
        } else {
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
        if (Math.abs(distance) < mTouchSlop) {
            return false;
        }

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
        mStopFling = false;

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

    private View mTouchTarget;
    private View getTouchTarget(ViewGroup viewGroup, int rawX, int rawY) {
        // Reverse traversal is for care top element first
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            View view = viewGroup.getChildAt(i);
            int[] viewXy = new int[2];
            view.getLocationOnScreen(viewXy);
            Rect viewRect = new Rect(viewXy[0], viewXy[1], viewXy[0] + view.getWidth(), viewXy[1] + view.getHeight());
            if (view.getVisibility() != GONE
                    && viewRect.contains(rawX, rawY)) {
                if (!(view instanceof ViewGroup)
                        || view instanceof NestedScrollingChild) {
                    Log.e(TAG, String.format("Down Target: %s", view.toString()));
                    return view;
                } else {
                    return getTouchTarget((ViewGroup) view, rawX, rawY);
                }
            }
        }

        return null;
    }

    //
    public static abstract class ViewHolder {
        public View view;

        public ViewHolder(View view) {
            this.view = view;
        }
    }

    public interface OnPullListener<T extends ViewHolder> {
        void onStart(T headerViewHolder);

        void onPull(T headerViewHolder, int dy, double py);

        void onRefresh(T headerViewHolder);
    }
}
