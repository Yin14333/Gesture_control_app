package com.yin.gesturecontrolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.AbstractMap;
import java.util.HashMap;

public class GestureDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_demo);

        VideoView gestureDemoView = findViewById(R.id.gesture_demo_video);
        Intent intent = getIntent();
        String videoName = intent.getExtras().getString("videoName");
        String gestureName = intent.getExtras().getString("gestureName");

        int rawId = getResources().getIdentifier(videoName, "raw", getPackageName());
        String videoPath = "android.resource://" + getPackageName() + "/" + rawId;
        Uri uri = Uri.parse(videoPath);
        gestureDemoView.setVideoURI(uri);

        Button playDemo = (Button) findViewById(R.id.play_demo);
        playDemo.setOnClickListener(view -> gestureDemoView.start());

        Button practiceBtn = (Button) findViewById(R.id.practice_gestures);
        practiceBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(GestureDemoActivity.this, PracticeRecording.class);
            intent1.putExtra("gestureName", gestureName);
            startActivity(intent1);
        });
    }
}


