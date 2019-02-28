package com.arduk.animationcreator;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;

public class DrawEdit {
    private DrawView.editorTool tool;
    private Path path;
    private Paint paint;

    public DrawEdit(DrawView.editorTool tool_in, Path path_in, Paint paint_in) {
        tool = tool_in;
        path = new Path(path_in);
        paint = new Paint(paint_in);
    }

    public DrawEdit(DrawEdit edit) {
        tool = edit.tool;
        path = new Path(edit.path);
        paint = new Paint(edit.paint);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void setTool(DrawView.editorTool tool) {
        this.tool = tool;
    }

    public Path getPath() {
        return path;
    }

    public Paint getPaint() {
        return paint;
    }

    public DrawView.editorTool getTool() {
        return tool;
    }
}
