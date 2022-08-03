package com.yin.gesturecontrolapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private HashMap<String,String> gestureVideoMap = new HashMap(){{
        put("Turn On Light","hlighton");
        put("Turn Off Light", "hlightoff");
        put("Turn On Fan", "hfanon");
        put("Turn Off Fan", "hfanoff");
        put("Increase Fan Speed", "hincreasefanspeed");
        put("Decrease Fan Speed", "hdecreasefanspeed");
        put("Set Thermostat to specified temperature", "hsetthermo");
        put("0", "h0");
        put("1", "h1");
        put("2", "h2");
        put("3", "h3");
        put("4", "h4");
        put("5", "h5");
        put("6", "h6");
        put("7", "h7");
        put("8", "h8");
        put("9", "h9");
    }};

    private HashMap<String,String> gestureNameMap = new HashMap(){{
        put("Turn On Light","LightOn");
        put("Turn Off Light", "LightOff");
        put("Turn On Fan", "FanOn");
        put("Turn Off Fan", "FanOff");
        put("Increase Fan Speed", "FanUp");
        put("Decrease Fan Speed", "FanDown");
        put("Set Thermostat to specified temperature", "SetThermo");
        put("0", "Num0");
        put("1", "Num1");
        put("2", "Num2");
        put("3", "Num3");
        put("4", "Num4");
        put("5", "Num5");
        put("6", "Num6");
        put("7", "Num7");
        put("8", "Num8");
        put("9", "Num9");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinnerGestures = findViewById(R.id.spinner_gestures);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gesture_videos, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGestures.setPrompt("Please select a gesture");
        spinnerGestures.setAdapter(adapter);

        spinnerGestures.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                String videoName = gestureVideoMap.get(selection);
                String gestureName = gestureNameMap.get(selection);
                if (!selection.equals("Select Your Gesture")) {
                    Intent intent = new Intent(MainActivity.this, GestureDemoActivity.class);
                    intent.putExtra("videoName", videoName);
                    intent.putExtra("gestureName", gestureName);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

}