package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

public class OffsetItemDecoration extends RecyclerView.ItemDecoration {

    private Context ctx;

    public OffsetItemDecoration(Context ctx) {

        this.ctx = ctx;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        ImageView childImage = (ImageView)((ViewGroup)view).getChildAt(0);
        int offset = (int) (getScreenWidth() / (float) (2)) - childImage.getLayoutParams().width / 2;
        Log.d("width", Integer.toString(childImage.getLayoutParams().width));
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (parent.getChildAdapterPosition(view) == 0) {
            //((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin = 0;
            offset -= ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
            setupOutRect(outRect, offset, true);
        } else if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
            //((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin = 0;
            offset -= ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
            setupOutRect(outRect, offset, false);
        }
    }

    private void setupOutRect(Rect rect, int offset, boolean start) {

        if (start) {
            rect.left = offset;
        } else {
            rect.right = offset;
        }
    }

    private int getScreenWidth() {

        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}