package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSelectionDialIndicatorView extends View {
    Paint linePaint;
    float hue = 0.f;

    public ColorSelectionDialIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorSelectionDialIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.rotate(hue, getCenterX(), getCenterY());
        canvas.drawLine(getCenterX(), getRadius() * 0.2f, getCenterX(), 0.f, linePaint);
        canvas.restore();
    }

    private float getCenterX() {
        return getMeasuredWidth()/2;
    }

    private float getCenterY() {
        return getMeasuredHeight()/2;
    }

    private float getRadius() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight())/2;
    }

    public float getAngle(MotionEvent event) {
        float deltaX = event.getX() - getCenterX();
        float deltaY = event.getY() - getCenterY();
        float angle = (float)Math.atan2(deltaY, deltaX);
        angle = (float)Math.toDegrees(angle);
        return angle;
    }

    public void pointIndicator(float hue) {
        this.hue = hue;
        invalidate();
    }
}
