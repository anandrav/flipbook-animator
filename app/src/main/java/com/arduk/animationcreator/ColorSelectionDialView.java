package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSelectionDialView extends View {
    Paint gradientPaint;
    Paint transparentPaint;
    Shader colorGradient;
    Bitmap gradientBitmap;
    Canvas gradientCanvas;
    int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};

    public ColorSelectionDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorSelectionDialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        gradientPaint = new Paint();
        gradientPaint.setAntiAlias(true);
        gradientPaint.setDither(true);
        colorGradient = new SweepGradient(getCenterX(), getCenterY(), colors, null);
        gradientPaint.setShader(colorGradient);

        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        gradientBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        gradientCanvas = new Canvas(gradientBitmap);

        gradientCanvas.save();
        gradientCanvas.rotate(-90, getCenterX(), getCenterY());
        gradientCanvas.drawCircle(getCenterX(), getCenterY(), getOuterRadius(), gradientPaint);
        gradientCanvas.drawCircle(getCenterX(), getCenterY(), getInnerRadius(), transparentPaint);
        gradientCanvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(gradientBitmap, 0, 0, gradientPaint);
    }

    private float getCenterX() {
        return getMeasuredWidth()/2;
    }

    private float getCenterY() {
        return getMeasuredHeight()/2;
    }

    private float getOuterRadius() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight())/2;
    }

    private float getInnerRadius() {
        return getOuterRadius() * 0.8f;
    }

    boolean withinBounds(MotionEvent event) {
        float deltaX = event.getX() - getCenterX();
        float deltaY = event.getY() - getCenterY();
        float distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance < getInnerRadius()) { return false; }
        if (distance > getOuterRadius()) { return false; }
        return true;
    }
}
