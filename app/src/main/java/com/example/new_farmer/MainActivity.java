package com.example.new_farmer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button captureImageButton, captureVideoButton, recordVoiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageButton = findViewById(R.id.captureImageButton);
        captureVideoButton = findViewById(R.id.captureVideoButton);
        recordVoiceButton = findViewById(R.id.recordVoiceButton);

        // Button click listeners
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to Capture Image Activity
                Intent intent = new Intent(MainActivity.this, image.class);
                startActivity(intent);
            }
        });

        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to Capture Video Activity
                Intent intent = new Intent(MainActivity.this, Video.class);
                startActivity(intent);
            }
        });

        recordVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to Record Voice Activity
                Intent intent = new Intent(MainActivity.this, Voice.class);
                startActivity(intent);
            }
        });
    }
}
