package com.example.new_farmer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;
import java.util.Map;

public class image extends AppCompatActivity {

    private String cloudName = "db"; // Replace with your Cloudinary cloud name
    private String uploadPreset = "preset-for-file-upload"; // Replace with your Cloudinary upload preset
    private ImageView selectedImageView;
    private ImageView uploadedImageView;
    private Button selectButton;
    private Button uploadButton;
    private Uri imageUri;
    private Bitmap bitmap;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Initialize views
        selectedImageView = findViewById(R.id.selectedImageView);
        uploadedImageView = findViewById(R.id.uploadedImageView);
        selectButton = findViewById(R.id.selectButton);
        uploadButton = findViewById(R.id.uploadButton);

        // Initialize ActivityResultLaunchers
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Get image from camera
                        bitmap = (Bitmap) result.getData().getExtras().get("data");
                        selectedImageView.setImageBitmap(bitmap); // Set the image to the selectedImageView
                        imageUri = getImageUriFromBitmap(bitmap); // Set image URI for upload
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Get image from gallery
                        Uri uri = result.getData().getData();
                        imageUri = uri; // Set the image URI for upload
                        Glide.with(image.this)
                                .load(uri)
                                .into(selectedImageView); // Load the image into the selectedImageView
                    }
                });

        // Initialize Cloudinary
        initCloudinary();

        // Set OnClickListener for select button
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Let the user pick an image from the gallery or use the camera
                showImageSourceDialog();
            }
        });

        // Set OnClickListener for upload button
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    // Upload the selected image if one is selected
                    uploadImageToCloudinary(imageUri);
                } else {
                    Toast.makeText(image.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Initialize Cloudinary
    private void initCloudinary() {
        // Cloudinary configuration
        MediaManager.init(this, Map.of("cloud_name", cloudName));
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        // Open Camera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(cameraIntent);
                    } else {
                        // Open Gallery
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(galleryIntent);
                    }
                }).create().show();
    }

    // Helper method to get image URI from Bitmap (for camera images)
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Camera Image", null);
        return Uri.parse(path);
    }

    // Upload the selected image to Cloudinary
    private void uploadImageToCloudinary(Uri imageUri) {
        if (imageUri != null) {
            // Upload the image to Cloudinary
            MediaManager.get().upload(imageUri).unsigned(uploadPreset).callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Log start of upload
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    // You can show upload progress here if needed
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    // Get the uploaded image URL
                    String imageUrl = (String) resultData.get("secure_url");

                    // Load the uploaded image into uploadedImageView using Glide
                    Glide.with(image.this)
                            .load(imageUrl)
                            .into(uploadedImageView);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    // Handle error if upload fails
                    Toast.makeText(image.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Handle rescheduling of upload if required
                }
            }).dispatch();
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
