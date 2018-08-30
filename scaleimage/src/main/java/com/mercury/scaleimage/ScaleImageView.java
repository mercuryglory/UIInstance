package com.mercury.scaleimage;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * @author Mercury
 * @descript
 * @since 2018/8/25
 */
public class ScaleImageView extends AppCompatImageView implements View.OnLayoutChangeListener {

    public static final String TAG = "ScaleImageView";

    private Matrix               mScaleMatrix;
    private GestureDetector      mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private float defaultScale;
    private float doubleScale;
    private float maxScale;
    private float minScale;

    private float mWidth;
    private float mHeight;

    private float mLastX;
    private float mLastY;

    private float mLastDisX;
    private float mLastDisY;

    private boolean mIsDragging;
    private float   mTouchSlop;

    private boolean mPointerScaling;
    private float prevScale;

    private int mScrollEdge = EDGE_BOTH;

    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_BOTH = 2;

    public ScaleImageView(Context context) {
        this(context, null);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);

        mGestureDetector = new GestureDetector(context, new GestureDetector
                .SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                final float x = e.getX();
                final float y = e.getY();
                float currentScale = getScale();
                float targetScale;
                if (currentScale > defaultScale) {
                    //缩小到原状
                    targetScale = defaultScale;
                } else {
                    //放大到双击最大值
                    targetScale = doubleScale;
                }

                Log.i(TAG, "currentScale: " + currentScale + ",targetScale: " + targetScale);
                updateScaleSmoothly(currentScale, targetScale, x, y);
                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector
                .OnScaleGestureListener() {


            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }
                if (getScale() < minScale) {
                    return false;
                }
                setScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addOnLayoutChangeListener(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnLayoutChangeListener(this);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return false;
        }
        mScaleGestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();
                final float dx = x - mLastX, dy = y - mLastY;

                if (!mIsDragging) {
                    mIsDragging = Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
                }

                if (mIsDragging && !mPointerScaling) {

                    //相比原图片已经放大了 需要可移动,如果正在缩放，则不可移动
                    if (getScale() > defaultScale) {
                        mScaleMatrix.postTranslate(dx, dy);
                        checkAndDisplayMatrix();

//                        Log.i(TAG, "move_edge: " + mScrollEdge);

                    }
                }
                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mPointerScaling = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                int pointerCount = event.getPointerCount();
                if (pointerCount == 2) {
                    mLastX = (event.getX(0) + event.getX(1)) / 2;
                    mLastY = (event.getY(0) + event.getY(1) / 2);
                }
                mPointerScaling = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mLastX = event.getX();
                mLastY = event.getY();

                if (getScale() > maxScale) {
                    setScale(maxScale / getScale(), event.getX(), event.getY());
                }

                if (getScale() < defaultScale) {
                    updateScaleSmoothly(getScale(), defaultScale, event.getX(), event.getY());
                }
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "dispatchTouchEvent: down");
                requestDisallowIntercept(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastDisX;
                float dy = y - mLastDisY;

                checkAndDisplayMatrix();
//                final RectF rectF = getMatrixRectF();
//                if (rectF == null) {
//                    return false;
//                }
//
//                final float width = rectF.width();
//
//                final int viewWidth = getImageViewWidth();
//
//                Log.i(TAG, "width: " + width + ",viewWidth: " + viewWidth + ",left: " + rectF.left
//                        + ",right:" + rectF.right);
//
//                if (width <= viewWidth) {
//                    mScrollEdge = EDGE_BOTH;
//                } else if (rectF.left > 0) {
//                    mScrollEdge = EDGE_LEFT;
//                } else if (rectF.right < viewWidth) {
//                    mScrollEdge = EDGE_RIGHT;
//                } else {
//                    mScrollEdge = EDGE_NONE;
//                }
//
//                if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_LEFT && dx >= 1f) ||
//                        (mScrollEdge == EDGE_RIGHT && dx <= 1f)) {
//                    requestDisallowIntercept(false);
//                } else {
//                    requestDisallowIntercept(true);
//                }
                Log.i(TAG, "dispatchTouchEvent: move");
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "dispatchTouchEvent: up");
                break;
        }

        mLastDisX = x;
        mLastDisY = y;


        return super.dispatchTouchEvent(event);
    }

    private void requestDisallowIntercept(boolean request) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(request);
        }
    }

    private void updateScaleSmoothly(float currentScale, float targetScale, final float x, final float y) {
        ValueAnimator animator = ValueAnimator.ofFloat(currentScale, targetScale);
        prevScale = currentScale;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                mScaleMatrix.postScale(animatedValue / prevScale, animatedValue / prevScale, x, y);
                prevScale = animatedValue;
                checkAndDisplayMatrix();
            }
        });
        animator.setDuration(300).start();
    }

    private int getImageViewWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    //获取ImageView本身占据的实际大小
    private int getImageViewHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private void setScale(float scale, float pivotX, float pivotY) {
        mScaleMatrix.postScale(scale, scale, pivotX, pivotY);
        checkAndDisplayMatrix();
    }

    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageMatrix(mScaleMatrix);
        }
    }

    private boolean checkMatrixBounds() {
        final RectF rectF = getMatrixRectF();
        if (rectF == null) {
            return false;
        }

        final float width = rectF.width(), height = rectF.height();
        float deltaX = 0, deltaY = 0;

        final int viewWidth = getImageViewWidth();
        final int viewHeight = getImageViewHeight();

        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - rectF.top;
        } else if (rectF.top > 0) {
            deltaY = -rectF.top;
        } else if (rectF.bottom < viewHeight) {
            deltaY = viewHeight - rectF.bottom;
        }

        Log.i(TAG, "width: " + width + ",viewWidth: " + viewWidth + ",left: " + rectF.left + "," +
                "right:" + rectF.right);

        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - rectF.left;
            mScrollEdge = EDGE_BOTH;
        } else if (rectF.left > 0) {
            deltaX = -rectF.left;
            mScrollEdge = EDGE_LEFT;
        } else if (rectF.right < viewWidth) {
            deltaX = viewWidth - rectF.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];

    }

    //可以获取图片缩放后的宽高
    private RectF getMatrixRectF() {
        Matrix scaleMatrix = mScaleMatrix;
        RectF rectF = new RectF();
        if (getDrawable() != null) {
            rectF.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            scaleMatrix.mapRect(rectF);
            return rectF;
        }
        return null;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int
            oldTop, int oldRight, int oldBottom) {
        //The width of this View
        mWidth = getImageViewWidth();
        mHeight = getImageViewHeight();

        Drawable drawable = getDrawable();
        if (drawable == null)
            return;

        //The width of the source image
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        float scale = 1.0f;

        //图片宽度大于控件宽度，图片高度小于等于控件高度
        if (imageWidth >= mWidth && imageHeight <= mHeight) {
            scale = mWidth / imageWidth;

            doubleScale = mHeight / (imageHeight * scale) * scale;
        }

        //图片高度大于控件高度，图片宽度小于等于控件宽度
        if (imageHeight >= mHeight && imageWidth <= mWidth) {
            scale = mHeight / imageHeight;
            doubleScale = mWidth / (imageWidth * scale) * scale;
        }

        //图片高度和宽度都大于控件高度宽度
        if ((imageWidth >= mWidth && imageHeight >= mHeight)) {
            scale = Math.min(mWidth / imageWidth, mHeight / imageHeight);
            doubleScale = scale * 2;
        }

        //图片高度和宽度都小于控件高度宽度
        if (imageWidth < mWidth && imageHeight < mHeight) {
            scale = Math.min(mWidth / imageWidth, mHeight / imageHeight);
            doubleScale = Math.max(mWidth / (imageWidth * scale), mHeight /
                    (imageHeight * scale)) * scale;
        }

        defaultScale = scale;
        maxScale = doubleScale * 2;
        minScale = defaultScale * 0.8f;

        float dx = mWidth / 2 - imageWidth / 2;
        float dy = mHeight / 2 - imageHeight / 2;
        mScaleMatrix.reset();
        mScaleMatrix.postTranslate(dx, dy);
        mScaleMatrix.postScale(scale, scale, mWidth / 2, mHeight / 2);
        setImageMatrix(mScaleMatrix);

    }
}
