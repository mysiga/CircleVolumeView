package com.mysiga.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.mysiga.lib.R;

/**
 * CircleVolumeView
 */
public class CircleVolumeView extends View {

    /**
     * degree count
     */
    private static final int DEGREE_COUNT = 36;

    /**
     * no show degree count
     */
    private static final int NO_SHOW_DEGREE_COUNT = 8;

    private static final int SHIELD_RADIUS = 30;
    /**
     * add min and max touch area,20
     */
    private static final int ADD_TOUCH_AREA = 30;

    private VolumeCallback mVolumeCallback;
    private int mArcRadius;
    private Paint mPaintDegree;
    private int mVolumeRadius;
    private int mMaxRadius;
    private int mCurrentDegrees = 0;
    private double mVolumeX = 0;
    private double mVolumeY = 0;
    private float mMinDegree;
    private boolean isMove = true;
    private Bitmap mBitmap;
    private Bitmap mCircleBitmap;
    /**
     * circular spacing
     */
    private final int mCircularSpacing;
    /**
     * degree width
     */
    private final int mDegreeWidth;
    private double mAcos;

    public CircleVolumeView(Context context) {
        this(context, null);
    }

    public CircleVolumeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleVolumeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaintDegree = new Paint();
        mPaintDegree.setColor(getResources().getColor(R.color.volumeDegree));
        mPaintDegree.setAntiAlias(true);
        mCircularSpacing = dp2px(22);
        mDegreeWidth = dp2px(5);
        mMinDegree = 360 / DEGREE_COUNT;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mMaxRadius == 0) {
            mMaxRadius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
            mArcRadius = mCircleBitmap.getWidth() / 2;
            mVolumeRadius = mArcRadius - mCircularSpacing;
        }

        //paint no check paint
        mPaintDegree.setStrokeWidth(2);
        for (int i = 0; i < DEGREE_COUNT; i++) {
            //remove bottom no show degree :14<xx<22
            if (i <= 14 || i >= 22) {
                float lineY = mMaxRadius - mArcRadius - mDegreeWidth;
                //show touch degrees logic
                int more = mCurrentDegrees + 22 - DEGREE_COUNT;
                if (more >= 0 && (i <= more || i >= 22)) {
                    //0.6*255
                    mPaintDegree.setAlpha(153);
                } else if (more < 0 && mCurrentDegrees >= 0 && i >= 22 && i - 22 <= mCurrentDegrees) {
                    //0.6*255
                    mPaintDegree.setAlpha(153);
                } else {
                    //0.2*255
                    mPaintDegree.setAlpha(51);
                }

                canvas.drawLine(mMaxRadius, lineY - mDegreeWidth, mMaxRadius,
                        lineY, mPaintDegree);
            }
//            canvas.drawText(""+i,mMaxRadius,1,mPaintDegree);
            //Around xx(getWidth()/2,getHeight()/2) the rotation
            canvas.rotate(mMinDegree, mMaxRadius, mMaxRadius);
        }


        mPaintDegree.setAlpha(255);
        canvas.drawBitmap(mCircleBitmap, mMaxRadius - mArcRadius, mMaxRadius - mArcRadius, mPaintDegree);

        drawVolume(canvas);

        mPaintDegree.setAlpha(76);
        mPaintDegree.setStrokeWidth(1);
        mPaintDegree.setTextSize(getResources().getDimensionPixelSize(R.dimen.size_volume_text));
        canvas.drawText("MIN", 0, mMaxRadius + mArcRadius, mPaintDegree);
        canvas.drawText("MAX", mMaxRadius + mArcRadius, mMaxRadius + mArcRadius, mPaintDegree);

    }

    private void drawVolume(Canvas canvas) {
        if (mAcos <= 90 - NO_SHOW_DEGREE_COUNT / 2 * mMinDegree && mAcos >= 0) {
            if (mAcos == 0) {
                mVolumeX = mMaxRadius - Math.sin(NO_SHOW_DEGREE_COUNT * Math.toRadians(mMinDegree) / 2) * mVolumeRadius;
                mVolumeY = mMaxRadius + Math.cos(NO_SHOW_DEGREE_COUNT * Math.toRadians(mMinDegree) / 2) * mVolumeRadius;
            } else {
                double acos = mAcos + NO_SHOW_DEGREE_COUNT / 2 * mMinDegree;
                mVolumeX = mMaxRadius - mVolumeRadius * Math.sin(Math.toRadians(acos));
                mVolumeY = mMaxRadius + mVolumeRadius * Math.cos(Math.toRadians(acos));
            }
        } else if (mAcos > 90 - NO_SHOW_DEGREE_COUNT / 2 * mMinDegree && mAcos <= 180 - NO_SHOW_DEGREE_COUNT / 2 * mMinDegree) {
            double acos = mAcos + NO_SHOW_DEGREE_COUNT / 2 * mMinDegree - 90;
            mVolumeX = mMaxRadius - mVolumeRadius * Math.cos(Math.toRadians(acos));
            mVolumeY = mMaxRadius - mVolumeRadius * Math.sin(Math.toRadians(acos));
        } else if (mAcos > 180 - NO_SHOW_DEGREE_COUNT / 2 * mMinDegree && mAcos <= 270 - NO_SHOW_DEGREE_COUNT / 2 * mMinDegree) {
            double acos = mAcos + NO_SHOW_DEGREE_COUNT / 2 * mMinDegree - 180;
            mVolumeX = mMaxRadius + mVolumeRadius * Math.sin(Math.toRadians(acos));
            mVolumeY = mMaxRadius - mVolumeRadius * Math.cos(Math.toRadians(acos));
        } else {
            double acos = mAcos + NO_SHOW_DEGREE_COUNT / 2 * mMinDegree - 270;
            mVolumeX = mMaxRadius + mVolumeRadius * Math.cos(Math.toRadians(acos));
            mVolumeY = mMaxRadius + mVolumeRadius * Math.sin(Math.toRadians(acos));
        }
        canvas.drawBitmap(mBitmap, (float) mVolumeX, (float) mVolumeY, mPaintDegree);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMove = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMove) {
                    return false;
                }
                float firstX = event.getX();
                float firstY = event.getY();
                float x = firstX > mMaxRadius ? firstX - mMaxRadius : mMaxRadius - firstX;
                float y = firstY > mMaxRadius ? firstY - mMaxRadius : mMaxRadius - firstY;
                double sqrt = Math.sqrt(x * x + y * y);
                if (sqrt <= (mVolumeRadius - SHIELD_RADIUS)) {
                    isMove = false;
                    return false;
                }

                double xScale = x / sqrt;
                double acos = Math.toDegrees(Math.acos(xScale));
                //where quadrant
                if (firstX <= mMaxRadius && firstY >= mMaxRadius) {
                    //Second quadrant
                    acos = 90 - acos;
                    if (acos <= mMinDegree * NO_SHOW_DEGREE_COUNT / 2) {
                        if (acos < mMinDegree * NO_SHOW_DEGREE_COUNT / 2 - ADD_TOUCH_AREA) {
                            isMove = false;
                            return false;
                        }
                        acos = 0;
                    } else {
                        acos = acos - mMinDegree * NO_SHOW_DEGREE_COUNT / 2;
                    }
                } else if (firstX < mMaxRadius && firstY < mMaxRadius) {
//                    Third quadrant
                    acos = acos + 90 - mMinDegree * NO_SHOW_DEGREE_COUNT / 2;
                } else if (firstX > mMaxRadius && firstY < mMaxRadius) {
                    //Fourth Quadrant
                    acos = 270 - acos - mMinDegree * NO_SHOW_DEGREE_COUNT / 2;
                } else {
                    //First quadrant
                    if (acos > mMinDegree * 5) {
                        if (acos > mMinDegree * 5 + ADD_TOUCH_AREA) {
                            isMove = false;
                            return false;
                        }
                        acos = mMinDegree * 5;
                    }
                    acos = acos + 270 - mMinDegree * NO_SHOW_DEGREE_COUNT / 2;

                }
                mAcos = acos;
                mCurrentDegrees = (int) (acos / mMinDegree);
                if (mVolumeCallback != null) {
                    mVolumeCallback.currentDegrees(mCurrentDegrees, totalDegrees());
                }
                invalidate();
                return true;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nav_volume_highlight);
        mCircleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nav_volume_bg);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mCircleBitmap != null && !mCircleBitmap.isRecycled()) {
            mCircleBitmap.recycle();
            mCircleBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * @param volumeCallback
     */
    public void setCallback(VolumeCallback volumeCallback) {
        this.mVolumeCallback = volumeCallback;
    }

    /**
     * set first degrees
     *
     * @param currentVolume
     * @param maxVolume
     */
    public void setDegrees(int currentVolume, int maxVolume) {
        mCurrentDegrees = (currentVolume * totalDegrees()) / maxVolume;
        mAcos = mCurrentDegrees * mMinDegree;
        invalidate();
    }

    /**
     * get current degrees
     *
     * @return
     */
    public int currentDegrees() {
        return mCurrentDegrees;
    }

    /**
     * total valid degrees
     *
     * @return
     */
    public int totalDegrees() {
        return DEGREE_COUNT - NO_SHOW_DEGREE_COUNT;
    }

    /**
     * move volume callback
     */
    public interface VolumeCallback {
        /**
         * current degrees
         *
         * @param currentDegrees
         */
        void currentDegrees(int currentDegrees, int maxDegrees);
    }

    /**
     * dp to px,
     *
     * @param dpValue
     * @return
     */
    private int dp2px(float dpValue) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dpValue * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}