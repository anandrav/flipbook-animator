//package com.arduk.animationcreator;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.Handler;
//import android.support.annotation.Nullable;
//
//import java.util.LinkedList;
//import java.util.Queue;
//
//public class ImageRequestHandlerDEPRECATED {
//
//    private Context context;
//
//    private Queue<Request> queue;
//
//    private Handler handler;
//
//    private static ImageRequestHandlerDEPRECATED instance;
//
//    private final int POST_DELAY = 10;
//
//    public static ImageRequestHandlerDEPRECATED getInstance(Context context) {
//        if (instance == null) {
//            instance = new ImageRequestHandlerDEPRECATED(context);
//        }
//        return instance;
//    }
//
//    // FIXME THIS DOES NOT USE A SEPARATE THREAD, USE A HANDLERTHREAD NOT JUST A HANDLER
//    // private constructor
//    private ImageRequestHandlerDEPRECATED(Context context) {
//        this.context = context;
//
//        queue = new LinkedList<>();
//
//        handler = new Handler();
//
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (!queue.isEmpty()) {
//                    Request request = queue.remove();
//                    request.execute();
//                }
//                handler.postDelayed(this, POST_DELAY);
//            }
//        };
//
//        handler.post(runnable);
//    }
//
//    public void add(Request request) {
//        queue.add(request);
//    }
//
//    private abstract class Request {
//        abstract String execute();
//    }
//
//    public interface Response {
//        void doSomething(Bitmap loadedBitmap);
//    }
//
//    public class LoadSheetRequest extends Request {
//
//        private String projectID;
//
//        private int frameID;
//
//        private int layerID;
//
//        private Bitmap bitmap;
//
//        private Response response;
//
//        // constructor
//        // if a bitmap is specified, sheet will be loaded into that reusable bitmap
//        // else, a new bitmap will be created in memory
//        public LoadSheetRequest(String projectID, int frameID, int layerID, @Nullable Bitmap bitmap,
//                                Response response) {
//            this.projectID = projectID;
//            this.frameID = frameID;
//            this.layerID = layerID;
//            this.bitmap = bitmap;
//            this.response = response;
//        }
//
//        public String execute() {
//            ProjectFileHandler.ProjectReaderWriter projectReaderWriter =
//                    new ProjectFileHandler.ProjectReaderWriter(context, projectID);
//            int width = projectReaderWriter.loadWidth();
//            int height = projectReaderWriter.loadHeight();
//            bitmap = projectReaderWriter.loadSheetBitmap(frameID, layerID, width, height, bitmap);
//
//            response.doSomething(bitmap);
//
//            return "Executed";
//        }
//    }
//
//    public class SaveSheetRequest extends Request {
//
//        private String projectID;
//
//        private int frameID;
//
//        private int layerID;
//
//        private Bitmap bitmap;
//
//        private Response response;
//
//        // constructor
//        public SaveSheetRequest(String projectID, int frameID, int layerID, Bitmap bitmap,
//                                @Nullable Response response) {
//            this.projectID = projectID;
//            this.frameID = frameID;
//            this.layerID = layerID;
//            this.bitmap = bitmap;
//            this.response = response;
//        }
//
//        public String execute() {
//            ProjectFileHandler.ProjectReaderWriter projectReaderWriter =
//                    new ProjectFileHandler.ProjectReaderWriter(context, projectID);
//            int width = projectReaderWriter.loadWidth();
//            int height = projectReaderWriter.loadHeight();
//            projectReaderWriter.saveSheetBitmap(bitmap, frameID, layerID);
//
//            if (response != null) {
//                response.doSomething(bitmap);
//            }
//
//            return "Executed";
//        }
//    }
//}
