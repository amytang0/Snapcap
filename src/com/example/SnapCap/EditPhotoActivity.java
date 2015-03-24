package com.example.SnapCap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.Touch;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Photo editing screen
 */
public class EditPhotoActivity extends Activity {
    private static String TAG = "EditPhotoActivity";
    private Bitmap mBitmap;
    private ImageView mImageView;
    private View mCaption;
    private DragGestureListener mDragGestureListener;
    private GestureDetector mDragGestureDetector;

    private boolean onCaption = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entered Edit Photo Activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_photo);
        mImageView = (ImageView) findViewById(R.id.imageView1);
        mCaption = findViewById(R.id.caption);
        mDragGestureListener = new DragGestureListener();
        mDragGestureDetector = new GestureDetector(this, mDragGestureListener);
        mCaption.setVisibility(View.VISIBLE);
        mCaption.setClickable(false);
        mCaption.clearFocus();
        mCaption.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onCaption = true;
                // onTouchEvent(event);
                return false;
            }
        });
        mImageView.requestFocus();
        setCapturedPhotoInImageView();
    }

    private void setCapturedPhotoInImageView() {
        Intent intent = getIntent();
        byte[] data = intent.getByteArrayExtra(CameraActivity.CAPTURED_PHOTO);
        mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        mImageView.setImageBitmap(mBitmap);
    }

    public void savePhoto(View view) {
        // Draw caption on captured photo.
        FrameLayout savedImage = (FrameLayout)findViewById(R.id.frame);
        savedImage.setDrawingCacheEnabled(true);
        savedImage.buildDrawingCache();
        Bitmap bitmap = savedImage.getDrawingCache();

        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return;
        }
        try {
            Log.d(TAG, "Trying to save photo.");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Log.d(TAG, "Saved photo.");
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }

        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", "desc");
        Intent intent = new Intent(EditPhotoActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SnapCap");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }



    public boolean onTouchEvent(MotionEvent event) {
        return mDragGestureDetector.onTouchEvent(event);
    }

    protected class DragGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean onObject;

        @Override
        public boolean onDown(MotionEvent e) {
            mCaption.setVisibility(View.VISIBLE);
            onObject = onObject(e.getRawX(), e.getRawY());
            Log.d(TAG, "is on object? " + onObject);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "scrolling");
            if (onCaption) {
                FrameLayout.MarginLayoutParams mlp =
                        (FrameLayout.MarginLayoutParams) mCaption.getLayoutParams();
               // mlp.leftMargin -= (int) distanceX;
                mlp.topMargin -= (int) distanceY;
                mCaption.setLayoutParams(mlp);
                return true;
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onCaption = false;
            mImageView.requestFocus();
            return true;
        }

        private boolean onObject(float x, float y) {

            Rect rect = new Rect();
            mCaption.getGlobalVisibleRect(rect);

            Log.d(TAG, "x: " + (int)x+ " | y: "+(int)y + " rect"+rect.toShortString() );
            return rect.contains((int) x, (int) y);
        }
    }

}
