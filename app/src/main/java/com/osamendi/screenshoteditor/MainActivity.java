package com.osamendi.screenshoteditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    ImageView imagePreview;
    TextView resultText;
    Button selectImageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);
        resultText = findViewById(R.id.resultText);
        selectImageBtn = findViewById(R.id.selectImageBtn);

        selectImageBtn.setOnClickListener(v -> openGallery());
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

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                imagePreview.setImageBitmap(bitmap);

                detectText(bitmap);

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

                    StringBuilder result = new StringBuilder();

                    for (com.google.mlkit.vision.text.Text.TextBlock block : visionText.getTextBlocks()) {
                        result.append(block.getText());
                        result.append("\n\n");
                    }

                    resultText.setText(result.toString());

                })
                .addOnFailureListener(e -> resultText.setText("OCR Failed"));
    }
}
