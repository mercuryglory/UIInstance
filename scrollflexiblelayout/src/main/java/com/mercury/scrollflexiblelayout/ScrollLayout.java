package com.mercury.scrollflexiblelayout;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wang.zhonghao
 * @date 2018/9/19
 * @descript
 */

public class ScrollLayout extends FrameLayout {

    public static final String TAG = "ScrollLayout";

    private float startX;

    private int minimumVelocity;
    private int maximumVelocity;
    private int mTouchSlop;

    private boolean mIsBeingDragged;

    private VelocityTracker mVelocityTracker;

    private Context mContext;

    private static final float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private static final float INFLEXION = 0.35f;
    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    private final float mPpi;
    private float mPhysicalCoeff;

    private Scroller mScroller = new Scroller(getContext());

    public ScrollLayout(Context context) {
        this(context, null);
    }

    public ScrollLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mContext);
        minimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        maximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();

        mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        mPhysicalCoeff = computeDeceleration(0.84f);
    }

    private float computeDeceleration(float friction) {
        return SensorManager.GRAVITY_EARTH     // g (m/s^2)
                * 39.37f                       // inch/meter
                * mPpi                         // pixels per inch
                * friction;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int averageWidth = ScreenUtil.getScreenWidth(mContext) / 4;
        if (heightMode == MeasureSpec.AT_MOST) {
            int heightSize = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view.getVisibility() != GONE) {
                    heightSize = Math.max(heightSize, view.getMeasuredHeight());
                    int measureSpecWidth = MeasureSpec.makeMeasureSpec(averageWidth, MeasureSpec
                            .EXACTLY);
                    int measureSpecHeight = MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(),
                            MeasureSpec.AT_MOST);
                    view.measure(measureSpecWidth,measureSpecHeight);
                }
            }

            int widthCount = getChildCount() % 4;
            setMeasuredDimension(ScreenUtil.getScreenWidth(mContext) * (widthCount + 1), heightSize);

        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int size = getChildCount();
        int averageSize = ScreenUtil.getScreenWidth(mContext) / 4;

        for (int i = 0; i < size; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != GONE) {
                view.layout(averageSize * i, 0, averageSize * (i + 1), view.getMeasuredHeight());
            }
        }
    }

    private void smoothScrollBy(int deltaX) {
        int scrollX = getScrollX();
        mScroller.startScroll(scrollX, 0, deltaX, 0, 0);
        invalidate();
    }

    private void smoothScrollTo(int destX) {
        int scrollX = getScrollX();
        mScroller.startScroll(scrollX, 0, destX - scrollX, 0, 600);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i(TAG, "l: " + l + ",t: " + t);
        if (l >= 0 && l <= ScreenUtil.getScreenWidth(mContext)) {
            Log.i(TAG, "ratio: " + l * 1.0f / ScreenUtil.getScreenWidth(mContext));
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageChange(l * 1.0f / ScreenUtil.getScreenWidth(mContext));
            }
        }
    }

    public interface OnPageChangeListener{
        void onPageChange(float ratio);
    }

    private OnPageChangeListener mOnPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                mIsBeingDragged = !mScroller.isFinished();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float xDiff = Math.abs(x - startX);
                if (xDiff > mTouchSlop) {
                    startX = x;
                    mIsBeingDragged = true;

                    requestParentDisallowTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                break;

        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();

        float currentX;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                initOrResetVelocityTracker();
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    requestParentDisallowTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                float deltaX = startX - currentX;
                if (mScroller.getCurrX() < 0 || mScroller.getCurrX() > ScreenUtil.getScreenWidth
                        (mContext)) {
                    smoothScrollBy((int) deltaX / 3);
                } else {
                    smoothScrollBy((int) deltaX);
                }
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    requestParentDisallowTouchEvent(true);
                    mIsBeingDragged = true;
                }
                startX = currentX;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int screenWidth = ScreenUtil.getScreenWidth(mContext);
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) > minimumVelocity && (getScrollX() > 0 && getScrollX() <
                        screenWidth)) {
                    fling((int) (-getVelocityByDistance(screenWidth) * Math.signum
                            (mVelocityTracker.getXVelocity())));
                } else {
                    if (mScroller.getCurrX() > screenWidth / 2) {
                        smoothScrollTo(screenWidth);
                    } else {
                        smoothScrollTo(0);
                    }
                }
                Log.i(TAG, "xVelocity: " + xVelocity);
                startX = event.getX();
                recycleVelocityTracker();
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }

    private void fling(int velocityX) {
        int scrollX = getScrollX();
        int screenWidth = ScreenUtil.getScreenWidth(mContext);
        mScroller.fling(scrollX, getScrollY(), velocityX, 0, 0, screenWidth, 0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }


    private int getVelocityByDistance(double distance) {
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        final double l = Math.log(distance / (mFlingFriction * mPhysicalCoeff)) * decelMinusOne /
                DECELERATION_RATE;
        return (int) (Math.exp(l) * mFlingFriction * mPhysicalCoeff / INFLEXION);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void requestParentDisallowTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void setAdapter(ScrollAdapter adapter) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            View view = adapter.onCreateView(ScrollLayout.this);
            if (view.getParent() != null) {
                ((ViewGroup)view.getParent()).removeView(view);
            }
            addView(view);

            adapter.onBindView(view, i);
        }
    }

    public abstract static class ScrollAdapter<T> {

        private List<T> mList = new ArrayList<>();
        protected Context mContext;

        public ScrollAdapter(){}

        public ScrollAdapter(Context Context) {
            this.mContext = Context;
        }

        public void setData(List<T> list) {
            mList.clear();
            mList.addAll(list);
        }

        public abstract void onBindView(View view, int position);

        public abstract View onCreateView(ViewGroup parent);

        public List<T> getData() {
            return mList;
        }

        public T getItem(int position) {
            if (position >= 0) {
                return getData().get(position);
            }
            throw new IllegalStateException("the value of position must above or equals zero");
        }

        public abstract int getItemCount();
    }

}
