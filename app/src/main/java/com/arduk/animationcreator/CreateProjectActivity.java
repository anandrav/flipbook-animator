package com.arduk.animationcreator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class CreateProjectActivity extends AppCompatActivity {

    private EditText projectTitleEditText;
    private Spinner fpsSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        projectTitleEditText = (EditText)findViewById(R.id.title_of_project_edit_text);
        fpsSpinner = (Spinner)findViewById(R.id.fps_spinner);
        Integer[] items = new Integer[]{6,8,12,24};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, items);
        fpsSpinner.setAdapter(adapter);
        fpsSpinner.setSelection(3);
    }

    public void createProject(View view) {
        //create new project in internal storage given parameters
        String title = projectTitleEditText.getText().toString();

        int fps = Integer.parseInt(fpsSpinner.getSelectedItem().toString());


        int width = 900; //FIXME change back to 1920
        int height = 500; //FIXME change back to 1080

        Intent myIntent = new Intent(getApplicationContext(), ProjectEditorActivity.class);
        myIntent.putExtra("mode", ProjectEditorActivity.CREATE_NEW_PROJECT);
        myIntent.putExtra("title", title);
        myIntent.putExtra("fps", fps);
        myIntent.putExtra("width", width);
        myIntent.putExtra("height", height);
        startActivity(myIntent);
    }
}
