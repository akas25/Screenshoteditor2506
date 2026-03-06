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
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    ImageView imagePreview;
    FrameLayout imageContainer;
    Button selectImageBtn, saveImageBtn;

    Bitmap workingBitmap;

    ArrayList<TextBox> textBoxes = new ArrayList<>();

    class TextBox {

        Rect rect;
        String text;

        TextBox(Rect r, String t) {
            rect = r;
            text = t;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        textBoxes.clear();
        imageContainer.removeViews(1, imageContainer.getChildCount() - 1);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(visionText -> {

                    for (com.google.mlkit.vision.text.Text.TextBlock block : visionText.getTextBlocks()) {

                        Rect rect = block.getBoundingBox();
                        String text = block.getText();

                        textBoxes.add(new TextBox(rect, text));

                        addOverlay(rect, text);
                    }

                });
    }

    private void addOverlay(Rect rect, String text) {

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setBackgroundColor(Color.argb(120, 0, 255, 0));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(rect.width(), rect.height());
        params.leftMargin = rect.left;
        params.topMargin = rect.top;

        tv.setLayoutParams(params);

        tv.setOnClickListener(v -> editText(tv));

        imageContainer.addView(tv);
    }

    private void editText(TextView tv) {

        EditText editText = new EditText(this);
        editText.setText(tv.getText());

        new AlertDialog.Builder(this)
                .setTitle("Edit Text")
                .setView(editText)
                .setPositiveButton("Save", (d, w) -> {

                    tv.setText(editText.getText().toString());

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveImage() {

        if (workingBitmap == null) return;

        Bitmap newBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(newBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(40);

        for (int i = 1; i < imageContainer.getChildCount(); i++) {

            TextView tv = (TextView) imageContainer.getChildAt(i);

            canvas.drawText(tv.getText().toString(), tv.getLeft(), tv.getTop() + 40, paint);
        }

        try {

            Uri uri = MediaStore.Images.Media.insertImage(getContentResolver(), newBitmap, "edited", null) != null
                    ? Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), newBitmap, "edited", null))
                    : null;

            Toast.makeText(this, "Image Saved", Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
