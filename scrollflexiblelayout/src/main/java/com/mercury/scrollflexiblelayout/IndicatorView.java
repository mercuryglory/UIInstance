package com.mercury.scrollflexiblelayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Mercury
 * @descript
 * @since 2018/9/27
 */
public class IndicatorView extends View {

    private int mIndicatorWidth;
    private int mHeight;

    private Paint mSelectPaint;
    private Paint mNormalPaint;

    private float mSpace;
    private float mRadius;
    private float mRectFirstLeft;       //第一个rect左边坐标
    private float mRectFirstRight;      //第一个rect右边坐标
    private float mRectSecondLeft;      //第二个rect左边坐标
    private float mRectSecondRight;     //第二个rect右边坐标

    private RectF mFirstRectF  = new RectF();
    private RectF mSecondRectF = new RectF();

    private static final int INDICATOR_RAW = 16;    //以设计图dp标识的原始值
    private static final int HEIGHT_RAW = 4;
    private static final int RADIUS_RAW = 2;

    private static final String selectColor = "#FF5C5E8A";
    private static final String normalColor = "#FFB8C0CC";

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIndicatorWidth = (int) (context.getResources().getDisplayMetrics().density * INDICATOR_RAW);
        mHeight = (int) (context.getResources().getDisplayMetrics().density * HEIGHT_RAW);
        mRadius = (context.getResources().getDisplayMetrics().density * RADIUS_RAW);
        mSpace = mHeight;

        mNormalPaint = new Paint();
        mNormalPaint.setAntiAlias(true);
        mNormalPaint.setColor(Color.parseColor(normalColor));
        mNormalPaint.setStyle(Paint.Style.FILL);

        mSelectPaint = new Paint();
        mSelectPaint.setAntiAlias(true);
        mSelectPaint.setColor(Color.parseColor(selectColor));
        mSelectPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measuredWidth, mHeight);
        }

        mRectFirstLeft = (measuredWidth - mIndicatorWidth - mSpace - mHeight) / 2;
        mRectFirstRight = mRectFirstLeft + mIndicatorWidth;

        mRectSecondLeft = mRectFirstRight + mSpace;
        mRectSecondRight = mRectSecondLeft + mHeight;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mFirstRectF.set(mRectFirstLeft, 0, mRectFirstRight, mHeight);
        canvas.drawRoundRect(mFirstRectF, mRadius, mRadius, mSelectPaint);

        mSecondRectF.set(mRectSecondLeft, 0, mRectSecondRight, mHeight);
        canvas.drawRoundRect(mSecondRectF, mRadius, mRadius, mNormalPaint);
    }

    public void setNewPosition(float ratio) {
        mRectFirstRight = Math.max(mRectFirstLeft + mIndicatorWidth * (1 - ratio), mRectFirstLeft + mHeight);
        mRectSecondLeft = Math.min(mRectSecondRight - ratio * mIndicatorWidth, mRectSecondRight - mHeight);

        mSelectPaint.setColor(Color.parseColor(ColorUtil.caculateColor(selectColor, normalColor,
                ratio)));
        mNormalPaint.setColor(Color.parseColor(ColorUtil.caculateColor(normalColor, selectColor,
                ratio)));
        invalidate();
    }

}
