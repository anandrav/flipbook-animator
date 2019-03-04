package com.arduk.animationcreator;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Scroller;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;


public class ProjectEditorActivity extends AppCompatActivity /*implements View.OnClickListener*/ {
    public static final int MAX_LAYERS = 3;
    public static final int MIN_BRUSH_SIZE = 1;
    public static final int MAX_BRUSH_SIZE = 128;
    public static final int MIN_ERASER_SIZE = 1;
    public static final int MAX_ERASER_SIZE = 256;

    private ProjectFileHandler projectFileHandler;

    private Project project;

    private DrawView drawView;

    private RecyclerView frameRecyclerView;
    private RecyclerView.LayoutManager mFrameRecyclerViewLayoutManager;
    private FrameAdapter mFrameAdapter;
    private int frameItemWidth = -1;
    private int frameItemOffset = -1;

    /* layer editor stuff */
    private RecyclerView layerRecyclerView;
    private RecyclerView.LayoutManager mLayerRecyclerViewLayoutManager;
    private LayerAdapter mLayerAdapter;

    private BottomSheetBehavior bottomSheetBehavior;
    private ImageButton showBottomSheetDialogButton;

    private ColorSelectionFragment colorFragment;

    private ImageButton playAnimationButton;

    public static final int CREATE_NEW_PROJECT = 0;
    public static final int LOAD_EXISTING_PROJECT = 1;

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (colorFragment.isVisible()) {
            getFragmentManager().beginTransaction()
                    .hide(colorFragment)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    public void onDrawSheetActivated(int frameID, int layerID) {
        int framePos = project.frameIdToPos(frameID);
        int layerPos = project.layerIdToPos(layerID);

        mFrameAdapter.notifyItemChanged(framePos);

        drawView.notifyFrameLayerSelection();
    }

    public void onFrameSelected() {
        drawView.notifyFrameLayerSelection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_editor_coordinator);

        projectFileHandler = ProjectFileHandler.getInstance();

        setupProject();

        // canvas that is drawn on
        drawView = (DrawView)findViewById(R.id.draw_view);
        drawView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        drawView.init();
        drawView.attachToProject(project);

        // color picker
        colorFragment = new ColorSelectionFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .add(R.id.color_selection_container, colorFragment, "colorFragment")
                .hide(colorFragment)
                .commit();

        // initialize frameRecyclerView
        frameRecyclerView = findViewById(R.id.frameRecyclerView);
        frameRecyclerView.setHasFixedSize(true);
        mFrameRecyclerViewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        frameRecyclerView.setLayoutManager(mFrameRecyclerViewLayoutManager);
        class FastLinearSnapHelper extends LinearSnapHelper {
        }
        final SnapHelper snapHelper = new FastLinearSnapHelper();
        snapHelper.attachToRecyclerView(frameRecyclerView);
        OffsetItemDecoration dividerItemDecoration = new OffsetItemDecoration(frameRecyclerView.getContext());
        //frameRecyclerView.addItemDecoration(dividerItemDecoration);
        mFrameAdapter = new FrameAdapter(this, project, mFrameRecyclerViewLayoutManager);
        frameRecyclerView.setAdapter(mFrameAdapter);
        //disable dim animation on item change
        ((SimpleItemAnimator) frameRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        frameRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (frameRecyclerView.computeHorizontalScrollOffset() == 0) {
                    //get cell width for animating the scrubbing preview
                    if (frameItemWidth == -1) {
                        View itemView = frameRecyclerView.findViewHolderForLayoutPosition(0).itemView;
                        int itemWidth = itemView.getWidth();
                        int itemMargins = ((ViewGroup.MarginLayoutParams) frameRecyclerView.findViewHolderForLayoutPosition(0).itemView.getLayoutParams()).leftMargin +
                                ((ViewGroup.MarginLayoutParams) frameRecyclerView.findViewHolderForLayoutPosition(0).itemView.getLayoutParams()).rightMargin;
                        frameItemWidth = itemWidth + itemMargins;
                    }

                    // get offset for recyclerview
                    if (frameItemOffset == -1) {
                        Log.d("offset", "CALCULATED OFFSET");
                        View itemView = frameRecyclerView.findViewHolderForLayoutPosition(0).itemView;
                        ImageView childImage = (ImageView) ((ViewGroup) itemView).getChildAt(0);
                        int offset = (int) (getScreenWidth() / (float) (2)) - childImage.getLayoutParams().width / 2;
                        offset -= ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).leftMargin;
                        frameItemOffset = offset;
//                        frameRecyclerView.setPadding(offset, 0, offset, 0);

                        // tell offset to frameAdapter
                        mFrameAdapter.setItemOffset(offset);
                    }
                }
            }
        });
        final Handler scrubbingHandler = new Handler();
        frameRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                View centerView = snapHelper.findSnapView(mFrameRecyclerViewLayoutManager);
                int pos = mFrameRecyclerViewLayoutManager.getPosition(centerView);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // if stopped scrolling then stop scrubbing preview runnable
                    scrubbingHandler.removeCallbacksAndMessages(null);
                    // if stopped scrolling then select frame
                    int prevSelected = project.getSelectedFrame();
                    project.selectFrame(pos);
                    drawView.notifyFrameLayerSelection();
                    //mFrameAdapter.notifyItemChanged(pos);
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // if started scrolling then start scrubbing preview runnable
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            View centerView = snapHelper.findSnapView(mFrameRecyclerViewLayoutManager);
                            int pos = mFrameRecyclerViewLayoutManager.getPosition(centerView);
                            View itemView = frameRecyclerView.findViewHolderForLayoutPosition(pos).itemView;
                            ImageView imageView = (ImageView)((ViewGroup)itemView).getChildAt(1);
                            Bitmap previewBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                            Log.d("scrubbingRunnable", "RAN!");
                            drawView.invalidateWithBitmap(previewBitmap);
                            final int UPDATE_PREVIEW_DELAY = 40;
                            scrubbingHandler.postDelayed(this, UPDATE_PREVIEW_DELAY);
                        }
                    };
                    scrubbingHandler.post(r);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            int dragFrom = -1;
            int dragTo = -1;

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Log.d("dragged", "dragged: " + Integer.toString(position_dragged));
                Log.d("dragged", "target: " + Integer.toString(position_target));

                if (dragFrom == -1) {
                    dragFrom = position_dragged;
                }
                dragTo = position_target;

                project.swapFrames(position_dragged, position_target);
                mFrameAdapter.notifyItemMoved(position_target, position_dragged);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

//                if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
//                    reallyMoved(dragFrom, dragTo);
//                }
//                dragFrom = -1;
//                dragTo = -1;
                //alignWithSnap();
            }

            private void alignWithSnap() {
                View centerView = snapHelper.findSnapView(mFrameRecyclerViewLayoutManager);
                int pos = mFrameRecyclerViewLayoutManager.getPosition(centerView);
                ((LinearLayoutManager)mFrameRecyclerViewLayoutManager)
                        .scrollToPositionWithOffset(pos, frameItemOffset);
                project.selectFrame(pos);
                onFrameSelected();
            }
        });
        itemTouchHelper.attachToRecyclerView(frameRecyclerView);

        // initialize layerRecyclerView
        layerRecyclerView = findViewById(R.id.layerRecyclerView);
        layerRecyclerView.setHasFixedSize(true);
        mLayerRecyclerViewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //not needed
        layerRecyclerView.setLayoutManager(mLayerRecyclerViewLayoutManager);
        mLayerAdapter = new LayerAdapter(this, project);
        layerRecyclerView.setAdapter(mLayerAdapter);
        //disable dim animation on item change
        ((SimpleItemAnimator) layerRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        ViewCompat.setNestedScrollingEnabled(layerRecyclerView, false);

        // layer editor
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.layerEditorLayout));
        showBottomSheetDialogButton = (ImageButton) findViewById(R.id.layer_button);
        showBottomSheetDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayerAdapter.notifyDataSetChanged();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (bottomSheetBehavior instanceof LockableBottomSheetBehavior) {
                        ((LockableBottomSheetBehavior) bottomSheetBehavior).setLocked(true);
                    }
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        ImageButton brushButton = findViewById(R.id.brush_tool_button);
        brushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setCurrentTool(DrawView.editorTool.brush);
            }
        });
        ImageButton eraseButton = findViewById(R.id.erase_tool_button);
        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setCurrentTool(DrawView.editorTool.eraser);
            }
        });

        ImageButton undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.makeUndo();
            }
        });

        ImageButton redoButton = findViewById(R.id.redo_button);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.makeRedo();
            }
        });

        ImageButton paletteButton = findViewById(R.id.palette_button);
        paletteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make the palette activity visible
                getFragmentManager().beginTransaction()
                        .show(colorFragment)
                        .commit();
            }
        });

        ImageButton onionSkinButton = findViewById(R.id.onion_skin_button);
        onionSkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.toggleOnionSkin();
            }
        });

        playAnimationButton = findViewById(R.id.play_animation_button);
        playAnimationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.togglePreviewAnimation();
                if (drawView.isPlayingAnimation()) {
                    ((ImageButton)v).setImageResource(R.drawable.ic_baseline_stop_24px);
                } else {
                    ((ImageButton)v).setImageResource(R.drawable.ic_baseline_play_arrow_24px);
                }
            }
        });

        ImageButton addFrameButton = findViewById(R.id.add_frame_button);
        addFrameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                project.insertFrame(project.getNumFrames());
                mFrameAdapter.notifyItemChanged(project.getNumFrames()-2);
                mFrameAdapter.notifyItemInserted(project.getNumFrames()-1);
            }
        });

        ImageButton removeFrameButton = findViewById(R.id.delete_frame_button);
        removeFrameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getNumFrames() == 1) { return; }

                int selectedFrame = project.getSelectedFrame();
                project.removeFrame(selectedFrame);
                if (selectedFrame == project.getNumFrames()) {
                    project.selectFrame(project.getNumFrames() - 1);
                }

                mFrameAdapter.notifyItemRemoved(selectedFrame);
                mFrameAdapter.notifyItemRangeChanged(selectedFrame-1,
                        project.getNumFrames() - selectedFrame + 1);
                drawView.notifyFrameLayerSelection();
            }
        });

        ImageButton addLayerButton = findViewById(R.id.add_layer_button);
        addLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getNumLayers() < 3) {
                    project.insertLayer(project.getNumLayers());

                    mLayerAdapter.notifyItemInserted(project.getNumLayers() - 1);
                }
            }
        });

        ImageButton removeLayerButton = findViewById(R.id.delete_layer_button);
        removeLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getNumLayers() == 1) { return; }

                int selectedLayer = project.getSelectedLayer();
                project.removeLayer(selectedLayer);
                if (selectedLayer == project.getNumLayers()) {
                    project.selectLayer(project.getNumLayers() - 1);
                }

                drawView.notifyFrameLayerSelection();
                mLayerAdapter.notifyItemRemoved(selectedLayer);
                mLayerAdapter.notifyItemRangeChanged(selectedLayer-1,
                        project.getNumLayers() - selectedLayer + 1);
            }
        });

        ImageButton swapAboveButton = findViewById(R.id.swap_above_button);
        swapAboveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int layer1 = project.getSelectedLayer();
                int layer2 = layer1 - 1;
                if (layer2 >= 0) {
                    project.swapLayers(layer1, layer2);
                    project.selectLayer(layer2);

                    mLayerAdapter.notifyItemMoved(layer1, layer2);
                    mLayerAdapter.notifyItemChanged(layer1);
                    mLayerAdapter.notifyItemChanged(layer2);
                    drawView.notifyFrameLayerSelection();
                }
            }
        });

        ImageButton swapBelowButton = findViewById(R.id.swap_below_button);
        swapBelowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int layer2 = project.getSelectedLayer();
                int layer1 = layer2 + 1;
                if (layer1 < project.getNumLayers()) {
                    project.swapLayers(layer1, layer2);
                    project.selectLayer(layer1);

                    mLayerAdapter.notifyItemMoved(layer1, layer2);
                    mLayerAdapter.notifyItemChanged(layer1);
                    mLayerAdapter.notifyItemChanged(layer2);
                    drawView.notifyFrameLayerSelection();
                }
            }
        });

        ImageButton swapRightButton = findViewById(R.id.swap_right_button);
        swapRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frame1 = project.getSelectedFrame();
                int frame2 = frame1 + 1;
                if (frame2 < project.getNumFrames()) {
                    project.swapFrames(frame1, frame2);
                    project.selectFrame(frame2);

                    mFrameAdapter.notifyItemMoved(frame1, frame2);
                    mFrameAdapter.notifyItemChanged(frame1);
                    mFrameAdapter.notifyItemChanged(frame2);
                    drawView.notifyFrameLayerSelection();
                }
            }
        });

        ImageButton swapLeftButton = findViewById(R.id.swap_left_button);
        swapLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frame1 = project.getSelectedFrame();
                int frame2 = frame1 - 1;
                if (frame2 >= 0) {
                    project.swapFrames(frame1, frame2);
                    project.selectFrame(frame2);

                    mFrameAdapter.notifyItemMoved(frame2, frame1);
                    mFrameAdapter.notifyItemChanged(frame1);
                    mFrameAdapter.notifyItemChanged(frame2);
                    drawView.notifyFrameLayerSelection();
                }
            }
        });

        ImageButton copyRightButton = findViewById(R.id.copy_frame_right_button);
        copyRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a new frame to the right, copy to it
                project.insertFrame(project.getSelectedFrame()+1);
                project.copyFrameToClipboard(project.getSelectedFrame());
                project.pasteFrameFromClipboard(project.getSelectedFrame()+1);

                mFrameAdapter.notifyItemChanged(project.getNumFrames()-2);
                mFrameAdapter.notifyItemInserted(project.getSelectedFrame()+1);
                drawView.notifyFrameLayerSelection();
            }
        });
    }

    @Override
    protected void onPause() {
        //prevent runnable from continuing in the background
        if (drawView.isPlayingAnimation()) {
            drawView.togglePreviewAnimation();
        }
        if (drawView.isPlayingAnimation()) {
            playAnimationButton.setImageResource(R.drawable.ic_baseline_stop_24px);
        } else {
            playAnimationButton.setImageResource(R.drawable.ic_baseline_play_arrow_24px);
        }
        project.saveFramesInCache();

        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    public void setCurrentColor(int color) {
        drawView.setColor(color);
        if (drawView.getCurrentTool() == DrawView.editorTool.brush) {
            projectFileHandler.setBrushColor(color);
        }
    }

    public int getCurrentColor() {
        return drawView.getColor();
    }

    public void setCurrentAlpha(int alpha) {
        drawView.setAlphaValue(alpha);
        if (drawView.getCurrentTool() == DrawView.editorTool.brush) {
            projectFileHandler.setBrushAlpha(alpha);
        } else if (drawView.getCurrentTool() == DrawView.editorTool.eraser){
            projectFileHandler.setEraserAlpha(alpha);
        }
    }

    public int getCurrentAlpha(){
        return drawView.getAlphaValue();
    }

    public DrawView.editorTool getCurrentTool() {
        return drawView.getCurrentTool();
    }

    public void setCurrentBrushSize(float width) {
        drawView.setBrushWidth(width);
        if (drawView.getCurrentTool() == DrawView.editorTool.brush) {
            projectFileHandler.setBrushRadius(Math.round(width));
        } else if (drawView.getCurrentTool() == DrawView.editorTool.eraser){
            projectFileHandler.setEraserRadius(Math.round(width));
        }
    }

    public float getCurrentBrushSize() {
        return drawView.getBrushWidth();
    }

    private void setupProject() {
        int mode = getIntent().getIntExtra("project_mode", 0);
        if (mode == CREATE_NEW_PROJECT) {
            String title = getIntent().getStringExtra("title");
            int fps = getIntent().getIntExtra("fps", 12);
            int width = getIntent().getIntExtra("width", 1920);
            int height = getIntent().getIntExtra("height", 1080);

            String projectID = projectFileHandler.createProject(this, width, height);
            projectFileHandler.attachToProject(this, projectID);
            project = new Project(this, projectID);
            project.setTitle(title);
            project.setFps(fps);
        } else if (mode == LOAD_EXISTING_PROJECT) {
            String projectID = getIntent().getStringExtra("project_id");
            projectFileHandler.attachToProject(this, projectID);
            project = new Project(this, projectID);
        }
    }

    private int getScreenWidth() {

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}