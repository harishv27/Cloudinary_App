package com.example.new_farmer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Voice extends AppCompatActivity {

    private Button startRecordingButton, stopRecordingButton, playButton, uploadButton;
    private TextView displayText;
    private MediaRecorder recorder;
    private String audioFilePath;
    private static final String CLOUDINARY_UPLOAD_PRESET = "preset-for-file-upload"; // Replace with your Cloudinary upload preset
    private static final String CLOUD_NAME = "db"; // Replace with your Cloudinary cloud name
    private pl.droidsonroids.gif.GifImageView recordingGif;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        // Initialize Cloudinary
        MediaManager.init(this, Map.of("cloud_name", CLOUD_NAME));

        // Initialize views
        startRecordingButton = findViewById(R.id.startRecordingButton);
        stopRecordingButton = findViewById(R.id.stopRecordingButton);
        playButton = findViewById(R.id.playButton);
        uploadButton = findViewById(R.id.uploadButton);
        displayText = findViewById(R.id.display);
        recordingGif = findViewById(R.id.recordingGif);

        // Set initial visibility of buttons and gif
        stopRecordingButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        uploadButton.setVisibility(View.GONE);
        recordingGif.setVisibility(View.GONE);

        // Set button click listeners
        startRecordingButton.setOnClickListener(v -> startRecording());
        stopRecordingButton.setOnClickListener(v -> stopRecording());
        playButton.setOnClickListener(v -> playAudio());
        uploadButton.setOnClickListener(v -> uploadAudio());

        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        } else {
            setupRecording();
        }
    }

    private void setupRecording() {
        // This will be called if the permissions are already granted
        startRecordingButton.setVisibility(View.VISIBLE);
    }

    private void startRecording() {
        try {
            // Show recording gif and hide the start button
            recordingGif.setVisibility(View.VISIBLE);
            startRecordingButton.setVisibility(View.GONE);
            stopRecordingButton.setVisibility(View.VISIBLE);

            // Create a new directory for recordings if not already created
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "MyRecordings");
            if (!dir.exists()) {
                boolean isCreated = dir.mkdirs(); // Create directory if it doesn't exist
                if (!isCreated) {
                    throw new IOException("Failed to create directory");
                }
            }

            // Set the audio file path
            audioFilePath = new File(dir, "recorded_audio.mp4").getAbsolutePath(); // Use .mp4 format

            // Setup MediaRecorder
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Use MPEG_4 for compatibility
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // Use AAC for higher quality
            recorder.setOutputFile(audioFilePath);

            // Prepare and start recording
            recorder.prepare();
            recorder.start();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Security error: Missing permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            // Stop and release the recorder
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }

            // Hide the recording gif and show Play, Upload buttons
            recordingGif.setVisibility(View.GONE);
            stopRecordingButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);

            // Display a message
            displayText.setText("Recording complete! You can play or upload your audio.");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error stopping recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio() {
        try {
            // Play the recorded audio
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Change the UI to indicate playback
            playButton.setVisibility(View.GONE);
            stopRecordingButton.setVisibility(View.VISIBLE);
            displayText.setText("Playing audio...");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAudio() {
        File audioFile = new File(audioFilePath);
        if (audioFile.exists()) {
            // Convert the File to a Uri
            Uri audioUri = Uri.fromFile(audioFile);

            // Upload the audio file to Cloudinary
            MediaManager.get().upload(audioUri)
                    .unsigned(CLOUDINARY_UPLOAD_PRESET)
                    .option("resource_type", "video")  // Use "video" for audio uploads
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // Start upload (optional: show a progress bar or message)
                            displayText.setText("Uploading audio...");
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Update progress if needed (optional)
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            // Get the uploaded audio URL
                            String audioUrl = (String) resultData.get("secure_url");
                            Toast.makeText(Voice.this, "Audio uploaded successfully!", Toast.LENGTH_SHORT).show();

                            // Display the uploaded audio URL in the TextView
                            displayText.setText("Audio uploaded successfully! URL: " + audioUrl);

                            // Optional: Provide a link or button for playback
                            playButton.setVisibility(View.VISIBLE);
                            playButton.setOnClickListener(v -> playAudioFromUrl(audioUrl));
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(Voice.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Handle rescheduling if needed
                        }
                    }).dispatch();
        } else {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void playAudioFromUrl(String url) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            displayText.setText("Playing uploaded audio...");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio from URL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean audioPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storagePermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (audioPermissionGranted && storagePermissionGranted) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    setupRecording();
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Release MediaPlayer resources when activity is stopped
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
