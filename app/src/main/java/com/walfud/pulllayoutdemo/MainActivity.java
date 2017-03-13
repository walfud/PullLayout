package com.walfud.pulllayoutdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.walfud.pulllayout.PullLayout;

import java.util.Random;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private PullLayout mPl;
    private RecyclerView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPl = (PullLayout) findViewById(R.id.pl);

        // Header
        final View header = LayoutInflater.from(this).inflate(R.layout.header_pulllayout, mPl, false);
        mPl.setHeader(new HeaderViewHolder(header), new PullLayout.OnPullListener<HeaderViewHolder>() {
            private ViewPropertyAnimator mAnim;

            private void anim(final View view) {
                mAnim = view.animate().alpha(0.6f).scaleX(1.3f).scaleY(1.3f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        mAnim = view.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setInterpolator(new BounceInterpolator()).setDuration(600).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                anim(view);
                            }
                        });
                    }
                });
            }

            @Override
            public void onStart(HeaderViewHolder headerViewHolder) {
                mPl.setFooterEnable(false);
                Log.e(TAG, "onStart: header");
            }

            @Override
            public void onPull(HeaderViewHolder headerViewHolder, int dy, double py) {
                ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;

                iv.setScaleX((float) Math.min(py, 1.3));
                iv.setScaleY((float) Math.min(py, 1.3));
                tv.setText(String.format("onPull: dy=%d, py=%.2f", dy, py));
                Log.e(TAG, String.format("onPull: py=%.2f", py));
            }

            @Override
            public void onRefresh(HeaderViewHolder headerViewHolder) {
                Log.e(TAG, "onRefresh: ");

                mPl.setHeaderEnable(false);

                final ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;

                anim(iv);
                tv.setText(String.format("onRefresh"));

                mPl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mAnim != null) {
                            mAnim.cancel();
                            mAnim = null;
                        }
                        mPl.hideHeader();
                        mPl.setFooterEnable(true);
                        mRv.getAdapter().notifyDataSetChanged();
                    }
                }, 3 * 1000);
            }
        });

        // Footer
        final View footer = LayoutInflater.from(this).inflate(R.layout.footer_pulllayout, mPl, false);
        mPl.setFooter(new FooterViewHolder(footer), new PullLayout.OnPullListener<FooterViewHolder>() {
            private ViewPropertyAnimator mAnim;

            private void anim(final View view) {
                mAnim = view.animate().alpha(0.6f).scaleX(1.3f).scaleY(1.3f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        mAnim = view.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setInterpolator(new BounceInterpolator()).setDuration(600).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                anim(view);
                            }
                        });
                    }
                });
            }

            @Override
            public void onStart(FooterViewHolder footerViewHolder) {
                mPl.setHeaderEnable(false);
                Log.e(TAG, "onStart: footer");
            }

            @Override
            public void onPull(FooterViewHolder footerViewHolder, int dy, double py) {
                ImageView iv = footerViewHolder.iv;
                TextView tv = footerViewHolder.tv;

                iv.setScaleX((float) Math.min(py, 1.3));
                iv.setScaleY((float) Math.min(py, 1.3));
                tv.setText(String.format("onPull: dy=%d, py=%.2f", dy, py));
                Log.e(TAG, String.format("onPull: py=%.2f", py));
            }

            @Override
            public void onRefresh(FooterViewHolder footerViewHolder) {
                Log.e(TAG, "onRefresh: ");

                final ImageView iv = footerViewHolder.iv;
                TextView tv = footerViewHolder.tv;

                anim(iv);
                tv.setText(String.format("onRefresh"));

                mPl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mAnim != null) {
                            mAnim.cancel();
                            mAnim = null;
                        }
                        mPl.hideFooter();
                        mPl.setHeaderEnable(true);
                        mRv.getAdapter().notifyDataSetChanged();
                    }
                }, 3 * 1000);
            }
        });

        mRv = (RecyclerView) findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRv.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(MainActivity.this);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
                tv.setLayoutParams(layoutParams);
                return new RecyclerView.ViewHolder(tv) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView tv = (TextView) holder.itemView;
                tv.setText(String.valueOf(position));
                tv.setGravity(Gravity.CENTER);
                holder.itemView.setBackgroundColor(0xFF000000 | new Random().nextInt(0x00FFFFFF));
            }

            @Override
            public int getItemCount() {
                return 20;
            }
        });
    }

    public void onClick(View v) {
        Toast.makeText(MainActivity.this, "Click Me", Toast.LENGTH_SHORT).show();
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

    public static class FooterViewHolder extends PullLayout.ViewHolder {

        public ImageView iv;
        public TextView tv;

        public FooterViewHolder(View view) {
            super(view);
            iv = (ImageView) view.findViewById(R.id.iv);
            tv = (TextView) view.findViewById(R.id.tv);
        }
    }
}
