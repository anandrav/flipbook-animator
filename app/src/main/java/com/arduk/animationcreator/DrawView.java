package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {
    private Context context;
    private Project project;
    private ProjectFileHandler projectFileHandler;

    private int imageWidth;
    private final int DEFAULT_IMAGE_WIDTH = 1920;
    private int imageHeight;
    private final int DEFAULT_IMAGE_HEIGHT = 1080;

    private RectF editWindowRect; //rectangle that defines where edit window will be drawn
    private Matrix editWindowMatrix; //matrix for storing translations and scales made to the edit window
    private Matrix utilMatrix;

    private final static float mMinZoom = 0.7f;
    private final static float mMaxZoom = 15.f;

    private final static int MIN_EDIT_PATH_LENGTH = 100;

    private CanvasState mCanvasState = CanvasState.NONE;
    public enum CanvasState {
        NONE, DRAW, ZOOM_PAN
    }

    private Paint displayPaint;
    private Paint playbackPaint;
    private Paint borderPaint;
    private final float borderWidth = 5;
    private Paint backgroundPaint; //squarePaint for background of editWindowRect

    private Bitmap displayBitmap;
    private Canvas displayBitmapCanvas;

    private Bitmap auxiliaryBitmap;
    private Canvas auxiliaryBitmapCanvas;

    private Bitmap animationBitmap;
    private Canvas animationBitmapCanvas;

    private DrawEdit currentEdit;
    private Paint brushPaint;
    private Paint eraserPaint;
    private Paint onionSkinPaint;
    private final float DEFAULT_BRUSH_WIDTH = 8;
    private final float DEFAULT_ERASER_WIDTH = 120;
    private final int DEFAULT_COLOR = Color.BLACK;
    private final int DEFAULT_ALPHA = 255;

    private Handler playHandler;
    private boolean isPlayingAnimation = false;
    private int playbackFrame = 0;
    private boolean isOnionSkinning = false;
    private int onionSkinBackward = 1;
    private int onionSkinFoward = 0;
    private int onionSkinTransparency = 100;

    private editorTool currentTool = editorTool.brush;
    public enum editorTool {
        none, brush, eraser, bucket, lasso
    }

    // INITIALIZERS //

    public DrawView(Context context) {
        super(context);
        this.context = context;
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void init() {
        imageWidth = DEFAULT_IMAGE_WIDTH;
        imageHeight = DEFAULT_IMAGE_HEIGHT;

        oldMidpoint = new PointF();
        lastEventPoint = new PointF();

        editWindowRect = new RectF(0, 0, imageWidth, imageHeight);

        editWindowMatrix = new Matrix();
        utilMatrix = new Matrix();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeJoin(Paint.Join.MITER);
        borderPaint.setStrokeCap(Paint.Cap.SQUARE);
        borderPaint.setColor(Color.DKGRAY);
        borderPaint.setStrokeWidth(borderWidth);

        displayPaint = new Paint();
        displayPaint.setAntiAlias(true);
        displayPaint.setFilterBitmap(true);
        displayPaint.setDither(true);

        playbackPaint = new Paint();
        playbackPaint.setAntiAlias(false);
        playbackPaint.setFilterBitmap(false);
        playbackPaint.setDither(false);

        onionSkinPaint = new Paint();
        onionSkinPaint.setAlpha(onionSkinTransparency);

        displayBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        displayBitmapCanvas = new Canvas(displayBitmap);

        auxiliaryBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        auxiliaryBitmapCanvas = new Canvas(auxiliaryBitmap);

        animationBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        animationBitmapCanvas = new Canvas(animationBitmap);

        playHandler = new Handler();

        brushPaint = new Paint();
        brushPaint.setAntiAlias(true);
        brushPaint.setStyle(Paint.Style.STROKE);
        brushPaint.setStrokeJoin(Paint.Join.ROUND);
        brushPaint.setStrokeCap(Paint.Cap.ROUND);
        brushPaint.setColor(DEFAULT_COLOR);
        brushPaint.setAlpha(DEFAULT_ALPHA);
        brushPaint.setStrokeWidth(DEFAULT_BRUSH_WIDTH);

        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setStrokeWidth(DEFAULT_ERASER_WIDTH);
        eraserPaint.setColor(Color.BLACK);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        currentEdit = new DrawEdit(currentTool, new Path(), new Paint());
        setCurrentTool(editorTool.brush);

        updateDisplayBitmap();
        invalidate();
    }

    public void attachToProject(Project project) {
        this.project = project;
        projectFileHandler = ProjectFileHandler.getInstance();

        this.imageWidth = project.getWidth();
        this.imageHeight = project.getHeight();

        displayBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        displayBitmapCanvas = new Canvas(displayBitmap);

        auxiliaryBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        auxiliaryBitmapCanvas = new Canvas(auxiliaryBitmap);

        animationBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        animationBitmapCanvas = new Canvas(animationBitmap);

        updateDisplayBitmap();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setStartingPosition(w, h);
    }

    public void setStartingPosition(int width, int height) {
        //FIXME don't use absolute pixel values, use dp
        final float windowMargin = borderWidth;
        float scale = (float)width / (imageWidth + windowMargin*2);

        float dy = ((float)height - imageHeight) / 2;

        editWindowMatrix.postScale(scale, scale);
        editWindowMatrix.postTranslate(windowMargin*scale, dy);

        editWindowRect.set(0, 0, imageWidth, imageHeight);
        editWindowMatrix.mapRect(editWindowRect);
    }

    // RENDER METHODS //

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //fill canvas with solid color (light gray)
        canvas.drawColor(Color.rgb(225, 225, 225));

        //DRAW border around canvas
        canvas.drawRect(editWindowRect, borderPaint);

        //DRAW background behind drawLayer
        canvas.drawRect(editWindowRect, backgroundPaint);

        //DRAW the display bitmap
        canvas.drawBitmap(displayBitmap, null, editWindowRect, displayPaint);

        // draw circle for eraser
        if (currentTool == editorTool.eraser && mCanvasState == CanvasState.DRAW) {
            canvas.drawCircle(lastEventPoint.x, lastEventPoint.y,
                    eraserPaint.getStrokeWidth() * getCurrentZoom()/2, borderPaint);
        }
    }

    private void updateDisplayBitmap() {
        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (project == null) { return; }

        //onion skinning backward
        if (isOnionSkinning) {
            for (int frame = project.getSelectedFrame() - onionSkinBackward;
                 frame < project.getSelectedFrame() && frame >= 0; ++frame) {
                for (int layer = project.getNumLayers()-1; layer >= 0; --layer) {
                    Bitmap bitmap = project.getDrawSheetObject(frame, layer).getBitmap();
                    if (bitmap != null) {
                        displayBitmapCanvas.drawBitmap(bitmap, 0, 0, onionSkinPaint);
                    }
                }
            }
        }

        int frame = project.getSelectedFrame();
        for (int layer = project.getNumLayers() - 1; layer >= 0; --layer) {
            auxiliaryBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            project.drawSheetToCanvas(auxiliaryBitmapCanvas, frame, layer, null, null);
            if (layer == project.getSelectedLayer()) {
                deTransformPath(currentEdit.getPath());
                auxiliaryBitmapCanvas.drawPath(currentEdit.getPath(), currentEdit.getPaint());
                reTransformPath(currentEdit.getPath());
            }
            displayBitmapCanvas.drawBitmap(auxiliaryBitmap, 0, 0, displayPaint);
        }
    }

    private void updateDisplayBitmap(Bitmap bitmap) {
        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        displayBitmapCanvas.drawBitmap(bitmap, 0, 0, displayPaint);
    }

//    private void updateDisplayBitmapLazy(Rect rect) {
//        displayBitmapCanvas.drawRect(rect, eraserPaint);
//
//        if (project == null) { return; }
//
//        // TODO onion-skinning
//
//        int frame = project.getSelectedFrame();
//        for (int layer = project.getNumLayers() - 1; layer >= 0; --layer) {
//            if (layer != project.getSelectedLayer()) {
//                project.drawSheetToCanvas(displayBitmapCanvas, frame, layer, rect);
//            } else {
//                auxiliaryBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                project.drawSheetToCanvas(auxiliaryBitmapCanvas, frame, layer, rect);
//                deTransformPath(currentEdit.getPath());
//                auxiliaryBitmapCanvas.drawPath(currentEdit.getPath(), currentEdit.getPaint());
//                reTransformPath(currentEdit.getPath());
//
//                displayBitmapCanvas.drawBitmap(auxiliaryBitmap, 0, 0, displayPaint);
//            }
//        }
//    }

//    private void updateDisplayBitmap() {
//        displayBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//        //onion skinning backward
//        if (isOnionSkinning) {
//            for (int i = drawSequence.getSelectedFrame() - onionSkinBackward;
//                 i < drawSequence.getSelectedFrame() && i >= 0; ++i) {
//                Bitmap bitmap = drawSequence.getFramePreviewBitmap(drawSequence.getWidth(),
//                        drawSequence.getHeight(), i);
//                displayBitmapCanvas.drawBitmap(bitmap, 0, 0, onionSkinPaint);
//            }
//        }
//
//        //current frame
//        ListIterator<Integer> i = drawSequence.getLayerOrder().listIterator(drawSequence.getLayerOrder().size());
//        while (i.hasPrevious()) {
//            int layer = i.previous();
//
//            //DRAW current edit if this is the current layer
//            if (layer != drawSequence.getLayerOrder().get(drawSequence.getSelectedLayer())) {
//                drawSequence.getSelectedFrameObject().getSheet(layer).drawSheetToCanvas(displayBitmapCanvas);
//            } else {
//                auxiliaryBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                drawSequence.getSelectedFrameObject().getSheet(layer).drawSheetToCanvas(auxiliaryBitmapCanvas);
//                deTransformPath(currentEdit.getPath());
//                auxiliaryBitmapCanvas.drawPath(currentEdit.getPath(), currentEdit.getPaint());
//                reTransformPath(currentEdit.getPath());
//
//                displayBitmapCanvas.drawBitmap(auxiliaryBitmap, 0, 0, displayPaint);
//            }
//        }
//        //onion skinning forward
//    }

    private void deTransformPath(Path path) {
        editWindowMatrix.invert(utilMatrix);
        path.transform(utilMatrix);
    }

    private void reTransformPath(Path path) {
        path.transform(editWindowMatrix);
    }

    private float lastDistance;
    private PointF oldMidpoint;
    private PointF lastEventPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        lastEventPoint.x = event.getX();
        lastEventPoint.y = event.getY();

        //Rect lazyRect = createLazyRect(lastEventPoint.x, lastEventPoint.y);

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!isPlayingAnimation) {
                    mCanvasState = CanvasState.DRAW;
                    //set up for a DRAW or pan
                    currentEdit.getPath().moveTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //do something based on state
                if (mCanvasState == CanvasState.DRAW) {
                    if (!isPlayingAnimation) {
                        //touch events come in batches, make a line to each vertex in batch
                        int historySize = event.getHistorySize();
                        for (int i = 0; i < historySize; ++i) {
                            float historicalX = event.getHistoricalX(i);
                            float historicalY = event.getHistoricalY(i);
                            currentEdit.getPath().lineTo(historicalX, historicalY);
                        }
                        currentEdit.getPath().lineTo(event.getX(), event.getY());
                        updateDisplayBitmap();
                    } else {
                        //cancel the current line being drawn
                        mCanvasState = CanvasState.NONE;
                        currentEdit.getPath().reset();
                        updateDisplayBitmap();
                    }
                } else if (mCanvasState == CanvasState.ZOOM_PAN) {
                    float currentDistance = spacing(event);
                    float dScale = currentDistance/lastDistance;
                    lastDistance = currentDistance;

                    PointF newMidpoint = new PointF();
                    setToMidpoint(newMidpoint, event);

                    float dx = (newMidpoint.x - oldMidpoint.x);
                    float dy = (newMidpoint.y - oldMidpoint.y);

                    editWindowMatrix.postTranslate(dxClamp(dx), dyClamp(dy));
                    editWindowMatrix.postScale(dScaleClamp(dScale), dScaleClamp(dScale), oldMidpoint.x, oldMidpoint.y);

                    editWindowRect.set(0, 0, imageWidth, imageHeight);
                    editWindowMatrix.mapRect(editWindowRect);

                    setToMidpoint(oldMidpoint, event);
                }
                break;
            case MotionEvent.ACTION_UP:
                CanvasState previousState = mCanvasState;
                mCanvasState = CanvasState.NONE;
                if (previousState == CanvasState.DRAW) {
                    if (!isPlayingAnimation) {
                        //drawSequence.getSelectedFrameObject().getSheet(drawSequence.getLayerOrder().get(drawSequence.getSelectedLayer())).makeEdit(currentEdit, editWindowMatrix);
                        deTransformPath(currentEdit.getPath());
                        project.makeEdit(currentEdit);
                        reTransformPath(currentEdit.getPath());
                        updateDisplayBitmap();
                    }
                    currentEdit.getPath().reset();
                    updateDisplayBitmap();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mCanvasState == CanvasState.DRAW) {
                    PathMeasure mPathMeasure = new PathMeasure(currentEdit.getPath(), false);
                    if (mPathMeasure.getLength() < MIN_EDIT_PATH_LENGTH) {
                        //cancel the current path if a second finger is placed down
                        //then switch to zoom pan state
                        currentEdit.getPath().reset();
                        mCanvasState = CanvasState.ZOOM_PAN;
                        updateDisplayBitmap();

                        lastDistance = spacing(event);
                        setToMidpoint(oldMidpoint, event);
                    }
                } else if (mCanvasState == CanvasState.NONE) {
                    //switch to zoom pan state
                    mCanvasState = CanvasState.ZOOM_PAN;
                    lastDistance = spacing(event);
                    setToMidpoint(oldMidpoint, event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2 && mCanvasState == CanvasState.ZOOM_PAN) { //so technically there is only 1 finger now
                    mCanvasState = CanvasState.NONE;
                    lastDistance = spacing(event);
                }
                break;
            default:
                return false;
        }

        invalidate(); //DRAW the view again
        return true;
    }

    private float dxClamp(float dx) {
        if (editWindowRect.left + dx > getWidth()) {
            return getWidth() - editWindowRect.left;
        }
        if (editWindowRect.right + dx < 0) {
            return 0 - editWindowRect.right;
        }
        return dx;
    }

    private float dyClamp(float dy) {
        if (editWindowRect.top + dy > getHeight()) {
            return getHeight() - editWindowRect.top;
        }
        if (editWindowRect.bottom + dy < 0) {
            return 0 - editWindowRect.bottom;
        }
        return dy;
    }

    private float dScaleClamp(float dScale) {
        if (getCurrentZoom() * dScale > mMaxZoom) {
            return mMaxZoom / getCurrentZoom();
        }
        if (getCurrentZoom() * dScale < mMinZoom) {
            return mMinZoom / getCurrentZoom();
        }
        return dScale;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private void setToMidpoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public Rect createLazyRect(float eventX, float eventY) {
        int left = Math.round(eventX - getBrushWidth());
        if (left < 0) { left = 0; }
        int right = Math.round(eventX + getBrushWidth());
        if (right > imageWidth) { right = imageWidth; }
        int top = Math.round(eventY - getBrushWidth());
        if (top < 0) { top = 0; }
        int bottom = Math.round(eventY + getBrushWidth());
        if (bottom > imageHeight) { bottom = imageHeight; }

        return new Rect(left, top, right, bottom);

        //return new Rect(0, 0, 0, 0);
    }

    public void setCurrentTool(editorTool tool) {
        currentTool = tool;
        if (tool == editorTool.brush) {
            currentEdit.setTool(editorTool.brush);
            currentEdit.setPaint(brushPaint);
        }
        if (tool == editorTool.eraser) {
            currentEdit.setTool(editorTool.eraser);
            currentEdit.setPaint(eraserPaint);
        }
    }

    public editorTool getCurrentTool() {
        return currentTool;
    }

    public void notifyFrameLayerSelection() {
        updateDisplayBitmap();
        invalidate();
    }

    public void makeUndo() {
        project.getSelectedSheetObject().undoEdit();
        updateDisplayBitmap();
        invalidate();
    }

    public void makeRedo() {
        project.getSelectedSheetObject().redoEdit();
        updateDisplayBitmap();
        invalidate();
    }

    public void makeClear() {
        updateDisplayBitmap();
        invalidate();
    }

    public void togglePreviewAnimation() {
        if (isPlayingAnimation) {
            isPlayingAnimation = false;
            playHandler.removeCallbacksAndMessages(null);
            updateDisplayBitmap();
            invalidate();
        } else {
            isPlayingAnimation = true;
            playbackFrame = project.getSelectedFrame();
            playHandler.post(new Runnable() {
                private final int period = 1000 / project.getFps();
                private long startTime;
                private int delay;

                @Override
                public void run() {
                    invalidate();
                    startTime = System.currentTimeMillis();

                    int frameID = project.framePosToID(playbackFrame);
                    if (project.hasFrameCached(frameID)) {
                        animationBitmapCanvas.drawColor(Color.WHITE);
                        for (int i = project.getNumLayers()-1; i >= 0; --i) {
                            project.drawSheetToCanvas(animationBitmapCanvas, playbackFrame, i, null, null);
                        }
                        updateDisplayBitmap(animationBitmap);
                    } else {
                        Bitmap bitmap = projectFileHandler.getBitmapForAnimation(
                                project.getProjectID(), playbackFrame);
                        updateDisplayBitmap(bitmap);
                    }

                    Log.i("animating framePos:  ", Integer.toString(playbackFrame));

                    moveNextFrame();
                    //delay should account for time to do stuff
                    delay = period - (int)(System.currentTimeMillis() - startTime);
                    playHandler.postDelayed(this, delay);
                }

                private void moveNextFrame() {
                    //wrap around to beginning to play in a loop
                    if (playbackFrame == project.getNumFrames() - 1) {
                        playbackFrame = 0;
                    } else {
                        playbackFrame += 1;
                    }
                }
            });
        }
    }

    public void toggleOnionSkin() {
        isOnionSkinning = !isOnionSkinning;
        updateDisplayBitmap();
        invalidate();
    }

    private float getCurrentZoom() {
        return editWindowRect.width() / imageWidth;
    }

    public void setBrushWidth(float width) {
        if (currentTool == editorTool.brush) {
            brushPaint.setStrokeWidth(width);
        } else if (currentTool == editorTool.eraser) {
            eraserPaint.setStrokeWidth(width);
        }
    }

    public float getBrushWidth() {
        if (currentTool == editorTool.brush) {
            return brushPaint.getStrokeWidth();
        }
        return eraserPaint.getStrokeWidth();
    }

    public void setColor(int color) {
        brushPaint.setColor(color);
    }

    public int getColor() { return brushPaint.getColor(); }

    public void setAlphaValue(int alpha) {
        if (currentTool == editorTool.brush) {
            brushPaint.setAlpha(alpha);
        } else if (currentTool == editorTool.eraser) {
            eraserPaint.setAlpha(alpha);
        }
    }

    public int getAlphaValue() {
        if (currentTool == editorTool.brush) {
            return brushPaint.getAlpha();
        }
        return eraserPaint.getAlpha();
    }

    public boolean isPlayingAnimation() {
        return isPlayingAnimation;
    }
}
