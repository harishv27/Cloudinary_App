package com.example.new_farmer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Video extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 101;
    private Button startRecordingButton, uploadButton;
    private TextView displayText;
    private String videoFilePath;
    private static final String CLOUDINARY_UPLOAD_PRESET = "preset-for-file-upload"; // Replace with your Cloudinary upload preset
    private static final String CLOUD_NAME = "db"; // Replace with your Cloudinary cloud name
    private pl.droidsonroids.gif.GifImageView recordingGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Initialize Cloudinary
        MediaManager.init(this, Map.of("cloud_name", CLOUD_NAME));

        // Initialize views
        startRecordingButton = findViewById(R.id.startVideoRecordingButton);
        uploadButton = findViewById(R.id.uploadVideoButton);
        displayText = findViewById(R.id.displayVideoText);
        recordingGif = findViewById(R.id.recordingVideoGif);

        // Set initial visibility of buttons and gif
        uploadButton.setVisibility(View.GONE);
        recordingGif.setVisibility(View.GONE);

        // Set button click listeners
        startRecordingButton.setOnClickListener(v -> startRecording());
        uploadButton.setOnClickListener(v -> uploadVideo());

        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
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
        // Open camera intent to capture video
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void uploadVideo() {
        File videoFile = new File(videoFilePath);
        if (videoFile.exists()) {
            // Upload the video file to Cloudinary
            MediaManager.get().upload(Uri.fromFile(videoFile))
                    .unsigned(CLOUDINARY_UPLOAD_PRESET)
                    .option("resource_type", "video")  // Use "video" for video uploads
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // Start upload (optional: show a progress bar or message)
                            displayText.setText("Uploading video...");
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Update progress if needed (optional)
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            // Get the uploaded video URL
                            String videoUrl = (String) resultData.get("secure_url");
                            Toast.makeText(Video.this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();

                            // Display the uploaded video URL in the TextView
                            displayText.setText("Video uploaded successfully! URL: " + videoUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(Video.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Handle rescheduling if needed
                        }
                    }).dispatch();
        } else {
            Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                videoFilePath = getFilePathFromUri(videoUri);
                if (videoFilePath != null) {
                    displayText.setText("Video ready! You can upload now.");
                    uploadButton.setVisibility(View.VISIBLE);
                } else {
                    displayText.setText("Failed to get video file path.");
                }
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            // Get the real file path from content URI
            String[] projection = {MediaStore.Video.Media.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    filePath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e("VideoActivity", "Failed to get file path: " + e.getMessage());
            }
        } else if (uri.getScheme().equals("file")) {
            // If URI is a file path (directly from storage)
            filePath = uri.getPath();
        }
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storagePermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (cameraPermissionGranted && storagePermissionGranted) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    setupRecording();
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
