package com.mercury.scrollflexiblelayout;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author wang.zhonghao
 * @date 2018/9/26
 * @descript
 */

public class ScreenUtil {

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

}
