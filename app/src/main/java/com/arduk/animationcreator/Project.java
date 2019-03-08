package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class Project {

    private final Context context;

    private ProjectFileHandler projectFileHandler;

    private String projectID;

    private int selectedFramePos;

    private int selectedLayerPos;

    private DrawFrameCache drawFrameCache;

    private DrawFrame frameClipboard;

    // INITIALIZERS //

    // constructor
    public Project(Context context, String projectID) {
        this.context = context;
        this.projectID = projectID;
        projectFileHandler = ProjectFileHandler.getInstance();
        selectedFramePos = 0;
        selectedLayerPos = 0;

        drawFrameCache = new DrawFrameCache(7, this,
                context);
    }

    // PUBLIC METHODS //

    // select frame to be edited and center of focus
    // sheets cannot be edited unless they are selected
    // sheets are loaded into the cache based on their proximity to the selected frame
    public void selectFrame(int pos) {
        selectedFramePos = pos;
        // put it in the cache immediately
        int frameID = framePosToID(pos);
        drawFrameCache.getDrawFrame(frameID);
    }

    public void selectLayer(int pos) {
        selectedLayerPos = pos;
    }

    public void makeEdit(DrawEdit edit) {
        getSelectedSheetObject().makeEdit(edit);
    }

    public void makeUndo() {
        getSelectedSheetObject().undoEdit();
    }

    public void makeRedo() {
        getSelectedSheetObject().redoEdit();
    }

    public void insertFrame(int pos) {
        // add a new frame to the end
        projectFileHandler.addFrame(projectID);
        List<Integer> frameOrder = projectFileHandler.getFrameOrder(projectID);

        // move the new frame to the desired pos
        for (int i = frameOrder.size()-1; i >= pos+1; --i) {
            int temp = frameOrder.get(i);
            frameOrder.set(i, frameOrder.get(i-1));
            frameOrder.set(i-1, temp);
        }
        projectFileHandler.setFrameOrder(projectID, frameOrder);

        //NOTE: you don't have to notify the cache that a frame was added, it will automatically
        //      get added when the user selects it.
    }

    public void removeFrame(int pos) {
        if (getNumFrames() == 1) { return; }

        int frameID = projectFileHandler.getFrameOrder(projectID).get(pos);
        projectFileHandler.removeFrame(projectID, frameID);

        drawFrameCache.notifyFrameDeleted(frameID);
    }

    public void insertLayer(int pos) {
        // add a new layer to the end
        projectFileHandler.addLayer(projectID);
        List<Integer> layerOrder = projectFileHandler.getLayerOrder(projectID);

        // move the new layer to the desired pos
        for (int i = layerOrder.size()-1; i >= pos+1; --i) {
            int temp = layerOrder.get(i);
            layerOrder.set(i, layerOrder.get(i-1));
            layerOrder.set(i-1, temp);
        }
        projectFileHandler.setLayerOrder(projectID, layerOrder);

        drawFrameCache.notifyLayerAdded();
    }

    public void removeLayer(int pos) {
        if (getNumLayers() == 1) { return; }

        int layerID = projectFileHandler.getLayerOrder(projectID).get(pos);
        projectFileHandler.removeLayer(projectID, layerID);

        drawFrameCache.notifyLayerRemoved(layerID);
    }

    public void swapFrames(int pos1, int pos2) {
        List<Integer> frameOrder = projectFileHandler.getFrameOrder(projectID);
        int temp = frameOrder.get(pos1);
        frameOrder.set(pos1, frameOrder.get(pos2));
        frameOrder.set(pos2, temp);
        projectFileHandler.setFrameOrder(projectID, frameOrder);
    }

    public void swapLayers(int pos1, int pos2) {
        List<Integer> layerOrder = projectFileHandler.getLayerOrder(projectID);
        int temp = layerOrder.get(pos1);
        layerOrder.set(pos1, layerOrder.get(pos2));
        layerOrder.set(pos2, temp);
        projectFileHandler.setLayerOrder(projectID, layerOrder);
    }

//    public void copyFrame(int pos) {
//        // TO IMPLEMENT //
//        return;
//    }
    public void copyFrameToClipboard(int pos) {
        int frameID = framePosToID(pos);
        //copy from the cache if you can, else create from storage
        if (hasFrameCached(frameID)) {
            frameClipboard = new DrawFrame(drawFrameCache.getDrawFrame(frameID));
        } else {
            DrawFrame drawFrame = new DrawFrame(context, getWidth(), getHeight());

            for (int i = 0; i < getNumLayers(); ++i) {
                drawFrame.addNewSheet(context, getWidth(), getHeight());
                DrawSheet layerSheet = drawFrame.getSheet(i);
                projectFileHandler.loadBitmapIntoDrawSheet(context, projectID, frameID, i, layerSheet);
            }

            frameClipboard = drawFrame;
        }
    }

    public void pasteFrameFromClipboard(int pos) {
        DrawFrame newFrame = new DrawFrame(frameClipboard);
        int frameID = framePosToID(pos);
        drawFrameCache.put(frameID, newFrame);
    }

    public void mergeLayers(int pos1, int pos2) {
        // TO IMPLEMENT //
        return;
    }

    public boolean hasFrameCached(int frameID) {
        return drawFrameCache.has(frameID);
    }

    public void saveFramesInCache() {
        Map<Integer, DrawFrameCache.DrawFrameHolder> snapshot = drawFrameCache.snapshot();
        for (Map.Entry<Integer, DrawFrameCache.DrawFrameHolder> entry : snapshot.entrySet())
        {
            //save each sheet
            for (int i = 0; i < entry.getValue().drawFrame.getNumSheets(); ++i) {
                DrawSheet drawSheet = entry.getValue().drawFrame.getSheet(i);
                projectFileHandler.saveDrawSheetToStorage(projectID, entry.getKey(), i, drawSheet);
            }
        }
    }

    public void drawSheetToCanvas(Canvas canvas, int framePos, int layerPos,
                                  @Nullable Rect src, @Nullable Rect dest) {
        int frameID = projectFileHandler.getFrameOrder(projectID).get(framePos);
        int layerID = projectFileHandler.getLayerOrder(projectID).get(layerPos);
        DrawSheet drawSheet = drawFrameCache.getDrawFrame(frameID).getSheet(layerID);
        drawSheet.drawSheetToCanvas(canvas, src, dest);
    }

    public void drawSheetToCanvas(Canvas canvas, int framePos, int layerPos,
                                  @Nullable Matrix matrix) {
        int frameID = projectFileHandler.getFrameOrder(projectID).get(framePos);
        int layerID = projectFileHandler.getLayerOrder(projectID).get(layerPos);
        DrawSheet drawSheet = drawFrameCache.getDrawFrame(frameID).getSheet(layerID);
        drawSheet.drawSheetToCanvas(canvas, matrix);
    }

    public void drawSheetToCanvas(Canvas canvas, int framePos, int layerPos,
                                  float scale) {
        int frameID = projectFileHandler.getFrameOrder(projectID).get(framePos);
        int layerID = projectFileHandler.getLayerOrder(projectID).get(layerPos);
        DrawSheet drawSheet = drawFrameCache.getDrawFrame(frameID).getSheet(layerID);
        drawSheet.drawSheetToCanvas(canvas, scale);
    }

    public DrawSheet getDrawSheetObject(int framePos, int layerPos) {
        int frameID = projectFileHandler.getFrameOrder(projectID).get(framePos);
        int layerID = projectFileHandler.getLayerOrder(projectID).get(layerPos);
        return drawFrameCache.getDrawFrame(frameID).getSheet(layerID);
    }

    public DrawSheet getSelectedSheetObject() {
        int frameID = projectFileHandler.getFrameOrder(projectID).get(selectedFramePos);
        int layerID = projectFileHandler.getLayerOrder(projectID).get(selectedLayerPos);
        return drawFrameCache.getDrawFrame(frameID).getSheet(layerID);
    }


    // return position of selected frame
    public int getSelectedFrame() {
        return selectedFramePos;
    }

    public int getSelectedLayer() {
        return selectedLayerPos;
    }

    public String getProjectID() { return projectID; }

    public String getTitle() {
        return projectFileHandler.getTitle(projectID);
    }

    public int getWidth() {
        return projectFileHandler.getWidth(projectID);
    }

    public int getHeight() {
        return projectFileHandler.getHeight(projectID);
    }

    public int getNumFrames() {
        return projectFileHandler.getNumFrames(projectID);
    }

    public int getNumLayers() {
        return projectFileHandler.getNumLayers(projectID);
    }

    public int getFps() {
        return projectFileHandler.getFps(projectID);
    }

    public void setTitle(String title) {
        projectFileHandler.setTitle(projectID, title);
    }

    public void setFps(int fps) {
        projectFileHandler.setFps(projectID, fps);
    }

    public int framePosToID(int pos) {
        return projectFileHandler.getFrameOrder(projectID).get(pos);
    }

    public int frameIdToPos(int ID) {
        return projectFileHandler.frameIdToPos(projectID, ID);
    }

    public int layerPosToID(int pos) {
        return projectFileHandler.getLayerOrder(projectID).get(pos);
    }

    public int layerIdToPos(int ID) {
        return projectFileHandler.layerIdToPos(projectID, ID);
    }
}
