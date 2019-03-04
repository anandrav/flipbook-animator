package com.arduk.animationcreator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorSelectionFragment extends Fragment {
    private ColorSelectionSquareView squareView;
    private ColorSelectionDialView dialView;
    private ColorSelectionDialIndicatorView dialIndicatorView;
    private EditText hexEditText;
    private TextView opacityText;
    private TextView sizeText;
    private SeekBar opacityBar;
    private SeekBar sizeBar;

    private float[] hsv = {0.f, 0.f, 0.f};
    private float alpha = 1.0f;
    private float brushWidth = 20;

    private float MIN_BRUSH_SIZE;
    private float MAX_BRUSH_SIZE;

    private float MIN_ERASER_SIZE;
    private float MAX_ERASER_SIZE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_color_selection, container, false);

        MIN_BRUSH_SIZE = ((ProjectEditorActivity)getActivity()).MIN_BRUSH_SIZE;
        MAX_BRUSH_SIZE = ((ProjectEditorActivity)getActivity()).MAX_BRUSH_SIZE;

        MIN_ERASER_SIZE = ((ProjectEditorActivity)getActivity()).MIN_ERASER_SIZE;
        MAX_ERASER_SIZE = ((ProjectEditorActivity)getActivity()).MAX_ERASER_SIZE;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton closeButton = getView().findViewById(R.id.close_color_picker_button);
        final ColorSelectionFragment self = this;
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().hide(self).commit();
            }
        });

        squareView = getView().findViewById(R.id.color_selection_square_view);
        squareView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setupSquare();

        dialView = getView().findViewById(R.id.color_selection_dial_view);
        dialView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        dialIndicatorView = getView().findViewById(R.id.color_selection_dial_indicator_view);
        setupDial();

        hexEditText = getView().findViewById(R.id.hex_value_edit_text);
        setupEditText();

        opacityText = getView().findViewById(R.id.opacity_text_view);
        opacityBar = getView().findViewById(R.id.opacity_seek_bar);
        sizeText = getView().findViewById(R.id.brush_radius_text_view);
        sizeBar = getView().findViewById(R.id.brush_radius_seek_bar);
        setupSeekBars();

        //Intent intent = getIntent();
       // int mColor = intent.getIntExtra("color", 0xabcdef);
        int mColor = ((ProjectEditorActivity)getActivity()).getCurrentColor();
        Color.colorToHSV(mColor, hsv);

        dialIndicatorView.pointIndicator(hsv[0]);
        squareView.setSaturation(hsv[1]);
        squareView.setLightness(hsv[2]);
        updateSquareView();
        updateHexEditText();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            alpha = ((ProjectEditorActivity)getActivity()).getCurrentAlpha();
            int progress = (int)(alpha / 2.55);
            opacityBar.setProgress(progress);
            opacityText.setText(Integer.toString(progress) + "%");

            brushWidth = ((ProjectEditorActivity)getActivity()).getCurrentBrushSize();
            Log.i("brushWidth", Float.toString(brushWidth));
            DrawView.editorTool tool = ((ProjectEditorActivity)getActivity()).getCurrentTool();
            if (tool == DrawView.editorTool.brush) {
                progress = (int) (100 * (brushWidth - MIN_BRUSH_SIZE) / (MAX_BRUSH_SIZE - MIN_BRUSH_SIZE));
            } else {
                progress = (int) (100 * (brushWidth - MIN_ERASER_SIZE) / (MAX_ERASER_SIZE - MIN_ERASER_SIZE));
            }
            sizeBar.setProgress(progress);
            sizeText.setText(Integer.toString(Math.round(brushWidth)));
            Log.i("rounded", Integer.toString(Math.round(brushWidth)));
        }
    }

    void setupDial() {
        dialIndicatorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!dialView.withinBounds(event)) { return false; }
                        hsv[0] = positiveMod((dialIndicatorView.getAngle(event) + 90), 360);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        hsv[0] = positiveMod((dialIndicatorView.getAngle(event) + 90), 360);
                        break;
                    case MotionEvent.ACTION_UP:
                        hsv[0] = positiveMod((dialIndicatorView.getAngle(event) + 90), 360);
                        break;
                    default:
                        return false;
                }
                //update stuff based on new hue
                dialIndicatorView.pointIndicator(hsv[0]);
                updateSquareView();
                updateHexEditText();
                updateIntent();
                return true;
            }
        });
    }

    void setupSquare() {
        squareView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        squareView.updateIndicator(event);
                        hsv[1] = squareView.getSaturation();
                        hsv[2] = squareView.getLightness();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        squareView.updateIndicator(event);
                        hsv[1] = squareView.getSaturation();
                        hsv[2] = squareView.getLightness();
                        break;
                    case MotionEvent.ACTION_UP:
                        squareView.updateIndicator(event);
                        hsv[1] = squareView.getSaturation();
                        hsv[2] = squareView.getLightness();
                        break;
                    default:
                        return false;
                }
                //update stuff based on new hue
                updateSquareView();
                updateHexEditText();
                updateIntent();
                return true;
            }
        });
    }

    private void setupEditText() {
        hexEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = hexEditText.getText().toString();
                    try {
                        int mColor = Color.parseColor(text);
                        Color.colorToHSV(mColor, hsv);

                        dialIndicatorView.pointIndicator(hsv[0]);
                        updateSquareView();
                        updateIntent();

                        return true;
                    } catch (IllegalArgumentException e) {
                        //do nothing
                    }
                }

                return false;
            }
        });
    }

    void setupSeekBars() {
        alpha = ((ProjectEditorActivity)getActivity()).getCurrentAlpha();
        int progress = (int)(alpha / 2.55);
        opacityBar.setProgress(progress);
        opacityText.setText(Integer.toString(progress) + "%");
        opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                opacityText.setText(Integer.toString(progress) + "%");
                alpha = (int)(progress * 2.55);
                ((ProjectEditorActivity)getActivity()).setCurrentAlpha((int)alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brushWidth = ((ProjectEditorActivity)getActivity()).getCurrentBrushSize();
        DrawView.editorTool tool = ((ProjectEditorActivity)getActivity()).getCurrentTool();
        if (tool == DrawView.editorTool.brush) {
            progress = (int) (100 * (brushWidth - MIN_BRUSH_SIZE) / (MAX_BRUSH_SIZE - MIN_BRUSH_SIZE));
        } else {
            progress = (int) (100 * (brushWidth - MIN_ERASER_SIZE) / (MAX_ERASER_SIZE - MIN_ERASER_SIZE));
        }
        sizeBar.setProgress(progress);
        sizeText.setText(Integer.toString(Math.round(brushWidth)));
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DrawView.editorTool tool = ((ProjectEditorActivity)getActivity()).getCurrentTool();
                if (tool == DrawView.editorTool.brush) {
                    brushWidth = MIN_BRUSH_SIZE + progress * ((MAX_BRUSH_SIZE - MIN_BRUSH_SIZE) / 100);
                } else {
                    brushWidth = MIN_ERASER_SIZE + progress * ((MAX_ERASER_SIZE - MIN_ERASER_SIZE) / 100);
                }
                sizeText.setText(Integer.toString(Math.round(brushWidth)));
                ((ProjectEditorActivity)getActivity()).setCurrentBrushSize(brushWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateHexEditText() {
        int intColor = Color.HSVToColor(hsv);
        String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
        hexEditText.setText(hexColor);
    }

    private void updateSquareView() {
        squareView.setHue(hsv[0]);
        squareView.setSaturation(hsv[1]);
        squareView.setLightness(hsv[2]);
        squareView.invalidate();
    }

    private void updateIntent() {
        int intColor = Color.HSVToColor(hsv);
        ((ProjectEditorActivity)getActivity()).setCurrentColor(intColor);

//        Intent mIntent = new Intent();
//        mIntent.putExtra("color", intColor);
//        setResult(RESULT_OK, mIntent);
    }

    private float positiveMod(float num, float mod) {
        return (((num % mod) + mod) % mod);
    }
}
