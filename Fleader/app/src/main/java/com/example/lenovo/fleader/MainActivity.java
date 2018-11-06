package com.example.lenovo.fleader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private ImageClassifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_LOAD_CMR = 2;
    String imgDecodableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            classifier = new ImageClassifier(MainActivity.this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.");
            Log.e(TAG,e.getMessage());
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void loadCamera(View view){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, RESULT_LOAD_CMR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imgView = (ImageView) findViewById(R.id.imgView);

        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
                getTensorFlowResult(BitmapFactory
                        .decodeFile(imgDecodableString));
            } else if(requestCode == RESULT_LOAD_CMR && resultCode == RESULT_OK
                    && null != data){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                imgView.setImageBitmap(image);
                getTensorFlowResult(BitmapFactory
                        .decodeFile(imgDecodableString));
            }else{
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            Log.w("ERROR FOTO",e.getMessage());
        }

    }

    public void getTensorFlowResult(Bitmap bitmap){
        TextView txtResult = (TextView) findViewById(R.id.textViewResult);
        bitmap = Bitmap.createScaledBitmap(bitmap, ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y, false);

        String results = classifier.classifyFrame(bitmap);
//        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

        txtResult.setText(results.toString());
    }


}