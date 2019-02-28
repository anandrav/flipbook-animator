//package com.arduk.animationcreator;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.util.Log;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
///*
//    Config will contain:
//        projectName
//        numFrames
//        numLayers
//        fps
//        width
//        height
//    stored in a .JSON file
//*/
//
///*
//    Each project will have its own directory named after a UUID
//    Within this directory, each frame will have its own directory named after its number.
//    Within each frame's directory, each layer will have its own PNG file named after its number.
//    In addition, a compressed JPEG will be stored in each frame directory to hold a preview of what
//    the frame looks like, named "preview".
//*/
//
//
//public class ProjectSyncerDEPRECATED {
//    private Context context;
//
//    private String projectID;
//    private String projectName;
//    private File projectDir;
//    private File config;
//
//    private int numFrames;
//    private int numLayers;
//    private int fps;
//    private int width;
//    private int height;
//
//    private int brushColor;
//    private int brushAlpha;
//    private int brushWidth;
//    private int eraserWidth;
//
//    private List<Integer> layerOrder;
//
//    final public String DEFAULT_PROJECT_NAME = "name of project";
//    final public int DEFAULT_NUM_FRAMES = 1;
//    final public int DEFAULT_NUM_LAYERS = 1;
//    final public int DEFAULT_FPS = 24;
//    final public int DEFAULT_WIDTH = 1920;
//    final public int DEFAULT_HEIGHT = 1080;
//
//    final public int DEFAULT_COLOR = 0x000000;
//    final public int DEFAULT_BRUSH_WIDTH = 20;
//    final public int DEFAULT_BRUSH_ALPHA = 255;
//    final public int DEFAULT_ERASER_WIDTH = 120;
//
//    public final int COMPRESS_QUALITY = 100;
//
//    public ProjectSyncerDEPRECATED(Context context) {
//        this.context = context;
//        layerOrder = new ArrayList<>();
//    }
//
//    //fixme must account for corrupted projects
//    //gets information from config file and stores in member variables, including the file itself
//    public void load(String projectID) {
//        this.projectID = projectID;
//
//        projectDir = new File(context.getFilesDir(), projectID);
//
//        config = new File(projectDir, "config.JSON");
//        if (!config.exists()) {
//            try {
//                config.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            projectName = DEFAULT_PROJECT_NAME;
//            numFrames = DEFAULT_NUM_FRAMES;
//            numLayers = DEFAULT_NUM_LAYERS;
//            fps = DEFAULT_FPS;
//            width = DEFAULT_WIDTH;
//            height = DEFAULT_HEIGHT;
//            brushColor = DEFAULT_COLOR;
//            brushWidth = DEFAULT_BRUSH_WIDTH;
//            brushAlpha = DEFAULT_BRUSH_ALPHA;
//            eraserWidth = DEFAULT_ERASER_WIDTH;
//        } else {
//
//            String content = "something went wrong :(";
//
//
//            try {
//                content = getFileContents(config);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            Log.i("content", content);
//
//            JSONObject jsonObject;
//            try {
//                jsonObject = new JSONObject(content);
//
//                try {
//                    projectName = jsonObject.getString("name");
//                } catch (JSONException e) {
//                    projectName = DEFAULT_PROJECT_NAME;
//                }
//                try {
//                    numFrames = jsonObject.getInt("numFrames");
//                } catch (JSONException e) {
//                    numFrames = DEFAULT_NUM_FRAMES;
//                }
//                try {
//                    numLayers = jsonObject.getInt("numLayers");
//                } catch (JSONException e) {
//                    numLayers = DEFAULT_NUM_LAYERS;
//                }
//                try {
//                    fps = jsonObject.getInt("fps");
//                } catch (JSONException e) {
//                    fps = DEFAULT_FPS;
//                }
//                try {
//                    width = jsonObject.getInt("width");
//                } catch (JSONException e) {
//                    width = DEFAULT_WIDTH;
//                }
//                try {
//                    height = jsonObject.getInt("height");
//                } catch (JSONException e) {
//                    height = DEFAULT_HEIGHT;
//                }
//                try {
//                    brushColor = jsonObject.getInt("color");
//                } catch (JSONException e) {
//                    brushColor = DEFAULT_COLOR;
//                }
//                try {
//                    brushAlpha = jsonObject.getInt("brushAlpha");
//                } catch (JSONException e) {
//                    brushAlpha = DEFAULT_BRUSH_ALPHA;
//                }
//                try {
//                    brushWidth = jsonObject.getInt("brushWidth");
//                } catch (JSONException e) {
//                    brushWidth = DEFAULT_BRUSH_WIDTH;
//                }
//                try {
//                    eraserWidth = jsonObject.getInt("eraserWidth");
//                } catch (JSONException e) {
//                    eraserWidth = DEFAULT_ERASER_WIDTH;
//                }
//
//
//
//                JSONArray jArray;
//                try {
//                    jArray = jsonObject.getJSONArray("layerOrder");
//                    layerOrder.clear();
//                    for (int i = 0; i < jArray.length(); i++) {
//                        layerOrder.add(Integer.parseInt(jArray.getString(i)));
//                    }
//                } catch (JSONException e) {
//                    layerOrder.clear();
//                    for (int i = 0; i < numLayers; ++i) {
//                        layerOrder.add(i);
//                    }
//                }
//
//                Log.i("jsonread2", jsonObject.toString());
//
//            } catch (JSONException f) {
//                f.printStackTrace();
//            }
//        }
//    }
//
//    // updates the information in a config file. must load the config file first
//    private void updateConfigFile() {
//        String content = "";
//
//        if (!config.exists()) {
//            try {
//                config.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("name", projectName);
//            jsonObject.put("numFrames", numFrames);
//            jsonObject.put("numLayers", numLayers);
//            jsonObject.put("fps", fps);
//            jsonObject.put("width", width);
//            jsonObject.put("height", height);
//            jsonObject.put("color", brushColor);
//            jsonObject.put("brushAlpha", brushAlpha);
//            jsonObject.put("brushWidth", brushWidth);
//            jsonObject.put("eraserWidth", eraserWidth);
//            JSONArray jArray = new JSONArray();
//            for (int i = 0; i < layerOrder.size(); ++i) {
//                jArray.put(layerOrder.get(i));
//            }
//            jsonObject.put("layerOrder", jArray);
//
//            content = jsonObject.toString();
//            Log.i("jsonwrite", jsonObject.toString());
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            FileWriter writer = new FileWriter(config);
//            writer.write(content);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //creates a new project directory with config file. loads it too
//    public void create(String projectName, int fps, int width, int height) {
//        this.projectName = projectName;
//        numFrames = 0;
//        numLayers = 0;
//        this.fps = fps;
//        this.width = width;
//        this.height = height;
//        brushColor = DEFAULT_COLOR;
//        brushWidth = DEFAULT_BRUSH_WIDTH;
//        brushAlpha = DEFAULT_BRUSH_ALPHA;
//        eraserWidth = DEFAULT_ERASER_WIDTH;
//
//        projectID = UUID.randomUUID().toString();
//
//        projectDir = new File(context.getFilesDir(), projectID);
//        projectDir.mkdir();
//
//        config = new File(projectDir, "config.JSON");
//        try {
//            config.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        updateConfigFile();
//    }
//
//    //returns if a directory for projectName exists
//    /*
//    public boolean projectExists(String projectName) {
//        File projectDir = new File(context.getFilesDir(), projectName);
//        return projectDir.exists();
//    }
//    */
//
//
//    public File[] getListOfProjects() {
//        File[] projects = context.getFilesDir().listFiles();
//        return projects;
//    }
//
//    public Bitmap loadSheetBitmap(int frame, int layer, int reqWidth, int reqHeight) {
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/"
//                + Integer.toString(layer));
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        options.inMutable = true;
//        options.inSampleSize = calculateInSampleSize(getWidth(), getHeight(), reqWidth, reqHeight);
//        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
//        if (bitmap == null) {
//            //could not load from file, create a blank one
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        }
//        return bitmap;
//    }
//
//    public Bitmap loadSheetBitmapForAnimation(int frame, int layerID, int reqWidth, int reqHeight, Bitmap reusableBitmap, Canvas reusableBitmapCanvas) {
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/"
//                + Integer.toString(layerID));
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        options.inMutable = true;
//        options.inBitmap = reusableBitmap;
//        options.inSampleSize = 1;
//        //options.inSampleSize = calculateInSampleSize(loadWidth(), loadHeight(), reqWidth, reqHeight);
//        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
//        if (bitmap == null) {
//            //could not load from file, create a blank one
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        }
//        return bitmap;
//    }
//
//    public Bitmap loadPreviewBitmap(int frame, int reqWidth, int reqHeight) {
//        //fixme this is too slow, must be every 42ms...
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/"
//                + "preview");
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        //options.inSampleSize = 1;
//        options.inMutable = true;
//        //options.inJustDecodeBounds = true; //fixme should I use this?
//        options.inSampleSize = calculateInSampleSize(getWidth(), getHeight(), reqWidth, reqHeight);
//        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
//        if (bitmap == null) {
//            //could not load from file, create a blank one
//            bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565);
//            Canvas canvas = new Canvas(bitmap);
//            canvas.drawColor(Color.WHITE);
//        }
//        return bitmap;
//    }
//
//    public Bitmap loadPreviewBitmapForAnimation(Bitmap reusableBitmap, Canvas reusableBitmapCanvas,
//                                                int frame, int reqWidth, int reqHeight) {
//        //fixme this is too slow, must be every 42ms...
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/"
//                + "preview");
//
//        Log.i("frame", Integer.toString(frame));
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        options.inSampleSize = 2;
//        options.inMutable = true;
//        options.inBitmap = reusableBitmap;
//        //options.inJustDecodeBounds = true; //fixme should I use this?
//        //options.inSampleSize = calculateInSampleSize(loadWidth(), loadHeight(), reqWidth, reqHeight);
//        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
//        if (bitmap == null) {
//            //could not load from file, create a blank one
//            reusableBitmapCanvas.drawColor(Color.WHITE);
//            bitmap = reusableBitmap;
//        }
//        return bitmap;
//    }
//
//    public static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
//        int inSampleSize = 1;
//
//        if (rawHeight > reqHeight || rawWidth > reqWidth) {
//
//            final int halfHeight = rawHeight / 2;
//            final int halfWidth = rawWidth / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // rawHeight and rawWidth larger than the requested rawHeight and rawWidth.
//            while ((halfHeight / inSampleSize) >= reqHeight
//                    && (halfWidth / inSampleSize) >= reqWidth) {
//                inSampleSize *= 2;
//            }
//        }
//
//        return inSampleSize;
//    }
//
//    public void saveDrawSheet(Bitmap bitmap, int frame, int layer) {
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/"
//                                                 + Integer.toString(layer));
//        try {
//            FileOutputStream out = new FileOutputStream(bitmapFile);
//            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void saveDrawFramePreview(Bitmap bitmap, int frame) {
//        File bitmapFile = new File(projectDir, Integer.toString(frame) + "/" + "preview");
//
//        try {
//            FileOutputStream out = new FileOutputStream(bitmapFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void addFrame() {
//        //create directory for frame and add bitmap file for every layer
//        File frameDir = new File(projectDir, Integer.toString(numFrames));
//        frameDir.mkdir();
//        for (int i = 0; i < numLayers; ++i) {
//            File bitmapFile = new File(frameDir, Integer.toString(i));
//            try {
//                bitmapFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        File bitmapFile = new File(frameDir, "preview");
//        try {
//            bitmapFile.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        numFrames += 1;
//        updateConfigFile();
//    }
//
//    public void removeFrame(int index) {
//        //delete every bitmap file in the frame's directory and delete the directory
//        File frameDir = new File(projectDir, Integer.toString(index));
//        /*
//        for (int i = 0; i < numLayers; ++i) {
//            File bitmapFile = new File(frameDir, Integer.toString(i));
//            bitmapFile.delete();
//        }
//        File bitmapFile = new File(frameDir, "preview");
//        bitmapFile.delete();
//        frameDir.delete();
//        */
//        deleteRecursive(frameDir);
//
//        //rename the directories of the frames after to one less
//        for (int i = index + 1; i < numFrames; ++i) {
//            File directory1 = new File(projectDir, Integer.toString(i));
//            File directory2 = new File(projectDir, Integer.toString(i - 1));
//            directory1.renameTo(directory2);
//        }
//
//        numFrames -= 1;
//        updateConfigFile();
//    }
//
//    public void swapFrames(int frame1, int frame2) {
//        File directory1 = new File(projectDir, Integer.toString(frame1));
//        File directory2 = new File(projectDir, Integer.toString(frame2));
//
//        File temp = new File(directory1.getPath());
//        directory1.renameTo(directory2);
//        directory2.renameTo(temp);
//    }
//
//    public void addLayer() {
//        for (int i = 0; i < numFrames; ++i) {
//            File bitmapFile = new File(projectDir, Integer.toString(i) + "/"
//                                                    + Integer.toString(numLayers - 1));
//            try {
//                bitmapFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        numLayers += 1;
//        layerOrder.add(layerOrder.size());
//        updateConfigFile();
//    }
//
//    public void removeLayer(int layerID) {
//        int layerPos = layerIdToPos(layerID);
//
//        // rename image files in internal storage
//        for (int f = 0; f < numFrames; ++f) {
//            for (int l = 0; l < numLayers; ++l) {
//                File bitmapFile = new File(projectDir, Integer.toString(f) + "/"
//                        + Integer.toString(l));
//                if (l == layerID) {
//                    // delete the bitmap for the deleted layer
//                    bitmapFile.delete();
//                }
//                if (l > layerID) {
//                    // for all remaining bitmaps, decrement layerID
//                    File bitmapFile2 = new File(projectDir, Integer.toString(f) + "/"
//                            + Integer.toString(l - 1));
//                    bitmapFile.renameTo(bitmapFile2);
//                }
//            }
//        }
//
//
//
////        // move all values of layerOrder at index > layerPos down one, because
////        // position n becomes position (n - 1) for all positions after the deleted position
////        for (int i = layerPos; i + 1 < layerOrder.size(); ++i) {
////            int nextValue = layerOrder.get(i + 1);
////            layerOrder.set(i, nextValue);
////        }
////        layerOrder.remove(layerOrder.size() - 1);
//        layerOrder.remove(layerPos);
//
//        // decrement all values of layerOrder which are larger than layerId, the 'value'
//        for (int i = 0; i < layerOrder.size(); ++i) {
//            if (layerOrder.get(i) > layerID) {
//                int temp = layerOrder.get(i) - 1;
//                layerOrder.set(i, temp);
//            }
//        }
//
//        numLayers -= 1;
//
//        updateConfigFile();
//    }
//
//    public void swapLayers(int layer1, int layer2) {
//        int temp = layerOrder.get(layer1);
//        layerOrder.set(layer1, layerOrder.get(layer2));
//        layerOrder.set(layer2, temp);
//
//        updateConfigFile();
//    }
//
//    public void deleteProject(String projectID) {
//        File projectDir = new File(context.getFilesDir(), projectID);
//        deleteRecursive(projectDir);
//    }
//
//    private void deleteRecursive(File fileOrDirectory) {
//
//        if (fileOrDirectory.isDirectory()) {
//            for (File child : fileOrDirectory.listFiles()) {
//                deleteRecursive(child);
//            }
//        }
//
//        fileOrDirectory.delete();
//    }
//
//    private static String getFileContents(final File file) throws IOException {
//        final InputStream inputStream = new FileInputStream(file);
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//        final StringBuilder stringBuilder = new StringBuilder();
//
//        boolean done = false;
//
//        while (!done) {
//            final String line = reader.readLine();
//            done = (line == null);
//
//            if (line != null) {
//                stringBuilder.append(line);
//            }
//        }
//
//        reader.close();
//        inputStream.close();
//
//        return stringBuilder.toString();
//    }
//
//    public String getProjectID() {
//        return projectID;
//    }
//
//    public String getProjectName() {
//        return projectName;
//    }
//
//    public int getNumFrames() {
//        return numFrames;
//    }
//
//    public int getNumLayers() {
//        return numLayers;
//    }
//
//    public List<Integer> getLayerOrder() {
//        return layerOrder;
//    }
//
//    public int getFps() {
//        return fps;
//    }
//
//    public int getWidth() {
//        return width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public int getBrushColor() {
//        return brushColor;
//    }
//
//    public void setBrushColor(int color) {
//        brushColor = color;
//        updateConfigFile();
//    }
//
//    public int getBrushAlpha() {
//        return brushAlpha;
//    }
//
//    public void setBrushAlpha(int alpha) {
//        brushAlpha = alpha;
//        updateConfigFile();
//    }
//
//    public int getBrushWidth() {
//        return brushWidth;
//    }
//
//    public void setBrushWidth(float width) {
//        brushWidth = Math.round(width);
//        updateConfigFile();
//    }
//
//    public int getEraserWidth() {
//        return eraserWidth;
//    }
//
//    public void setEraserWidth(float width) {
//        eraserWidth = Math.round(width);
//        updateConfigFile();
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
//}
