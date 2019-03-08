package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.security.auth.callback.Callback;

public class ProjectFileHandler {

    private String projectID;

    private ProjectReaderWriter projectReaderWriter;

    private HandlerThread handlerThread;

    private Handler htHandler;

    private Handler uiHandler;

    private Bitmap reusableAnimationBitmap1;

    private Canvas reusableCanvas1;

    private Bitmap reusableAnimationBitmap2;

    private Canvas reusableCanvas2;

    private int activityCount;

    // singleton pattern
    private static ProjectFileHandler instance;

    public static ProjectFileHandler getInstance() {
        if (instance == null) {
            instance = new ProjectFileHandler();
        }
        return instance;
    }

    public void attachToProject(Context context, String projectID) {
        this.projectID = projectID;
        this.projectReaderWriter = new ProjectReaderWriter(context, projectID);
    }

    // private constructor
    private ProjectFileHandler() {
        handlerThread = new HandlerThread("Image Transfer", HandlerThread.NORM_PRIORITY);
        handlerThread.start();
        htHandler = new Handler(handlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());

        activityCount = 0;
    }

    public boolean hasActiveRunnable() {
        return activityCount > 0;
    }

    // Secondary Thread with Callback Methods, take much time //

    public void loadBitmapIntoDrawSheet(final Context context, final String projectID, final int frameID,
                                        final int layerID, final DrawSheet drawSheet) {
        activityCount += 1;

        final ProjectReaderWriter fProjectReaderWriter = projectReaderWriter;
        htHandler.post(new Runnable() {
            @Override
            public void run() {
                int width = fProjectReaderWriter.loadWidth();
                int height = fProjectReaderWriter.loadHeight();
                final Bitmap bitmap = fProjectReaderWriter.loadSheetBitmap(frameID, layerID,
                        width, height, null);

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        drawSheet.activate(bitmap);
                        ((ProjectEditorActivity)context).onDrawSheetActivated(frameID, layerID);
                        activityCount -= 1;
                    }
                });
            }
        });
    }

    public void loadSampledFrameIntoImageView(final String projectID, final int frameID,
                                              final int inSample, final ImageView imageView) {
        activityCount += 1;

        final ProjectReaderWriter fProjectReaderWriter = projectReaderWriter;
        final int width = getWidth(projectID)/inSample;
        final int height = getHeight(projectID)/inSample;
        final Bitmap viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        htHandler.post(new Runnable() {
            @Override
            public void run() {
                Canvas viewBitmapCanvas = new Canvas(viewBitmap);
                viewBitmapCanvas.drawColor(Color.WHITE);

                for (int layer = 0; layer < fProjectReaderWriter.loadNumLayers(); ++layer) {
                    Bitmap layerBitmap = fProjectReaderWriter.loadSheetBitmap(frameID, layer,
                            width, height, null);
                    viewBitmapCanvas.drawBitmap(layerBitmap, 0, 0, null);
                }

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(viewBitmap);
                        activityCount -= 1;
                    }
                });
            }
        });
    }

//    public void loadSampledFrameIntoImageViewAndDrawView(final String projectID, final int frameID,
//                                              final int inSample, final ImageView imageView,
//                                                         final DrawView drawView) {
//        activityCount += 1;
//
//        final ProjectReaderWriter fProjectReaderWriter = projectReaderWriter;
//        final int width = getWidth(projectID)/inSample;
//        final int height = getHeight(projectID)/inSample;
//        final Bitmap viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        htHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Canvas viewBitmapCanvas = new Canvas(viewBitmap);
//                viewBitmapCanvas.drawColor(Color.WHITE);
//
//                for (int layer = fProjectReaderWriter.loadNumLayers() - 1; layer >= 0; --layer) {
//                    Bitmap layerBitmap = fProjectReaderWriter.loadSheetBitmap(frameID, layer,
//                            width, height, null);
//                    viewBitmapCanvas.drawBitmap(layerBitmap, 0, 0, null);
//                }
//
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        imageView.setImageBitmap(viewBitmap);
//                        drawView.invalidateWithBitmap(viewBitmap);
//                    }
//                });
//            }
//        });
//    }

    public void saveDrawSheetToStorage(final String projectID, final int frameID, final int layerID,
                                       final DrawSheet drawSheet) {
        activityCount += 1;

        final ProjectReaderWriter fProjectReaderWriter = projectReaderWriter;
        htHandler.post(new Runnable() {
            @Override
            public void run() {
                fProjectReaderWriter.saveSheetBitmap(drawSheet.getBitmap(), frameID, layerID);

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        activityCount -= 1;
                    }
                });
            }
        });
    }

//    public void loadBitmapIntoDrawSheet(final Context context, final String projectID, final int frameID,
//                                        final int layerID, final DrawSheet drawSheet) {
//        htHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                int width = projectReaderWriter.loadWidth();
//                int height = projectReaderWriter.loadHeight();
//                final Bitmap bitmap = projectReaderWriter.loadSheetBitmap(frameID, layerID,
//                        width, height, null);
//
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        drawSheet.activate(bitmap);
//                        ((ProjectEditorActivity)context).onDrawSheetActivated(frameID, layerID);
//                    }
//                });
//            }
//        });
//    }
//
//    public void loadSampledFrameIntoImageView(final String projectID, final int frameID,
//                                              final int inSample, final ImageView imageView) {
//        final int width = getWidth(projectID);
//        final int height = getHeight(projectID);
//        final Bitmap viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        htHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Canvas viewBitmapCanvas = new Canvas(viewBitmap);
//                viewBitmapCanvas.drawColor(Color.WHITE);
//
//                for (int layer = projectReaderWriter.loadNumLayers() - 1; layer >= 0; --layer) {
//                    Bitmap layerBitmap = projectReaderWriter.loadSheetBitmap(frameID, layer,
//                            width/inSample, height/inSample, null);
//                    viewBitmapCanvas.drawBitmap(layerBitmap, 0, 0, null);
//                }
//
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        imageView.setImageBitmap(viewBitmap);
//                    }
//                });
//            }
//        });
//    }
//
//    public void saveDrawSheetToStorage(final String projectID, final int frameID, final int layerID,
//                                       final DrawSheet drawSheet) {
//        htHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                projectReaderWriter.saveSheetBitmap(drawSheet.getBitmap(), frameID, layerID);
//            }
//        });
//    }

    // UI Thread methods, take little time //

    public Bitmap getBitmapForAnimation(String projectID, int framePos) {
        int width = getWidth(projectID);
        int height = getHeight(projectID);
        if (reusableAnimationBitmap1 == null || reusableCanvas1 == null) {
            reusableAnimationBitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            reusableCanvas1 = new Canvas(reusableAnimationBitmap1);
        }
        if (reusableAnimationBitmap2 == null || reusableCanvas2 == null) {
            reusableAnimationBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            reusableCanvas2 = new Canvas(reusableAnimationBitmap2);
        }

        reusableCanvas2.drawColor(Color.WHITE);

        int frameID = getFrameOrder(projectID).get(framePos);

        List<Integer> layerOrder = getLayerOrder(projectID);
        ListIterator<Integer> it = layerOrder.listIterator();
        while (it.hasNext()) {
            reusableAnimationBitmap1 = projectReaderWriter
                    .loadSheetBitmap(frameID, it.next(), width, height, reusableAnimationBitmap1);
            reusableCanvas2.drawBitmap(reusableAnimationBitmap1, 0, 0, null);
        }

        return reusableAnimationBitmap2;
    }

    public String createProject(Context context, int width, int height) {
//        sleepHandlerThread();
        String projectID = ProjectReaderWriter.createProject(context, width, height);
//        wakeHandlerThread();
        return projectID;
    }

    public void removeProject(Context context, String projectID) {
//        sleepHandlerThread();
        ProjectReaderWriter.removeProject(context, projectID);
//        wakeHandlerThread();
    }

    // get list of projectIDs in storage
    public static List<String> getProjectIdList(Context context) {
//        sleepHandlerThread();
        List<String> list = ProjectReaderWriter.getProjectIdList(context);
//        wakeHandlerThread();
        return list;
    }

    public String getTitle(String projectID) {
//        sleepHandlerThread();
        String title = projectReaderWriter.loadTitle();
//        wakeHandlerThread();
        return title;
    }

    public int getWidth(String projectID) {
//        sleepHandlerThread();
        int width = projectReaderWriter.loadWidth();
//        wakeHandlerThread();
        return width;
    }

    public int getHeight(String projectID) {
//        sleepHandlerThread();
        int height = projectReaderWriter.loadHeight();
//        wakeHandlerThread();
        return height;
    }

    public int getFps(String projectID) {
//        sleepHandlerThread();
        int fps = projectReaderWriter.loadFps();
//        wakeHandlerThread();
        return fps;
    }

    public int getBrushColor() {
        return projectReaderWriter.loadBrushColor();
    }

    public int getBrushRadius() {
        return projectReaderWriter.loadBrushRadius();
    }

    public int getBrushAlpha() {
        return projectReaderWriter.loadBrushAlpha();
    }

    public int getEraserRadius() {
        return projectReaderWriter.loadEraserRadius();
    }

    public int getEraserAlpha() {
        return projectReaderWriter.loadEraserAlpha();
    }

    public int getNumFrames(String projectID) {
//        sleepHandlerThread();
        int numFrames = projectReaderWriter.loadNumFrames();
//        wakeHandlerThread();
        return numFrames;
    }

    public List<Integer> getFrameOrder(String projectID) {
//        sleepHandlerThread();
        List<Integer> list = projectReaderWriter.loadFrameOrder();
//        wakeHandlerThread();
        return list;
    }

    public int getNumLayers(String projectID) {
//        sleepHandlerThread();
        int numLayers = projectReaderWriter.loadNumLayers();
//        wakeHandlerThread();
        return numLayers;
    }

    public List<Integer> getLayerOrder(String projectID) {
//        sleepHandlerThread();
        List<Integer> list = projectReaderWriter.loadLayerOrder();
//        wakeHandlerThread();
        return list;
    }

    public int frameIdToPos(String projectID, int ID) {
//        sleepHandlerThread();
        int pos = projectReaderWriter.frameIdToPos(ID);
//        wakeHandlerThread();
        return pos;
    }

    public int layerIdToPos(String projectID, int ID) {
//        sleepHandlerThread();
        int pos = projectReaderWriter.layerIdToPos(ID);
//        wakeHandlerThread();
        return pos;
    }

    public void setTitle(String projectID, String title) {
//        sleepHandlerThread();
        projectReaderWriter.setTitle(title);
//        wakeHandlerThread();
    }

    public void setFps(String projectID, int fps) {
//        sleepHandlerThread();
        projectReaderWriter.setFps(fps);
//        wakeHandlerThread();
    }

    public void setBrushColor(int color) {
        projectReaderWriter.setBrushColor(color);
    }

    public void setBrushRadius(int radius) {
        projectReaderWriter.setBrushRadius(radius);
    }

    public void setBrushAlpha(int alpha) {
        projectReaderWriter.setBrushAlpha(alpha);
    }

    public void setEraserRadius(int radius) {
        projectReaderWriter.setEraserRadius(radius);
    }

    public void setEraserAlpha(int alpha) {
        projectReaderWriter.setEraserAlpha(alpha);
    }

    public void setFrameOrder(String projectID, List<Integer> frameOrder) {
//        sleepHandlerThread();
        projectReaderWriter.setFrameOrder(frameOrder);
//        wakeHandlerThread();
    }

    public void setLayerOrder(String projectID, List<Integer> layerOrder) {
//        sleepHandlerThread();
        projectReaderWriter.setLayerOrder(layerOrder);
//        wakeHandlerThread();
    }

    public void addFrame(String projectID) {
//        sleepHandlerThread();
        projectReaderWriter.addFrame();
//        wakeHandlerThread();
    }

    public void removeFrame(String projectID, int frameID) {
//        sleepHandlerThread();
        projectReaderWriter.removeFrame(frameID);
//        wakeHandlerThread();
    }

    public void addLayer(String projectID) {
//        sleepHandlerThread();
        projectReaderWriter.addLayer();
//        wakeHandlerThread();
    }

    public void removeLayer(String projectID, int layerID) {
//        sleepHandlerThread();
        projectReaderWriter.removeLayer(layerID);
//        wakeHandlerThread();
    }

    /**
     * Read and write project data to file system in internal storage
     * Internal storage has the following format:
     *      projects
     *          <projectUUID>
     *              config.JSON
     *              <frameID> <-- for every frame
     *                  layerID.PNG <-- for every layer
     *
     * The config file contains the following elements:
     *      String title
     *      int width
     *      int height
     *      int numFrames
     *      int numLayers
     *      int fps
     *      arr<int> frameOrder
     *      arr<int> layerOrder
     *
     * frameOrder and layerOrder are arrays that serve as a mapping from position to ID in the project,
     *      an index based method that simplifies swapping and repositioning of frames/layers
     *
     */
    private static class ProjectReaderWriter {

        private final Context context;

        private final String projectID;

        private static final String DEFAULT_TITLE = "title of project";
        private static final int DEFAULT_WIDTH = 1920;
        private static final int DEFAULT_HEIGHT = 1080;
        private static final int DEFAULT_NUM_FRAMES = 1;
        private static final int DEFAULT_NUM_LAYERS = 1;
        private static final int DEFAULT_FPS = 24;
        private static final int DEFAULT_BRUSH_COLOR = Color.BLACK;
        private static final int DEFAULT_BRUSH_RADIUS = 8;
        private static final int DEFAULT_BRUSH_ALPHA = 255;
        private static final int DEFAULT_ERASER_RADIUS = 120;
        private static final int DEFAULT_ERASER_ALPHA = 255;

        private static final int COMPRESS_QUALITY = 100;

        // constructor
        public ProjectReaderWriter(Context context, String projectID) {
            this.context = context;
            this.projectID = projectID;
        }

        // static method that creates project with resolution and returns its projectID
        public static String createProject(Context context, int width, int height) {
            String projectID = UUID.randomUUID().toString();

            String projectDirName = "projects/" + projectID;
            File projectDir = new File(context.getFilesDir(), projectDirName);
            if (!projectDir.mkdir()) {
                Log.e("project", "could not create project directory");
            }

            File configFile = new File(projectDir, "config.JSON");
            try {
                if (!configFile.createNewFile()) {
                    Log.e("project", "could not create project config");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // initialize with 1 frame, 1 layer
            File frameDir = new File(context.getFilesDir(), "projects/" + projectID + "/" +
                    Integer.toString(0));
            if (!frameDir.mkdir()) {
                Log.e("project", "could not create first frame of new project");
            }
            File sheetFile = new File(context.getFilesDir(), "projects/" + projectID + "/" +
                    Integer.toString(0) + "/" + Integer.toString(0) + ".PNG");
            try {
                if (!sheetFile.createNewFile()) {
                    Log.e("project", "could not create first sheet file");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            initializeConfig(configFile, width, height);

            return projectID;
        }

        public static void removeProject(Context context, String projectID) {
            File projectDir = new File(context.getFilesDir(), "projects/" + projectID);
            deleteRecursive(projectDir);
        }

        // get list of projectIDs in storage
        public static List<String> getProjectIdList(Context context) {
            File storage = new File(context.getFilesDir(), "projects");
            if (storage.mkdir()) {
                Log.v("project", "created /projects/ directory");
            }
            File[] files = storage.listFiles();
            for (int i = 0; i < files.length; ++i) {
                Log.v("files", files[i].getName());
            }
            List<String> result = new ArrayList<>();
            for (int i = 0; i < files.length; ++i) {
                String projectID = files[i].getName();
                result.add(projectID);
                Log.v("projectID", projectID);
            }
            return result;
        }

        // PUBLIC METHODS //

        public Bitmap loadSheetBitmap(int frameID, int layerID, int reqWidth, int reqHeight,
                                      @Nullable Bitmap reusableBitmap) {
            File bitmapFile = getSheetFile(frameID, layerID);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = calculateInSampleSize(loadWidth(),
                    loadHeight(), reqWidth, reqHeight);
            if (reusableBitmap != null) { options.inBitmap = reusableBitmap; }

            Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
            if (bitmap == null) {
                //could not load from file, create a blank one
                bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.ARGB_8888);
                Log.v("ProjectReaderWriter", "bitmap failed to be decoded, creating new one");
            } else {
                Log.v("ProjectReaderWriter", "bitmap decoded at frame id: " +
                        Integer.toString(frameID));
            }
            return bitmap;
        }

        public void saveSheetBitmap(Bitmap bitmap, int frameID, int layerID) {
            File bitmapFile = getSheetFile(frameID, layerID);

            try {
                FileOutputStream out = new FileOutputStream(bitmapFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
                Log.v("ProjectReaderWriter", "bitmap compressed at " + bitmapFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized String loadTitle() {
            String title = DEFAULT_TITLE;
            try {
                title = getConfig().getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return title;
        }

        public int loadWidth() {
            int width = DEFAULT_WIDTH;
            try {
                width = getConfig().getInt("width");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return width;
        }

        public int loadHeight() {
            int height = DEFAULT_HEIGHT;
            try {
                height = getConfig().getInt("height");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return height;
        }

        public int loadFps() {
            int fps = 0;
            try {
                fps = getConfig().getInt("fps");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return fps;
        }

        public int loadBrushColor() {
            int brushColor = DEFAULT_BRUSH_COLOR;
            try {
                brushColor = getConfig().getInt("brushColor");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return brushColor;
        }

        public int loadBrushRadius() {
            int brushRadius = DEFAULT_BRUSH_RADIUS;
            try {
                brushRadius = getConfig().getInt("brushRadius");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return brushRadius;
        }

        public int loadBrushAlpha() {
            int brushAlpha = DEFAULT_BRUSH_ALPHA;
            try {
                brushAlpha = getConfig().getInt("brushAlpha");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return brushAlpha;
        }

        public int loadEraserRadius() {
            int eraserRadius = DEFAULT_ERASER_RADIUS;
            try {
                eraserRadius = getConfig().getInt("eraserRadius");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return eraserRadius;
        }

        public int loadEraserAlpha() {
            int eraserAlpha = DEFAULT_ERASER_ALPHA;
            try {
                eraserAlpha = getConfig().getInt("eraserAlpha");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return eraserAlpha;
        }

        public int loadNumFrames() {
            int numFrames = 0;
            try {
                numFrames = getConfig().getInt("numFrames");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return numFrames;
        }

        public List<Integer> loadFrameOrder() {
            List<Integer> result = new ArrayList<>();
            JSONArray jArray;
            try {
                jArray = getConfig().getJSONArray("frameOrder");
                for (int i = 0; i < jArray.length(); i++) {
                    result.add(Integer.parseInt(jArray.getString(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                for (int i = 0; i < loadNumFrames(); ++i) {
                    result.add(i);
                }
            }

            return result;
        }

        public int loadNumLayers() {
            int numLayers = 0;
            try {
                numLayers = getConfig().getInt("numLayers");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return numLayers;
        }

        public List<Integer> loadLayerOrder() {
            List<Integer> result = new ArrayList<>();
            JSONArray jArray;
            try {
                jArray = getConfig().getJSONArray("layerOrder");
                for (int i = 0; i < jArray.length(); i++) {
                    result.add(Integer.parseInt(jArray.getString(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                for (int i = 0; i < loadNumLayers(); ++i) {
                    result.add(i);
                }
            }

            return result;
        }

        public void setTitle(String title) {
            putToConfig("title", title);
        }

        public void setFps(int fps) {
            putToConfig("fps", fps);
        }

        public void setBrushColor(int color) { putToConfig("brushColor", color); }

        public void setBrushRadius(int radius) { putToConfig("brushRadius", radius); }

        public void setBrushAlpha(int alpha) { putToConfig("brushAlpha", alpha); }

        public void setEraserRadius(int radius) { putToConfig("eraserRadius", radius); }

        public void setEraserAlpha(int alpha) { putToConfig("eraserAlpha", alpha); }

        public void setFrameOrder(List<Integer> frameOrder) {
            JSONArray jArray = new JSONArray();
            for (int i = 0; i < frameOrder.size(); ++i) {
                jArray.put(frameOrder.get(i));
            }
            putToConfig("frameOrder", jArray);
        }

        public void setLayerOrder(List<Integer> layerOrder) {
            JSONArray jArray = new JSONArray();
            for (int i = 0; i < layerOrder.size(); ++i) {
                jArray.put(layerOrder.get(i));
            }
            putToConfig("layerOrder", jArray);
        }

        public void addFrame() {
            // create new directory for frame with empty .PNG files for each layer
            int numFrames = loadNumFrames();
            int numLayers = loadNumLayers();
            File frameDir = getFrameDir(numFrames);
            if (!frameDir.mkdir()) {
                Log.e("project", "could not create dir for new frame");
            }
            for (int i = 0; i < numLayers; ++i) {
                File sheetFile = getSheetFile(numFrames, i);
                try {
                    if (!sheetFile.createNewFile()) {
                        Log.e("project", "could not create a file for new frame");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // update the config
            putToConfig("numFrames", numFrames+1);
            List<Integer> newFrameOrder = loadFrameOrder();
            newFrameOrder.add(newFrameOrder.size());
            setFrameOrder(newFrameOrder);
        }

        public void removeFrame(int frameID) {
            int numFrames = loadNumFrames();

            // always have at least 1 frame
            if (numFrames == 1) { return; }

            // remove the directory associated with frameID
            deleteRecursive(getFrameDir(frameID));

            // decrement the names of frame directories that occur after
            for (int f = 0; f < numFrames; ++f) {
                if (f > frameID) {
                    File before = getFrameDir(f);
                    File after = getFrameDir(f-1);
                    if (!before.renameTo(after)) {
                        Log.e("project", "could not rename a file during frame removal");
                    }
                }
            }

            // update the config
            List<Integer> newFrameOrder = loadFrameOrder();
            int oldFramePos = frameIdToPos(frameID);
            newFrameOrder.remove(oldFramePos);

            // decrement all values of frameOrder which are larger than frameID
            for (int i = 0; i < newFrameOrder.size(); ++i) {
                if (newFrameOrder.get(i) > frameID) {
                    int temp = newFrameOrder.get(i) - 1;
                    newFrameOrder.set(i, temp);
                }
            }

            setFrameOrder(newFrameOrder);
            putToConfig("numFrames", numFrames-1);
        }

        public void addLayer() {
            // create new directory for frame with empty .PNG files for each layer
            int numFrames = loadNumFrames();
            int numLayers = loadNumLayers();
            for (int i = 0; i < numFrames; ++i) {
                File sheetFile = getSheetFile(i, numLayers);
                try {
                    if (!sheetFile.createNewFile()) {
                        Log.e("project", "could not create a file for new layer");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // update the config
            putToConfig("numLayers", numLayers+1);
            List<Integer> newLayerOrder = loadLayerOrder();
            newLayerOrder.add(newLayerOrder.size());
            setLayerOrder(newLayerOrder);
        }

        public void removeLayer(int layerID) {
            int numFrames = loadNumFrames();
            int numLayers = loadNumLayers();

            // always have at least 1 layer
            if (numLayers == 1) { return; }

            // for every frame directory, delete the <layerID>.PNG and decrement the names of the
            //      other images whose names are larger than layerID
            for (int f = 0; f < numFrames; ++f) {
                File layer = getSheetFile(f, layerID);
                if (!layer.delete()) {
                    Log.e("project", "could not delete a file when removing layer");
                }

                for (int l = 0; l < numLayers; ++l) {
                    if (l > layerID) {
                        File before = getSheetFile(f, l);
                        File after = getSheetFile(f, l-1);
                        if (!before.renameTo(after)) {
                            Log.e("project", "could not rename a file during layer removal");
                        }
                    }
                }
            }


            // update the config
            List<Integer> newLayerOrder = loadLayerOrder();
            int oldLayerPos = layerIdToPos(layerID);
            newLayerOrder.remove(oldLayerPos);

            // decrement all values of layerOrder which are larger than layerId
            for (int i = 0; i < newLayerOrder.size(); ++i) {
                if (newLayerOrder.get(i) > layerID) {
                    int temp = newLayerOrder.get(i) - 1;
                    newLayerOrder.set(i, temp);
                }
            }

            setLayerOrder(newLayerOrder);
            putToConfig("numLayers", numLayers-1);
        }

        public int frameIdToPos(int ID) {
            List<Integer> frameOrder = loadFrameOrder();
            for (int pos = 0; pos < frameOrder.size(); ++pos) {
                if (frameOrder.get(pos) == ID) {
                    return pos;
                }
            }

            return 0; // prevent compiler error
        }

        public int layerIdToPos(int ID) {
            List<Integer> layerOrder = loadLayerOrder();
            for (int pos = 0; pos < layerOrder.size(); ++pos) {
                if (layerOrder.get(pos) == ID) {
                    return pos;
                }
            }

            return 0; // prevent compiler error
        }

        private File getProjectDir() {
            return new File(context.getFilesDir(), "projects/" + projectID);
        }

        private File getFrameDir(int frameID) {
            return new File(context.getFilesDir(), "projects/" + projectID + "/" +
                            Integer.toString(frameID));
        }

        private File getSheetFile(int frameID, int layerID) {
            return new File(context.getFilesDir(), "projects/" + projectID + "/" +
                            Integer.toString(frameID) + "/" + Integer.toString(layerID) + ".PNG");
        }

        private synchronized JSONObject getConfig() {
            String configFilename = "projects/" + projectID + "/config.JSON";
            File configFile = new File(context.getFilesDir(), configFilename);
            String content = "";
            try {
                content = getFileContents(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.v("read config", content);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        private synchronized void putToConfig(String key, Object value) {
            String content = "";
            try {
                JSONObject configObject = getConfig().put(key, value);
                content = configObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("put to config", content);

            String configFilename = "projects/" + projectID + "/config.JSON";
            File configFile = new File(context.getFilesDir(), configFilename);
            try {
                FileWriter writer = new FileWriter(configFile);
                writer.write(content);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized static void initializeConfig(File configFile, int width, int height) {
            String content = "";
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", DEFAULT_TITLE);
                jsonObject.put("width", width);
                jsonObject.put("height", height);
                jsonObject.put("fps", DEFAULT_FPS);
                jsonObject.put("numFrames", DEFAULT_NUM_FRAMES);
                jsonObject.put("numLayers", DEFAULT_NUM_LAYERS);
                jsonObject.put("brushRadius", DEFAULT_BRUSH_RADIUS);
                jsonObject.put("brushColor", DEFAULT_BRUSH_COLOR);
                jsonObject.put("brushAlpha", DEFAULT_BRUSH_ALPHA);
                jsonObject.put("eraserRadius", DEFAULT_ERASER_RADIUS);
                jsonObject.put("eraserAlpha", DEFAULT_ERASER_ALPHA);
                JSONArray zero = new JSONArray();
                zero.put(0);
                jsonObject.put("frameOrder", zero);
                jsonObject.put("layerOrder", zero);
                content = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("project", "could not initialize config file");
            }

            try {
                FileWriter writer = new FileWriter(configFile);
                writer.write(content);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // UTILITY FUNCTIONS //

        private synchronized static void deleteRecursive(File fileOrDirectory) {

            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            }

            if (!fileOrDirectory.delete()) {
                Log.e("project", "recursive delete unsuccessful");
            }
        }

        // read contents of file into string
        private synchronized static String getFileContents(final File file) throws IOException {
            final InputStream inputStream = new FileInputStream(file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            final StringBuilder stringBuilder = new StringBuilder();

            boolean done = false;

            while (!done) {
                final String line = reader.readLine();
                done = (line == null);

                if (line != null) {
                    stringBuilder.append(line);
                }
            }

            reader.close();
            inputStream.close();

            return stringBuilder.toString();
        }

        private static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth,
                                                 int reqHeight) {
            int inSampleSize = 1;

            if (rawHeight > reqHeight || rawWidth > reqWidth) {

                final int halfHeight = rawHeight / 2;
                final int halfWidth = rawWidth / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // rawHeight and rawWidth larger than the requested rawHeight and rawWidth.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }
}
