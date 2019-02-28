package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;

/* DrawSheet is like a clear sheet of plastic that can be painted on */
public class DrawSheet {
    private Bitmap permanentBitmap; //bitmap for storing permanent edits that cannot be undone
    private Canvas permanentBitmapCanvas;
    private Bitmap displayBitmap; //bitmap for displaying the DRAW sheet
    private Canvas displayBitmapCanvas;

    //buffer for storing edits that can still be undone
    private LinkedList<DrawEdit> editHistory;
    private int editIndex = 0;
    private final int MAX_NUM_EDITS = 10;

    private Context context;
    private int width;
    private int height;

    private boolean activated;
    private boolean dirty;

    public DrawSheet(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;

        editHistory = new LinkedList<>();
        activated = false;
        dirty = false;
    }

    public void activate(Bitmap bitmap) {
        Log.i("drawSheet", "drawSheet activated");
        activated = true;

        permanentBitmap = bitmap;
        permanentBitmapCanvas = new Canvas(bitmap);
        displayBitmap = Bitmap.createBitmap(bitmap);
        displayBitmapCanvas = new Canvas(displayBitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        activate(bitmap);
    }

    public void deactivate() {
        activated = false;

        if (permanentBitmap != null) {
            permanentBitmap.recycle();
        }
        permanentBitmap = null;
        permanentBitmapCanvas = null;

        if (displayBitmap != null) {
            displayBitmap.recycle();
        }
        displayBitmap = null;
        displayBitmapCanvas = null;

        clearEditHistory();
    }

    private void updateDisplayBitmap() {
        if (!activated) { return; }

        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        displayBitmapCanvas.drawBitmap(permanentBitmap, 0, 0, null);
        for (int i = 0; i < editIndex; ++i) {
            displayBitmapCanvas.drawPath(editHistory.get(i).getPath(), editHistory.get(i).getPaint());
        }
    }

    private void clearEditHistory() {
        editHistory.clear();
        editIndex = 0;
    }

    public void drawSheetToCanvas(Canvas canvas, @Nullable Rect src, @Nullable Rect dest) {
        if (activated) {
            if (src == null || dest == null) {
                canvas.drawBitmap(displayBitmap, 0, 0, null);
            } else {
                canvas.drawBitmap(displayBitmap, src, dest, null);
            }
        }

//        for (int i = 0; i < editHistory.size(); ++i) {
//            Path path = editHistory.get(i).getPath();
//            Paint paint = editHistory.get(i).getPaint();
//            canvas.drawPath(path, paint);
//        }
    }

    public Bitmap getBitmap() { return displayBitmap; }

    public void drawSheetToCanvas(Canvas canvas, @Nullable Matrix matrix) {
        if (activated) {
            if (matrix == null) {
                canvas.drawBitmap(permanentBitmap, 0, 0, null);
            } else {
                canvas.drawBitmap(permanentBitmap, matrix, null);
            }
        }

        if (matrix == null) {
            for (int i = 0; i < editHistory.size(); ++i) {
                Path path = editHistory.get(i).getPath();
                Paint paint = editHistory.get(i).getPaint();
                canvas.drawPath(path, paint);
            }
        } else {
            Matrix inverse = new Matrix();
            matrix.invert(inverse);
            float[] f = new float[9];
            matrix.getValues(f);
            float scale = f[Matrix.MSCALE_X];

            for (int i = 0; i < editHistory.size(); ++i) {
                Path path = editHistory.get(i).getPath();
                Paint paint = editHistory.get(i).getPaint();
                path.transform(matrix);
                float oldWidth = paint.getStrokeWidth();
                paint.setStrokeWidth(oldWidth * scale);
                canvas.drawPath(path, paint);
                path.transform(inverse);
                paint.setStrokeWidth(oldWidth);
            }
        }
    }

    public void drawSheetToCanvas(Canvas canvas, float scale) {
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(1.f/scale, 1.f/scale);
        Matrix inverse = new Matrix();
        scaleMatrix.invert(inverse);

        if (activated) {
            canvas.drawBitmap(permanentBitmap, scaleMatrix, null);
        }

        for (int i = 0; i < editHistory.size(); ++i) {
            Path path = editHistory.get(i).getPath();
            Paint paint = editHistory.get(i).getPaint();
            path.transform(scaleMatrix);
            float oldWidth = paint.getStrokeWidth();
            paint.setStrokeWidth(oldWidth / scale);
            canvas.drawPath(path, paint);
            path.transform(inverse);
            paint.setStrokeWidth(oldWidth);
        }
    }

    public void makeEdit(DrawEdit edit) {
        dirty = true;

        edit = new DrawEdit(edit);

        while (editHistory.size() - editIndex > 0) {
            editHistory.removeLast();
        }

        if (editHistory.size() == MAX_NUM_EDITS) {
            DrawEdit oldestEdit = editHistory.getFirst();
            editHistory.removeFirst();
            editIndex -= 1;

            if (activated) {
                permanentBitmapCanvas.drawPath(oldestEdit.getPath(), oldestEdit.getPaint());
            }
        }

        editHistory.addLast(edit);
        editIndex += 1;

        updateDisplayBitmap();
    }

    //make the path consistent with the displayBitmap by removing the transformation/scaling
    private void adjustPath(Path path, Matrix currentWindowTransformation) {
        Matrix inverseMatrix = new Matrix(currentWindowTransformation);
        inverseMatrix.invert(inverseMatrix);
        path.transform(inverseMatrix);
    }

    public void undoEdit() {
        if (editIndex == 0) {
            Toast.makeText(context, "out of undos", Toast.LENGTH_SHORT).show();
        } else {
            editIndex -= 1;
        }

        updateDisplayBitmap();
    }

    public void redoEdit() {
        if (editIndex == editHistory.size()) {
            Toast.makeText(context, "nothing to redo", Toast.LENGTH_SHORT).show();
        } else {
            editIndex += 1;
        }

        updateDisplayBitmap();
    }

    public void clear() {
        clearEditHistory();

        if (!activated) { return; }

        permanentBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        updateDisplayBitmap();
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markAsClean() {
        dirty = false;
    }
}

//package com.arduk.animationcreator;
//
//        import android.content.Context;
//        import android.graphics.Bitmap;
//        import android.graphics.Canvas;
//        import android.graphics.Color;
//        import android.graphics.Matrix;
//        import android.graphics.Path;
//        import android.graphics.PorterDuff;
//        import android.util.Log;
//        import android.widget.Toast;
//
//        import java.util.LinkedList;
//
///* DrawSheet is like a clear sheet of plastic that can be painted on */
//public class DrawSheet {
//    private Bitmap permanentBitmap; //bitmap for storing permanent edits that cannot be undone
//    private Canvas permanentBitmapCanvas;
//    private Bitmap displayBitmap; //bitmap for displaying the DRAW sheet
//    private Canvas displayBitmapCanvas;
//
//    //buffer for storing edits that can still be undone
//    private LinkedList<DrawEdit> editHistory;
//    private int editIndex = 0;
//    private final int MAX_NUM_EDITS = 10;
//
//    private Context context;
//    private int width;
//    private int height;
//
//    private boolean activated;
//
//    public DrawSheet(Context context, int width, int height) {
//        this.context = context;
//        this.width = width;
//        this.height = height;
//
//        editHistory = new LinkedList<>();
//        activated = false;
//    }
//
//    public void activate(Bitmap bitmap) {
//        activated = true;
//
//        permanentBitmap = bitmap;
//        permanentBitmapCanvas = new Canvas(bitmap);
//
//        displayBitmap = Bitmap.createBitmap(bitmap);
//        displayBitmapCanvas = new Canvas(displayBitmap);
//    }
//
//    public void setBitmap(Bitmap bitmap) {
//        activate(bitmap);
//    }
//
//    public void deactivate() {
//        activated = false;
//
//        if (permanentBitmap != null) {
//            permanentBitmap.recycle();
//        }
//        permanentBitmap = null;
//        permanentBitmapCanvas = null;
//
//        if (displayBitmap != null) {
//            displayBitmap.recycle();
//        }
//        displayBitmap = null;
//        displayBitmapCanvas = null;
//
//        clearEditHistory();
//    }
//
//    public Bitmap getBitmap() {
//        return displayBitmap;
//    }
//
//    private void updateDisplayBitmap() {
//        if (!activated) { return; }
//
//        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        displayBitmapCanvas.drawBitmap(permanentBitmap, 0, 0, null);
//        for (int i = 0; i < editIndex; ++i) {
//            displayBitmapCanvas.drawPath(editHistory.get(i).getPath(), editHistory.get(i).getPaint());
//        }
//    }
//
//    private void clearEditHistory() {
//        editHistory.clear();
//        editIndex = 0;
//    }
//
//    public void drawSheetToCanvas(Canvas canvas) {
//        if (!activated) { return; }
//
//        canvas.drawBitmap(displayBitmap, 0, 0, null);
//    }
//
//    public void makeEdit(DrawEdit edit, Matrix currentWindowTransformation) {
//        Log.i("drawsheet", "edit made!");
//        edit = new DrawEdit(edit);
//        adjustPath(edit.getPath(), currentWindowTransformation);
//
//        while (editHistory.size() - editIndex > 0) {
//            editHistory.removeLast();
//        }
//
//        if (editHistory.size() == MAX_NUM_EDITS) {
//            DrawEdit oldestEdit = editHistory.getFirst();
//            editHistory.removeFirst();
//            editIndex -= 1;
//
//            if (activated) {
//                permanentBitmapCanvas.drawPath(oldestEdit.getPath(), oldestEdit.getPaint());
//            }
//        }
//
//        editHistory.addLast(edit);
//        editIndex += 1;
//
//        updateDisplayBitmap();
//    }
//
//    //make the path consistent with the displayBitmap by removing the transformation/scaling
//    private void adjustPath(Path path, Matrix currentWindowTransformation) {
//        Matrix inverseMatrix = new Matrix(currentWindowTransformation);
//        inverseMatrix.invert(inverseMatrix);
//        path.transform(inverseMatrix);
//    }
//
//    public void undoEdit() {
//        if (editIndex == 0) {
//            Toast.makeText(context, "out of undos", Toast.LENGTH_SHORT).show();
//        } else {
//            editIndex -= 1;
//        }
//
//        updateDisplayBitmap();
//    }
//
//    public void redoEdit() {
//        if (editIndex == editHistory.size()) {
//            Toast.makeText(context, "nothing to redo", Toast.LENGTH_SHORT).show();
//        } else {
//            editIndex += 1;
//        }
//
//        updateDisplayBitmap();
//    }
//
//    public void clear() {
//        clearEditHistory();
//
//        if (!activated) { return; }
//        permanentBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//        updateDisplayBitmap();
//    }
//
//    public boolean isActivated() {
//        return activated;
//    }
//}