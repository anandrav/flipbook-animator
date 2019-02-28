package com.arduk.animationcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.LayerViewHolder> {
    private Context context;
    private Project project;
    //fixme you don't need this, just make the background of the imageview white
    // fixme then only use one rgb565 bitmap
    private Bitmap auxBitmap;
    private Canvas auxBitmapCanvas;

    public static class LayerViewHolder extends RecyclerView.ViewHolder {
        private ImageView mBackgroundImageView;
        private ImageView mImageView;
        private TextView mTextView;

        public LayerViewHolder(View itemView) {
            super(itemView);
            mBackgroundImageView = itemView.findViewById(R.id.layer_item_background_image_view);
            mImageView = itemView.findViewById(R.id.layer_item_image_view);
            mTextView = itemView.findViewById(R.id.layer_item_text_view);
        }
    }

    public LayerAdapter(Context context, Project project) {
        this.context = context;
        this.project = project;
        int width = project.getWidth();
        int height = project.getHeight();
        auxBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        auxBitmapCanvas = new Canvas(auxBitmap);
    }

    @NonNull
    @Override
    public LayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layer_item, parent, false);
        LayerViewHolder lvh = new LayerViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(@NonNull LayerViewHolder holder, int position) {
        holder.mTextView.setText(Integer.toString(position + 1));

        //highlight if the currentLayer
        if (position == project.getSelectedLayer()) {
            holder.mBackgroundImageView.setBackgroundColor(Color.RED);
        } else {
            holder.mBackgroundImageView.setBackgroundColor(Color.TRANSPARENT);
        }

        Bitmap bitmap = Bitmap.createBitmap(project.getWidth(), project.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.MAGENTA);
        holder.mImageView.setImageBitmap(bitmap);

        final LayerViewHolder fHolder = holder;
        final int fPosition = position;
        class setImageViewTask extends AsyncTask<Void, Void, Void> {
            private Bitmap viewBitmap;

            @Override
            protected Void doInBackground(Void... params) {
                int scale = 1;
                viewBitmap = Bitmap.createBitmap(project.getWidth()/scale,
                        project.getHeight()/scale, Bitmap.Config.RGB_565);
                Canvas viewBitmapCanvas = new Canvas(viewBitmap);
                viewBitmapCanvas.drawColor(Color.WHITE);

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(1.f/scale, 1.f/scale);
                auxBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                project.drawSheetToCanvas(auxBitmapCanvas, project.getSelectedFrame(), fPosition,
                        scale);
                viewBitmapCanvas.drawBitmap(auxBitmap, 0, 0, null);

                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                fHolder.mImageView.setImageBitmap(viewBitmap);
            }
        }

        new setImageViewTask().execute();

        final LayerAdapter adapter = this;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousSelected = project.getSelectedLayer();
                int pos = fHolder.getLayoutPosition();
                project.selectLayer(pos);
                adapter.notifyItemChanged(pos);
                adapter.notifyItemChanged(previousSelected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return project.getNumLayers();
    }
}
