package com.mercury.uiinstance;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    FlowLayout mFlowLayout;

    String[] appNames = {"Java", "Android", "C", "C++", "JavaScript", "Python", "Ruby", "C#",
            "Node.js", "HTML", "PHP", "COCOS2D-X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFlowLayout = findViewById(R.id.fl_container);

        int layoutPadding = Utils.getDimens(this, R.dimen.dp10);
        mFlowLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);

        for (int i = 0; i < appNames.length; i++) {
            final TextView tv = new TextView(this);
            tv.setText(appNames[i]);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            int dp5 = Utils.getDimens(this, R.dimen.dp5);
            gradientDrawable.setCornerRadius(dp5);
            gradientDrawable.setColor(Color.rgb(Utils.createRandomColor(), Utils
                    .createRandomColor(), Utils.createRandomColor()));

            GradientDrawable gradientDrawable2 = new GradientDrawable();
            gradientDrawable2.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable2.setCornerRadius(dp5);
            gradientDrawable2.setColor(Color.rgb(Utils.createRandomColor(), Utils
                    .createRandomColor(), Utils.createRandomColor()));

            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            int paddingValue = Utils.getDimens(this,R.dimen.dp5);
            tv.setPadding(paddingValue,paddingValue,paddingValue,paddingValue);

            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                    gradientDrawable);
            stateListDrawable.addState(new int[]{}, gradientDrawable2);

            tv.setBackground(stateListDrawable);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mFlowLayout.addView(tv);
        }

    }
}
