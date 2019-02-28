package com.arduk.animationcreator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class ColorSelectionActivity extends AppCompatActivity {

    private ColorSelectionSquareView squareView;
    private ColorSelectionDialView dialView;
    private ColorSelectionDialIndicatorView dialIndicatorView;
    private EditText hexEditText;

    private float[] hsv = {0.f, 0.f, 0.f};
    private float alpha = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection);

        squareView = findViewById(R.id.color_selection_square_view);
        squareView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setupSquare();

        dialView = findViewById(R.id.color_selection_dial_view);
        dialView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        dialIndicatorView = findViewById(R.id.color_selection_dial_indicator_view);
        setupDial();

        hexEditText = findViewById(R.id.hex_value_edit_text);
        setupEditText();

        Intent intent = getIntent();
        int mColor = intent.getIntExtra("color", 0xabcdef);
        Color.colorToHSV(mColor, hsv);

        dialIndicatorView.pointIndicator(hsv[0]);
        squareView.setSaturation(hsv[1]);
        squareView.setLightness(hsv[2]);
        updateSquareView();
        updateHexEditText();
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
        Intent mIntent = new Intent();
        mIntent.putExtra("color", intColor);
        setResult(RESULT_OK, mIntent);
    }

    private float positiveMod(float num, float mod) {
        return (((num % mod) + mod) % mod);
    }
}
