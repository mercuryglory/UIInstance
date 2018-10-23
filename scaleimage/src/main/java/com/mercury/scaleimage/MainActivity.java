package com.mercury.scaleimage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        final int[] idList = {R.drawable.test, R.drawable.test2, R.drawable.test3, R.drawable.test4, R
                .drawable.test5};
        CustomViewPager viewPager = findViewById(R.id.viewpager);
        PagerAdapter adapter=new PagerAdapter() {
            @Override
            public int getCount() {
                return idList.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ScaleImageView scaleImageView = new ScaleImageView(container.getContext());
                scaleImageView.setImageResource(idList[position]);
//                SubsamplingScaleImageView scaleImageView = new SubsamplingScaleImageView(container.getContext());
//                scaleImageView.setImage(ImageSource.resource(idList[position]));
                container.addView(scaleImageView);
                return scaleImageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object
                    object) {
                container.removeView((View) object);
            }
        };

        viewPager.setAdapter(adapter);

    }
}
