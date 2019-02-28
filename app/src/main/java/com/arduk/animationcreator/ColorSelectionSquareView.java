package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSelectionSquareView extends View {
    public float left = 0.f;
    public float top = 0.f;

    final float indicatorWidth = 28.f;

    Paint squarePaint;
    Shader brightnessGradient;
    Paint linePaint;
    final float linePaintWidth = 8.f;

    float[] hsv = {0.f, 1.f, 1.f};

    public ColorSelectionSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorSelectionSquareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        left = (getMeasuredWidth() - getSquareWidth()) / 2;
        top = (getMeasuredHeight() - getSquareWidth()) / 2;

        squarePaint = new Paint();
        brightnessGradient = new LinearGradient(0.f, top, 0.f, getSquareWidth() + top, 0xffffffff, 0xff000000, Shader.TileMode.CLAMP);

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(linePaintWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Shader saturationGradient = new LinearGradient(left, 0.f, getSquareWidth() + left, 0.f, 0xffffffff, Color.HSVToColor(new float[]{hsv[0], 1.f, 1.f}), Shader.TileMode.CLAMP);
        ComposeShader mComposeShader = new ComposeShader(brightnessGradient, saturationGradient, PorterDuff.Mode.MULTIPLY);

        squarePaint.setShader(mComposeShader);

        canvas.drawRect(left, top, getSquareWidth() + left, getSquareWidth() + top, squarePaint);

        canvas.drawCircle(getIndicatorX(), getIndicatorY(), indicatorWidth, linePaint);
        canvas.drawCircle(getIndicatorX(), getIndicatorY(), indicatorWidth - linePaintWidth/2, squarePaint);
    }

    public float getSquareWidth() {
        return getMeasuredWidth() * 0.8f;
    }

    public void setHue(float hue) {
        hsv[0] = hue;
    }

    private float getIndicatorX() {
        return hsv[1] * getSquareWidth() + left;
    }

    private float getIndicatorY() {
        return (1.f - hsv[2]) * getSquareWidth() + top;
    }

    public void setSaturation(float saturation) {
        hsv[1] = saturation;
    }

    public void setLightness(float lightness) {
        hsv[2] = lightness;
    }

    public void updateIndicator(MotionEvent event) {
        hsv[1] = (clampX(event.getX()) - left) / getSquareWidth();
        hsv[2] = 1.f - ((clampY(event.getY()) - top) / getSquareWidth());
    }

    public float getSaturation() {
        return hsv[1];
    }

    public float getLightness() {
        return hsv[2];
    }

    private float clampX(float x) {
        return Math.min(Math.max(left, x), left + getSquareWidth());
    }

    private float clampY(float y) {
        return Math.min(Math.max(top, y), top + getSquareWidth());
    }
}
