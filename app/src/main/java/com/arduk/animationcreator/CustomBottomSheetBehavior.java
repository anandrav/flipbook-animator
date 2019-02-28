package com.arduk.animationcreator;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomBottomSheetBehavior extends BottomSheetBehavior {

    public CustomBottomSheetBehavior() {
        super();
    }

    public CustomBottomSheetBehavior(Context context, AttributeSet attr) {
        super(context, attr);
    }


    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return false;
    }
}
