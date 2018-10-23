package com.mercury.scaleimage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author wang.zhonghao
 * @date 2018/10/18
 * @descript
 */

public class CustomViewPager extends ViewPager {

    private static final String TAG = "CustomViewPager";

    private static final boolean DEBUG = false;

    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result;
        try {
            result = super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            result = false;
            if (DEBUG) {
                Log.i(TAG, "mActivePointerId: " + ev.getPointerId(0));
            }
        }
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result;
        try {
            result = super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            result = false;
            if (DEBUG) {
                Log.i(TAG, "mActivePointerId: " + ev.getPointerId(0));
            }
        }
        return result;
    }
}
