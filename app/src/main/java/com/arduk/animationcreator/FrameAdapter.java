package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {
    private Context context;
    private Project project;
    private ProjectFileHandler projectFileHandler;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private int itemOffset;

    public static class FrameViewHolder extends RecyclerView.ViewHolder {
        private ImageView mBackgroundImageView;
        private ImageView mImageView;
        private TextView mTextView;

        public FrameViewHolder(View itemView) {
            super(itemView);
            mBackgroundImageView = itemView.findViewById(R.id.frame_item_background_image_view);
            mImageView = itemView.findViewById(R.id.frame_item_image_view);
            mTextView = itemView.findViewById(R.id.frame_item_text_view);
        }
    }

    public FrameAdapter(Context context, Project project,
                        RecyclerView.LayoutManager layoutManager) {
        this.context = context;
        this.project = project;
        this.itemOffset = 0;
        this.layoutManager = layoutManager;
        projectFileHandler = ProjectFileHandler.getInstance();
        int width = project.getWidth();
        int height = project.getHeight();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public void setItemOffset(int itemOffset) {
        this.itemOffset = itemOffset;
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_item, parent, false);
        FrameViewHolder fvh = new FrameViewHolder(v);
        return fvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        holder.mTextView.setText(Integer.toString(position + 1));

        //highlight if the currentFrame
//        if (position == project.getSelectedFrame()) {
//            holder.mBackgroundImageView.setBackgroundColor(Color.RED);
//        } else {
//            holder.mBackgroundImageView.setBackgroundColor(Color.rgb(120, 120, 120));
//        }

        Bitmap bitmap = Bitmap.createBitmap(project.getWidth(), project.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        holder.mImageView.setImageBitmap(bitmap);

        final FrameViewHolder fHolder = holder;
        final int SCALE = 1;
        int frameID = project.framePosToID(position);
        if (project.hasFrameCached(frameID)) {
            canvas.drawColor(Color.WHITE);
            for (int layer = project.getNumLayers() - 1; layer >= 0; --layer) {
                project.drawSheetToCanvas(canvas, position, layer, null, null);
            }
            holder.mImageView.setImageBitmap(bitmap);
        } else {
            projectFileHandler.loadSampledFrameIntoImageView(project.getProjectID(), frameID,
                    SCALE, holder.mImageView);
        }

        final FrameAdapter adapter = this;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int previousSelected = project.getSelectedFrame();
//                int pos = fHolder.getLayoutPosition();
//                project.selectFrame(pos);
//                ((ProjectEditorActivity)context).onFrameSelected();
//                adapter.notifyItemChanged(pos);
//                adapter.notifyItemChanged(previousSelected);

                //initiate a scroll to this position
//                int position = fHolder.getLayoutPosition();
//                ((LinearLayoutManager)layoutManager).scrollToPositionWithOffset(position, itemOffset);
//                project.selectFrame(position);
//                ((ProjectEditorActivity)context).onFrameSelected();

//                Log.d("layout position", "layout position" + Integer.toString(fHolder.getLayoutPosition()));
//                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(mRecyclerView.getContext()) {
//                    @Override
//                    protected int getVerticalSnapPreference() {
//                        return LinearSmoothScroller.SNAP_TO_ANY;
//                    }
//
//                    @Override
//                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//                        return 120f / displayMetrics.densityDpi;
//                    }
//                };
//                smoothScroller.setTargetPosition(fHolder.getLayoutPosition());
//                mRecyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            }
        });
    }

    @Override
    public int getItemCount() {
        return project.getNumFrames();
    }
}
