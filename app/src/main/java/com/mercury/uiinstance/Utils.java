package com.mercury.uiinstance;

import android.content.Context;

import java.util.Random;

public class Utils {

    public static String[] getStringArray(Context context,int resId){
        return context.getResources().getStringArray(resId);
    }

    public static int getDimens(Context context,int resId){
        return context.getResources().getDimensionPixelSize(resId);
    }


    public static int getColor(Context context,int resId) {
        return context.getResources().getColor(resId);
    }

    public static int createRandomColor() {
        Random random = new Random();
        return random.nextInt(100) + 80;
    }
}