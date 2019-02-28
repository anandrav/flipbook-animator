package com.arduk.animationcreator;

import android.content.Context;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class DrawFrameCache extends LruCache<Integer,
        DrawFrameCache.DrawFrameHolder> {

    private Context context;

    private ProjectFileHandler projectFileHandler;

    private Project project;

    public class DrawFrameHolder {
        DrawFrame drawFrame;

        public DrawFrameHolder(DrawFrame drawFrame) {
            this.drawFrame = drawFrame;
        }
    }

    public DrawFrameCache(int maxSize, Project project, Context context) {
        super(maxSize);
        this.project = project;
        this.context = context;
        this.projectFileHandler = ProjectFileHandler.getInstance();
    }

    public boolean has(Integer key) {
        DrawFrameHolder holder = super.get(key);
        return holder != null;
    }

    public void put(int key, DrawFrame frame) {
        DrawFrameHolder holder = new DrawFrameHolder(frame);
        this.put(key, holder);
    }

    public DrawFrame getDrawFrame(Integer key) {
        DrawFrameHolder holder = super.get(key);
        if (holder == null) {
            holder = createFromStorage(key);
            this.put(key, holder);
        }
        return holder.drawFrame;
    }

    private DrawFrameHolder createFromStorage(Integer key) {
        int width = project.getWidth();
        int height = project.getHeight();
        DrawFrame drawFrame = new DrawFrame(context, width, height);

        for (int i = 0; i < project.getNumLayers(); ++i) {
            drawFrame.addNewSheet(context, width, height);
            DrawSheet layerSheet = drawFrame.getSheet(i);
            projectFileHandler.loadBitmapIntoDrawSheet(context, project.getProjectID(), key, i, layerSheet);
        }


        return new DrawFrameHolder(drawFrame);
    }

    @Override
    protected void entryRemoved(boolean evicted, Integer key,
                                DrawFrameHolder oldValue, DrawFrameHolder newValue) {
        //if manually removed or replaced then don't save
        if (evicted && newValue == null) {
            //save the sheets to storage
            for (int i = 0; i < project.getNumLayers(); ++i) {
                DrawSheet layerSheet = oldValue.drawFrame.getSheet(i);
                projectFileHandler.saveDrawSheetToStorage(project.getProjectID(), key, i, layerSheet);
            }
        }
    }

    //fixme reuse existing, adjacent frames
    //fixme removing frame objects removes undo history, find a way to move them instead!
    public void notifyFrameDeleted(int removedFrameID) {
        Map<Integer, DrawFrameHolder> snapshot = this.snapshot();
        Set<Integer> keySet = snapshot.keySet();
        List<Integer> keyList = new ArrayList<>(keySet);

        Collections.sort(keyList);

        ListIterator<Integer> it = keyList.listIterator();
        while (it.hasNext()) {
            // remove the frame since it is no longer part of the project
            int key = it.next();
            if (removedFrameID == key) {
                this.remove(key);
            }

            // frame objects must be assigned new keys since some of their frameIDs were altered
            if (key > removedFrameID && this.has(key)) {
                DrawFrameHolder holder = this.get(key);
                this.remove(key);
                this.put(key-1, holder);
            }
        }
    }

//    public void notifyFramesSwapped(int frame1, int frame2) {
//        DrawFrame drawFrame1 = this.get(frame1);
//        DrawFrame drawFrame2 = this.get(frame2);
//
//        if (drawFrame1 != null) {
//            this.put(frame2, drawFrame1);
//        }
//        if (drawFrame2 != null) {
//            this.put(frame1, drawFrame2);
//        }
//    }

    public void notifyLayerAdded() {
        Map<Integer, DrawFrameHolder> snapshot = this.snapshot();
        Set<Integer> keySet = snapshot.keySet();
        List<Integer> keyList = new ArrayList<>(keySet);
        Collections.sort(keyList);

        for (int i = 0; i < keyList.size(); ++i)
        {
            int key = keyList.get(i);
            //add a sheet to each frame
            DrawFrame drawFrame = this.get(key).drawFrame;
            drawFrame.addNewSheet(context, project.getWidth(), project.getHeight());
            int layerIndex = project.getNumLayers()-1;
            DrawSheet newLayer = drawFrame.getSheet(layerIndex);

            projectFileHandler.loadBitmapIntoDrawSheet(context, project.getProjectID(), key, layerIndex, newLayer);
        }
    }

    public void notifyLayerRemoved(int index) {
        Map<Integer, DrawFrameHolder> snapshot = this.snapshot();
        Set<Integer> keySet = snapshot.keySet();
        List<Integer> keyList = new ArrayList<>(keySet);
        Collections.sort(keyList);

        for (int i = 0; i < keyList.size(); ++i)
        {
            int key = keyList.get(i);
            //remove the sheet from each frame
            DrawFrame drawFrame = this.get(key).drawFrame;
            drawFrame.removeSheet(index);
        }
    }
}

