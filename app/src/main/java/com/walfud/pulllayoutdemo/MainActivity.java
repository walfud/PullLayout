package com.walfud.pulllayoutdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.walfud.pulllayout.PullLayout;

public class MainActivity extends Activity {

    private PullLayout mPl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPl = (PullLayout) findViewById(R.id.pl);

        final View header = LayoutInflater.from(this).inflate(R.layout.header_pulllayout, mPl, false);
        mPl.setHeader(new HeaderViewHolder(header));
        mPl.setOnEventListener(new PullLayout.OnPullDownListener<HeaderViewHolder>() {
            @Override
            public void onPullDown(HeaderViewHolder headerViewHolder, int dy, double percent) {
                ImageView iv = headerViewHolder.iv;
                TextView tv = headerViewHolder.tv;

                iv.setRotation((float) (percent * 360));
                tv.setText(String.format("onPullDown: dy=%d, percent=%.2f", dy, percent));

                mPl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPl.hideHeader();
                    }
                }, 3 * 1000);
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
