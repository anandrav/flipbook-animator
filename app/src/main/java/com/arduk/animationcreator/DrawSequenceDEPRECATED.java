
//
//package com.arduk.animationcreator;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.util.Log;
//
//import java.util.List;
//import java.util.ListIterator;
//import java.util.Map;
//
///* Container for a sequence of DRAW frames, responsible for managing memory and
//   loading from/writing to Internal Storage only when necessary
//*/
//
///* projects are stored in a specific format.
//   Within the project directory there is a config file called config.
//   In addition, there is a directory for each frame, named after its number.
//   Within each of these frame directories are bitmaps named after the number of their layer
// */
//
//public class DrawSequenceDEPRECATED {
//    Context context;
//
//    private int selectedFrame;
//    private int selectedLayer;
//
//    private ProjectSyncerDEPRECATED projectSyncer;
//    private DrawFrameCacheDEPRECATED drawFrameCache;
//    private Bitmap reusableBitmap; // use for frames in storage
//    private Canvas reusableBitmapCanvas;
//    private Bitmap secondaryReusableBitmap; // use for cached frames
//    private Canvas secondaryReusableBitmapCanvas;
//    private Paint displayPaint;
//
//    private final int MAX_NUM_CACHED_FRAMES = 3; //FIXME change this back to 3 immediately!
//
//    public DrawSequenceDEPRECATED(Context context) {
//        this.context = context;
//        projectSyncer = new ProjectSyncerDEPRECATED(context);
//    }
//
//    public void attachToProject(String projectID) {
//        //access the project data through ProjectSyncer
//        projectSyncer.load(projectID);
//        drawFrameCache = new DrawFrameCacheDEPRECATED(MAX_NUM_CACHED_FRAMES, projectSyncer, context);
//        reusableBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
//        reusableBitmapCanvas = new Canvas(reusableBitmap);
//        secondaryReusableBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
//        secondaryReusableBitmapCanvas = new Canvas(secondaryReusableBitmap);
//        displayPaint = new Paint();
//        displayPaint.setAntiAlias(true);
//    }
//
//    public void manualSave() {
//        //save the contents of the frames in the cache before exiting ProjectEditorActivity
//        //everything else is autosaved by ProjectSyncer
//
//        Map<Integer, DrawFrameDEPRECATED> snapshot = drawFrameCache.snapshot();
//        for (Map.Entry<Integer, DrawFrameDEPRECATED> entry : snapshot.entrySet())
//        {
//            //save each sheet
//            for (int i = 0; i < entry.getValue().getNumSheets(); ++i) {
//                //fixme use Async
//                Bitmap bitmap = entry.getValue().getSheet(i).getBitmap();
//                projectSyncer.saveDrawSheet(bitmap, entry.getKey(), i);
//            }
//            //save the preview
//            //fixme use Async
//            Bitmap bitmap = entry.getValue().createPreviewBitmap(getLayerOrder());
//            projectSyncer.saveDrawFramePreview(bitmap, entry.getKey());
//        }
//    }
//
//    public DrawFrameDEPRECATED getSelectedFrameObject() {
//        return drawFrameCache.getDrawFrame(selectedFrame);
//    }
//
//    public Bitmap getFramePreviewBitmapForAnimation(int width, int height, int frame) {
//        //check the cache
//        if (drawFrameCache.has(frame)) {
//            drawFrameCache.getDrawFrame(frame).drawPreviewToCanvas(secondaryReusableBitmapCanvas, getLayerOrder());
//            return secondaryReusableBitmap;
//        }
//
//        //get from storage if not in the cache
//        Bitmap bitmap = projectSyncer.loadPreviewBitmapForAnimation(reusableBitmap, reusableBitmapCanvas, frame, width, height);
//        return bitmap;
//    }
//
//    //fixme too slow :(
//    public Bitmap getFramePreviewBitmap(int width, int height, int frame) {
//        //check the cache
//        if (drawFrameCache.has(frame)) {
////            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
////            Canvas canvas = new Canvas(bitmap);
////            drawFrameCache.getDrawFrame(frame).drawPreviewToCanvas(canvas, loadLayerOrder());
//            Bitmap bitmap = Bitmap.createScaledBitmap(drawFrameCache.getDrawFrame(frame).createPreviewBitmap(getLayerOrder()), width, height, true);
//            return bitmap;
//        }
//
//        //get from storage if not in the cache
//        //Bitmap bitmap = projectSyncer.loadPreviewBitmap(frame, width, height);
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        Canvas bitmapCanvas = new Canvas(bitmap);
//
//        bitmapCanvas.drawColor(Color.WHITE);
//
//        ListIterator<Integer> i = getLayerOrder().listIterator(getLayerOrder().size());
//        while (i.hasPrevious()) {
//            int layer = i.previous();
//            Bitmap sheetBitmap = projectSyncer.loadSheetBitmapForAnimation(frame, layer, width, height, reusableBitmap, reusableBitmapCanvas);
//            bitmapCanvas.drawBitmap(sheetBitmap, 0, 0, displayPaint);
//        }
//
//        return bitmap;
//    }
//
//    public Bitmap getLayerPreviewBitmap(int width, int height, int layerPos) {
//        DrawFrameDEPRECATED currentFrame = drawFrameCache.getDrawFrame(selectedFrame);
//        return currentFrame.getSheet(layerPosToId(layerPos)).getBitmap();
//    }
//
//    public Bitmap getLayerPreviewBitmapForAnimation(int width, int height, int frame, int layerPos) {
//        if (drawFrameCache.has(frame)) {
//            DrawFrameDEPRECATED currentFrame = drawFrameCache.getDrawFrame(frame);
//            return currentFrame.getSheet(layerPosToId(layerPos)).getBitmap();
//        }
//
//        int layerID = layerPosToId(layerPos);
//        reusableBitmap = projectSyncer.loadSheetBitmapForAnimation(frame, layerID, width, height, reusableBitmap, reusableBitmapCanvas);
//        return reusableBitmap;
//    }
//
//    public void selectFrame(int frame) {
//        selectedFrame = frame;
//    }
//
//    public void selectLayer(int layerPos) {
//        selectedLayer = layerPos;
//    }
//
//    public void insertFrame() {
//        projectSyncer.addFrame();
//
//        //fixme need to add support for index-based insertion
//    }
//
//    public void removeFrame(int index) {
//        //if removing the last frame, first switch to second last frame
//        if (index == getNumFrames() - 1) {
//            selectFrame(getNumFrames() - 2);
//        }
//
//        projectSyncer.removeFrame(index);
//        drawFrameCache.notifyFrameRemoved(index);
//    }
//
//    public void swapFrames(int frame1, int frame2) {
//        //ProjectSyncer.swapFrames(frame1, frame2);
//        //drawFrameCache.notifyFramesSwapped(frame1, frame2);
//    }
//
//    public void addLayer() {
//        //Toast.makeText(context, "added layer", Toast.LENGTH_SHORT).show();
//        // add layer to each frame in cache
//        drawFrameCache.notifyLayerAdded();
//        // add layer to projectSyncer
//        projectSyncer.addLayer();
//   }
//
//    public void removeSelectedLayer() {
//        //Toast.makeText(context, "removed a layer", Toast.LENGTH_SHORT).show();
//        int layerPos = getSelectedLayer();
//        int layerId = layerPosToId(layerPos);
//
//        Log.i("selected layer", Integer.toString(layerPos));
//        Log.i("layerID", Integer.toString(layerId));
//
//        // remove layer from each frame in cache at index
//        drawFrameCache.notifyLayerRemoved(layerId);
//        // remove layer from projectSyncer
//        projectSyncer.removeLayer(layerId);
//
//        //if removing the last layer, first switch to second last layer
//        if (selectedLayer == getNumLayers()) {
//            selectLayer(getNumLayers() - 1);
//        }
//    }
//
//    public void swapLayers(int layerPos1, int layerPos2) {
//        //Toast.makeText(context, "swapped layers", Toast.LENGTH_SHORT).show();
//
//        projectSyncer.swapLayers(layerPos1, layerPos2);
//    }
//
//    public int layerPosToId(int pos) {
//        return getLayerOrder().get(pos);
//    }
//
//    public int layerIdToPos(int Id) {
//        for (int i = 0; i < getLayerOrder().size(); ++i) {
//            if (getLayerOrder().get(i) == Id) {
//                return i;
//            }
//        }
//
//        return 0; // prevent compiler error
//    }
//
//    public ProjectSyncerDEPRECATED getProjectSyncer() {
//        return projectSyncer;
//    }
//
//    public int getSelectedFrame() {
//        return selectedFrame;
//    }
//
//    public int getSelectedLayer() {
//        return selectedLayer;
//    }
//
//    public int getNumFrames() {
//        return projectSyncer.getNumFrames();
//    }
//
//    public int getNumLayers() {
//        return projectSyncer.getNumLayers();
//    }
//
//    public List<Integer> getLayerOrder() { return projectSyncer.getLayerOrder(); }
//
//    public int getWidth() {
//        return projectSyncer.getWidth();
//    }
//
//    public int getHeight() {
//        return projectSyncer.getHeight();
//    }
//
//    public int getFps() {
//        return projectSyncer.getFps();
//    }
//}
