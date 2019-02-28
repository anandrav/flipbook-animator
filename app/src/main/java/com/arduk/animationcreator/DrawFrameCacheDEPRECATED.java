//
//
//package com.arduk.animationcreator;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//import android.util.LruCache;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//public class DrawFrameCacheDEPRECATED extends LruCache<Integer, DrawFrame> {
//    Context context;
//    ProjectSyncerDEPRECATED projectSyncer;
//
//    public DrawFrameCacheDEPRECATED(int maxSize, ProjectSyncerDEPRECATED projectSyncer, Context context) {
//        super(maxSize);
//        this.projectSyncer = projectSyncer;
//        this.context = context;
//    }
//
//    public boolean has(Integer key) {
//        DrawFrame drawFrame = super.get(key);
//        return drawFrame != null;
//    }
//
//    public DrawFrame getDrawFrame(Integer key) {
//        DrawFrame drawFrame = super.get(key);
//        if (drawFrame == null) {
//            drawFrame = createFrameFromStorage(key);
//            this.put(key, drawFrame);
//        }
//        return drawFrame;
//    }
//
//    private DrawFrame createFrameFromStorage(Integer key) {
//        DrawFrame drawFrame = new DrawFrame(projectSyncer.getWidth(), projectSyncer.getHeight());
//        for (int i = 0; i < projectSyncer.getNumLayers(); ++i) {
//            //add a new sheet for every layer, activate with bitmap from storage
//            drawFrame.addNewSheet(context, projectSyncer.getWidth(), projectSyncer.getHeight());
//            Bitmap bitmap = projectSyncer.loadSheetBitmap(key, i, projectSyncer.getWidth(), projectSyncer.getHeight());
//            drawFrame.getSheet(i).activate(bitmap);
//        }
//        return drawFrame;
//    }
//
//    @Override
//    protected void entryRemoved(boolean evicted, Integer key, DrawFrame oldValue, DrawFrame newValue) {
//        //if manually removed or replaced then don't save
//        if (evicted && newValue == null) {
//            //Toast.makeText(context, "saved frame to IS", Toast.LENGTH_SHORT).show();
//
//
//            /*
//            //save preview to storage
//            Bitmap bitmap = oldValue.createPreviewBitmap(projectSyncer.loadLayerOrder());
//            projectSyncer.saveDrawFramePreview(bitmap, key);
//
//            for (int i = 0; i < oldValue.getNumSheets(); ++i) {
//                //save the sheet to storage
//                Bitmap sheetBitmap = oldValue.getSheet(i).getBitmap();
//                //fixme use Async
//                projectSyncer.saveDrawSheet(sheetBitmap, key, i);
//                //deactivate the sheet to free up memory
//                oldValue.getSheet(i).deactivate();
//            } */
//
//            new AsyncSaveFrameData(oldValue, key).execute();
//        }
//    }
//
//    //FIXME saving and loading bitmaps at the same time is leading to problems :(
//    //fixme don't use this
//    private class AsyncSaveFrameData extends AsyncTask<Void, Void, Void> {
//        private DrawFrame drawFrame;
//        private int index;
//
//        public AsyncSaveFrameData(DrawFrame drawFrame, int index) {
//            super();
//            this.drawFrame = drawFrame;
//            this.index = index;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            //save preview to storage
//            Bitmap bitmap = drawFrame.createPreviewBitmap(projectSyncer.getLayerOrder());
//            projectSyncer.saveDrawFramePreview(bitmap, index);
//
//            for (int i = 0; i < drawFrame.getNumSheets(); ++i) {
//                //save the sheet to storage
//                //Bitmap sheetBitmap = drawFrame.getSheet(i).getBitmap();
//                projectSyncer.saveDrawSheet(sheetBitmap, index, i);
//                //deactivate the sheet to free up memory
//                drawFrame.getSheet(i).deactivate();
//            }
//
//            return null;
//        }
//    }
//
//    //fixme reuse existing, adjacent frames
//    //fixme removing frame objects removes undo history, find a way to move them instead!
//    public void notifyFrameRemoved(int index) {
//        Map<Integer, DrawFrame> snapshot = this.snapshot();
//        Set<Integer> keySet = snapshot.keySet();
//        List<Integer> keyList = new ArrayList<>(keySet);
//        Collections.sort(keyList);
//
//        for (int i = 0; i < keyList.size(); ++i)
//        {
//            int key = keyList.get(i);
//            //remove the frame object if it was at the index, it is no longer valid
//            if (key == index) {
//                this.remove(key);
//            }
//
//            //preserve frame objects by moving them down one spot in the cache if their position
//            //  was made invalid by the frame removal
//            //preserving DrawFrames is important to maintain undo history
//            if (key > index && this.has(key)) {
//                DrawFrame drawFrame = this.get(key);
//                this.remove(key);
//                this.put(key - 1, drawFrame);
//            }
//        }
//    }
//
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
//
//    public void notifyLayerAdded() {
//        Map<Integer, DrawFrame> snapshot = this.snapshot();
//        Set<Integer> keySet = snapshot.keySet();
//        List<Integer> keyList = new ArrayList<>(keySet);
//        Collections.sort(keyList);
//
//        for (int i = 0; i < keyList.size(); ++i)
//        {
//            int key = keyList.get(i);
//            //add a sheet to each frame
//            DrawFrame drawFrame = this.get(key);
//            drawFrame.addNewSheet(context, projectSyncer.getWidth(), projectSyncer.getHeight());
//            Bitmap bitmap = projectSyncer.loadSheetBitmap(key, projectSyncer.getNumLayers(), projectSyncer.getWidth(), projectSyncer.getHeight());
//            drawFrame.getSheet(projectSyncer.getNumLayers()).activate(bitmap);
//        }
//    }
//
//    public void notifyLayerRemoved(int index) {
//        Map<Integer, DrawFrame> snapshot = this.snapshot();
//        Set<Integer> keySet = snapshot.keySet();
//        List<Integer> keyList = new ArrayList<>(keySet);
//        Collections.sort(keyList);
//
//        for (int i = 0; i < keyList.size(); ++i)
//        {
//            int key = keyList.get(i);
//            //add a sheet to each frame
//            DrawFrame drawFrame = this.get(key);
//            drawFrame.removeSheet(index);
//        }
//    }
//}
