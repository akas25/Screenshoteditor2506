package com.osamendi.screenshoteditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    ImageView imagePreview;
    FrameLayout imageContainer;
    Button selectImageBtn, saveImageBtn;

    Bitmap workingBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        UpdateChecker.check(this);

        imagePreview = findViewById(R.id.imagePreview);
        imageContainer = findViewById(R.id.imageContainer);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        saveImageBtn = findViewById(R.id.saveImageBtn);

        selectImageBtn.setOnClickListener(v -> openGallery());
        saveImageBtn.setOnClickListener(v -> saveImage());
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            Uri imageUri = data.getData();

            try {

                workingBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                imagePreview.setImageBitmap(workingBitmap);

                detectText(workingBitmap);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void detectText(Bitmap bitmap) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(visionText -> {

                    Toast.makeText(this, "Text detected", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "OCR failed", Toast.LENGTH_SHORT).show();

                });
    }

    private void saveImage() {

        if (workingBitmap == null) return;

        Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
    }
}
