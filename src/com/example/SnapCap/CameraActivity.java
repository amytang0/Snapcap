package com.example.SnapCap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {

    public static String CAPTURED_PHOTO = "captured_photo";

    private Camera camera;
    private CameraPreview cameraPreview;
    private boolean inPreview = false;
    private boolean cameraConfigured = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        //initPreview(0,0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        camera.stopPreview();
        camera.release();
        super.onDestroy();
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                }
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        SurfaceHolder previewHolder = cameraPreview.getHolder();
        if (camera != null && previewHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height,
                        parameters);

                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }
        }
    }

    public void snapIt(View view){
        camera.takePicture(null, null, pictureCallback);
    }

    public static Camera getCameraInstance(){
        Camera object = null;
        try {
            object = Camera.open();
        } catch (Exception e){
            Log.d("CameraActivity", "Camera could not open");
        }
        return object;
    }

    private PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
            if (bitmap == null) {
                Toast.makeText(getApplicationContext(), "not taken", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();
               // imageView.setImageBitmap(bitmap);
                // Should open new activity
                Intent intent = new Intent(CameraActivity.this, EditPhotoActivity.class);
                intent.putExtra(CAPTURED_PHOTO, data);
                startActivity(intent);
            }
            finish();
        }
    };


}
