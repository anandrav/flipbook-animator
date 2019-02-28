

package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/* this is a container/interface for drawSheets that are "stacked"
    on top one another.
*/
public class DrawFrame {
    private Context context;
    private int width;
    private int height;
    private ArrayList<DrawSheet> drawSheets;

    // constructor
    public DrawFrame(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;

        drawSheets = new ArrayList<>();
    }

    // deep copy constructor
    public DrawFrame(DrawFrame copy) {
        this.context = copy.context;
        this.width = copy.width;
        this.height = copy.height;

        this.drawSheets = new ArrayList<>();

        for (int i = 0; i < copy.getNumSheets(); ++i) {
            this.addNewSheet(context, width, height);
            Bitmap newBitmap = Bitmap.createBitmap(copy.getSheet(i).getBitmap());
            this.getSheet(i).activate(newBitmap);
        }
    }

    public void addNewSheet(Context context, int width, int height) {
        drawSheets.add(new DrawSheet(context, width, height));
    }

    public void removeSheet(int index) {
        drawSheets.remove(index);
    }

    public DrawSheet getSheet(int index) {
        return drawSheets.get(index);
    }

    public int getNumSheets() {
        return drawSheets.size();
    }

    public void clearSheets() {
        for (int i = 0; i < drawSheets.size(); ++i) {
            drawSheets.get(i).clear();
        }
    }
}
