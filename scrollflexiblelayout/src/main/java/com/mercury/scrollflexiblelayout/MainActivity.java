package com.mercury.scrollflexiblelayout;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    ScrollLayout mScrollLayout;
    IndicatorView mIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScrollLayout = findViewById(R.id.scroll);
        mIndicatorView = findViewById(R.id.indicator);

        mScrollLayout.setOnPageChangeListener(new ScrollLayout.OnPageChangeListener() {
            @Override
            public void onPageChange(float ratio) {
                mIndicatorView.setNewPosition(ratio);
            }
        });

        MyAdapter myAdapter= new MyAdapter(this);
        List<TestBean> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(new TestBean("第" + i + "个", R.drawable.test));
        }
        myAdapter.setData(list);
        mScrollLayout.setAdapter(myAdapter);

    }

    private static class MyAdapter extends ScrollLayout.ScrollAdapter<TestBean>{

        private MyAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindView(View view, final int position) {
            ImageView ivIcon = view.findViewById(R.id.iv_icon);
            TextView tvMenu = view.findViewById(R.id.tv_menu);

            ivIcon.setImageResource(getItem(position).getImg());
            tvMenu.setText(getItem(position).getTitle());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: " + getItem(position).getTitle());
                }
            });

        }

        @Override
        public View onCreateView(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            return inflater.inflate(R.layout.item_content, parent,false);
        }

        @Override
        public int getItemCount() {
            return getData().size();
        }
    }

}
