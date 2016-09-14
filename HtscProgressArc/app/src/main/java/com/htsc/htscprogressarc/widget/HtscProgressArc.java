package com.htsc.htscprogressarc.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.htsc.htscprogressarc.R;

/**
 * 扇形饼图
 * Created by zhangxiaoting on 16/9/5.
 */
public class HtscProgressArc extends View {

    private Paint mPaint;
    private int mArcWidth;
    private int mArcBackgroundColor;
    private int mShopOnlineProgress;
    private int mShopOnlineColor;
    private int mShopOfflineProgress;
    private int mLifeFeeProgress;
    private int max = 100;

    public HtscProgressArc(Context context) {
        this(context, null);
    }

    public HtscProgressArc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HtscProgressArc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcProgress);

        mArcWidth = (int) typedArray.getDimension(R.styleable.ArcProgress_arcProgressWidth, 150);
        mShopOnlineColor = typedArray.getColor(R.styleable.ArcProgress_arcProgressColor, getResources().getColor(R.color.colorShopOnline));
        mArcBackgroundColor = typedArray.getColor(R.styleable.ArcProgress_arcProgressBgColor, Color.GRAY);

        typedArray.recycle();
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.mShopOnlineProgress = progress;
            postInvalidate();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centre = getWidth() / 2; // 这是饼形圆心
        int radius = centre - mArcWidth / 2; // 半径

        mPaint.setColor(mArcBackgroundColor); // 设置圆环的颜色
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(mArcWidth); // 设置圆环的宽度
        mPaint.setAntiAlias(true); // 消除锯齿
        canvas.drawCircle(centre, centre, radius, mPaint); // 画出圆环

        Log.e("log", centre + "");

        // 设置进度是实心还是空心
        mPaint.setStrokeWidth(mArcWidth); // 设置圆环的宽度
        mPaint.setColor(mShopOnlineColor); // 设置进度的颜色
        RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

        mPaint.setStyle(Paint.Style.STROKE);
        if (mShopOnlineProgress != 0) {
            canvas.drawArc(oval, -90, 360 * mShopOnlineProgress / max, false, mPaint); // 根据进度画圆弧
        }

        mPaint.setColor(getResources().getColor(R.color.colorShopOffline));
        canvas.drawArc(oval, (-90 + 360 * mShopOnlineProgress / max), 360 * 40 / max, false, mPaint);

        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        canvas.drawArc(oval, (-90 + 360 * (mShopOnlineProgress + 40) / max), 360 * 30 / max, false, mPaint);


    }
}
