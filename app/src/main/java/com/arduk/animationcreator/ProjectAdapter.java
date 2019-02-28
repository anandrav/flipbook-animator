package com.arduk.animationcreator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private Context context;

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.project_item_image_view);
            mTextView = itemView.findViewById(R.id.project_item_text_view);
        }
    }

    public ProjectAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        ProjectViewHolder fvh = new ProjectViewHolder(v);
        return fvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        List<String> projectIDs = ProjectFileHandler.getProjectIdList(context);

        final String projectID = projectIDs.get(position);

        final ProjectFileHandler projectFileHandler = ProjectFileHandler.getInstance();
        projectFileHandler.attachToProject(context, projectID);
        holder.mTextView.setText(projectFileHandler.getTitle(projectID));

        Bitmap bitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.MAGENTA);
        holder.mImageView.setImageBitmap(bitmap);


        final ProjectAdapter adapter = this;
        final ProjectViewHolder fHolder = holder;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(projectFileHandler.getTitle(projectID));
                builder.setMessage("Would you like to delete this project?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        projectFileHandler.removeProject(context, projectID);
                        int pos = fHolder.getLayoutPosition();
                        adapter.notifyItemRemoved(pos);

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing

                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, ProjectEditorActivity.class);
                myIntent.putExtra("project_id", projectID);
                myIntent.putExtra("project_mode", ProjectEditorActivity.LOAD_EXISTING_PROJECT);
                context.startActivity(myIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        List<String> projectIDs = ProjectFileHandler.getProjectIdList(context);
        return projectIDs.size();
    }
}