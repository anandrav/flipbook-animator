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
import android.view.MotionEvent;
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
    private View.OnClickListener onClickListener;
    private int itemOffset = 0;
    private int itemWidth = 0;

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
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int input_position) {
        if (input_position == 0 || input_position == getItemCount() - 1) {
            //dummy elements to add a margins to recyclerview
            //set width to the offset
            ViewGroup.LayoutParams params = holder.mBackgroundImageView.getLayoutParams();
            if (itemWidth == 0) {
                itemWidth = params.width;
            }
            params.width = itemOffset;
            holder.mBackgroundImageView.setLayoutParams(params);
            //make it invisible
            holder.mBackgroundImageView.setVisibility(View.INVISIBLE);
            holder.mImageView.setVisibility(View.INVISIBLE);
            holder.mTextView.setVisibility(View.INVISIBLE);

            return;
        }
        //since views are reused, make sure view group is made normal
        ViewGroup.LayoutParams params = holder.mBackgroundImageView.getLayoutParams();
        params.width = itemWidth;
        holder.mBackgroundImageView.setLayoutParams(params);
        //make it visible now
        holder.mBackgroundImageView.setVisibility(View.VISIBLE);
        holder.mImageView.setVisibility(View.VISIBLE);
        holder.mTextView.setVisibility(View.VISIBLE);

        int position = input_position - 1;

        holder.mTextView.setText(Integer.toString(position + 1));

        Bitmap bitmap = Bitmap.createBitmap(project.getWidth(), project.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        holder.mImageView.setImageBitmap(bitmap);

        final FrameViewHolder fHolder = holder;
        final int SCALE = 1;
        int frameID = project.framePosToID(position);
        if (project.hasFrameCached(frameID)) {
            canvas.drawColor(Color.WHITE);
            for (int layer = 0; layer < project.getNumLayers(); ++layer) {
                project.drawSheetToCanvas(canvas, position, layer, null, null);
            }
            holder.mImageView.setImageBitmap(bitmap);
        } else {
            projectFileHandler.loadSampledFrameIntoImageView(project.getProjectID(), frameID,
                    SCALE, holder.mImageView);
        }

        final FrameAdapter adapter = this;
        View.OnClickListener listener = ((ProjectEditorActivity)context).new showFrameButtonsOnClickListener(position);
        holder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return project.getNumFrames() + 2;
    }
}
