package com.walfud.pulllayout;

import android.content.Context;
import android.support.v4.view.NestedScrollingParentHelper;
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

    private static final double MAX_THRESHOLD = 2;
    private static final int FLING_SPEED = 20;

    private ViewHolder mHeaderViewHolder, mFooterViewHolder;
    private OnPullListener mOnPullDownListener, mOnPullUpListener;

    public PullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, String.format("onScrollChange: scrollCurr=%d, scrollOld=%d, foo=%d", scrollY, oldScrollY, getScrollY()));


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
    private boolean mIsTouching;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerStart = mPointerOld = (int) ev.getY();
                mStartScroll = getScrollY();
                mIsTouching = true;
                return false;
            case MotionEvent.ACTION_MOVE:
                return true;
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
                isHandle = true;
                break;
            case MotionEvent.ACTION_MOVE: {
                isHandle = handleHeaderScroll(distance) || handleFooterScroll(distance);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsTouching = false;
                handleHeaderStop();
                handleFooterStop();
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

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i(TAG, String.format("onNestedScroll: consumeX=%d, consumeY=%d, unconsumeX=%d, unconsumeY=%d", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed));

        scrollBy(dxUnconsumed, dyUnconsumed);
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
    private boolean scrollY(final int to, final Runnable endWith) {
        if (mIsTouching) {
            return false;
        }

        int from = getScrollY();
        if (from < to) {
            // from >>> to
            scrollTo(0, Math.min(from + FLING_SPEED, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });
        } else if (to < from) {
            // to <<< from
            scrollTo(0, Math.max(from - FLING_SPEED, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });
        } else {
            post(endWith);
            return false;
        }

        return true;
    }

    private boolean handleHeaderScroll(int distance) {
        boolean isHandle = false;

        if (distance > 0 && mHeaderViewHolder != null) {
            int headerHeight = mHeaderViewHolder.view.getHeight();
            if (headerHeight != 0) {
                if (distance < headerHeight * MAX_THRESHOLD) {
                    scrollTo(0, -distance);
                    isHandle = true;
                } else {
                    // Over Down
                    scrollTo(0, -(int) (headerHeight * MAX_THRESHOLD));
                }
            }
        }

        return isHandle;
    }
    private boolean handleFooterScroll(int distance) {
        boolean isHandle = false;

        if (distance < 0 && mFooterViewHolder != null) {
            int footerHeight = mFooterViewHolder.view.getHeight();
            if (footerHeight != 0) {
                if (Math.abs(distance) < footerHeight * MAX_THRESHOLD) {
                    scrollTo(0, -distance);
                    isHandle = true;
                } else {
                    // Over Up
                    scrollTo(0, (int) (footerHeight * MAX_THRESHOLD));
                }
            }
        }

        return isHandle;
    }
    private void handleHeaderStop() {
        int scrollY = getScrollY();
        if (scrollY < 0 && mHeaderViewHolder != null) {
            final int headerHeight = mHeaderViewHolder.view.getHeight();
            if (Math.abs(scrollY) < headerHeight) {
                // Back to top
                scrollY(0, null);
            } else {
                // Scroll to refresh
                scrollY(-headerHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (mOnPullDownListener != null) {
                            mOnPullDownListener.onRefresh(mHeaderViewHolder);
                        }
                    }
                });
            }
        }
    }
    private void handleFooterStop() {
        int scrollY = getScrollY();
        if (scrollY > 0 && mFooterViewHolder != null) {
            final int footerHeight = mFooterViewHolder.view.getHeight();
            if (scrollY < footerHeight) {
                // Back to top
                scrollY(0, null);
            } else {
                // Scroll to refresh
                scrollY(footerHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (mOnPullUpListener != null) {
                            mOnPullUpListener.onRefresh(mFooterViewHolder);
                        }
                    }
                });
            }
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
