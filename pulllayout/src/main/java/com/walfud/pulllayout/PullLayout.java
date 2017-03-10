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

    private static final double MAX_THRESHOLD = 2;

    private ViewHolder mHeaderViewHolder;
    private OnPullListener mOnPullListener;
    private int mPointerStart, mPointerOld, mPointerNow;

    public PullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, String.format("onScrollChange: scrollCurr=%d, scrollOld=%d", scrollY, oldScrollY));

                if (mOnPullListener != null) {
                    int distance = Math.abs(scrollY);
                    if (scrollY < 0) {
                        mOnPullListener.onPullDown(mHeaderViewHolder, distance, distance / (double) mHeaderViewHolder.view.getHeight());
                    }
                }
            }
        });
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
            case MotionEvent.ACTION_MOVE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mFling) {
            return false;
        }

        mPointerNow = (int) event.getY();
        int distance = mPointerNow - mPointerStart;
        boolean isHandle = false;

        Log.i(TAG, String.format("onTouchEvent(%d): start=%d, curr=%d, distance=%d", event.getAction(), mPointerStart, (int) event.getY(), distance));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerStart = mPointerOld = mPointerNow;
                isHandle = true;
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mHeaderViewHolder != null) {
                    int headerHeight = mHeaderViewHolder.view.getHeight();
                    if (headerHeight != 0) {
                        if (distance < 0) {
                            // Over Up
                            scrollTo(0, 0);
                        } else if (0 <= distance && distance <= headerHeight * MAX_THRESHOLD) {
                            scrollTo(0, -distance);
                            isHandle = true;
                        } else {
                            // Over Down
                            scrollTo(0, - (int) (headerHeight * MAX_THRESHOLD));
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mHeaderViewHolder != null) {
                    final int headerHeight = mHeaderViewHolder.view.getHeight();
                    if (0 < distance && distance < headerHeight) {
                        // Back to top
                        scrollY(0, null);
                    } else if (headerHeight <= distance) {
                        // Scroll to refresh
                        scrollY(-headerHeight, new Runnable() {
                            @Override
                            public void run() {
                                if (mOnPullListener != null) {
                                    mOnPullListener.onRefresh(mHeaderViewHolder, headerHeight, 1.0);
                                }
                            }
                        });
                    }
                }
                isHandle = true;
                break;
            default:
                isHandle = false;
                break;
        }

        mPointerOld = mPointerNow;
        return isHandle;
    }

    // Function
    public <H extends ViewHolder> PullLayout setHeader(H header) {
        removeView(header.view);
        addView(header.view);
        mHeaderViewHolder = header;
        return this;
    }

    public PullLayout setOnEventListener(OnPullListener listener) {
        mOnPullListener = listener;
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

    // internal
    private boolean mFling;

    private boolean scrollY(final int to, final Runnable endWith) {
        mFling = true;

        int from = getScrollY();
        if (from < to) {
            // from >>> to
            scrollTo(0, Math.min(from + 9, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });
        } else if (to < from) {
            // to <<< from
            scrollTo(0, Math.max(from - 9, to));
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    scrollY(to, endWith);
                }
            });
        } else {
            mFling = false;
            post(endWith);
            return false;
        }

        return true;
    }

    //
    public static abstract class ViewHolder {
        public View view;

        public ViewHolder(View view) {
            this.view = view;
        }
    }

    public interface OnPullListener<T extends ViewHolder> {
        void onPullDown(T headerViewHolder, int dy, double py);

        void onRefresh(T headerViewHolder, int dy, double py);
    }
}
