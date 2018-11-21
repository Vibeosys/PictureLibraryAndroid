package com.vibeosys.photochooser.photolibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder prHolder;
    private Camera prCamera;
    public List<Camera.Size> prSupportedPreviewSizes;
    private Camera.Size prPreviewSize;
    private Context mContext;
    private OnCaptureClick myCaptureClick;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera, OnCaptureClick onCaptureClick) {
        super(context);
        prCamera = camera;
        mContext = context;

        prSupportedPreviewSizes = prCamera.getParameters().getSupportedPreviewSizes();
        myCaptureClick = onCaptureClick;
        prHolder = getHolder();
        prHolder.addCallback(this);
        prHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (prCamera == null) {
                prCamera = Camera.open();
                prCamera.setDisplayOrientation(90);
            }
            prCamera.setPreviewDisplay(holder);
            prCamera.startPreview();
        } catch (IOException e) {
            Log.d("Yologram", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        prCamera.stopPreview();
        prCamera.release();
        prCamera = null;

    }

    protected void releasePreview() {
        if (prCamera != null)
            prCamera.startPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (prHolder.getSurface() == null) {
            return;
        }

        try {
            prCamera.stopPreview();
        } catch (Exception e) {
        }

        try {
            Camera.Parameters parameters = prCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            parameters.setPreviewSize(prPreviewSize.width, prPreviewSize.height);

            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            Camera.Size size = sizes.get(0);
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }
            parameters.setPictureSize(size.width, size.height);

            prCamera.getParameters().setRotation(90);
            prCamera.setParameters(parameters);
            prCamera.setPreviewDisplay(prHolder);
            prCamera.startPreview();


        } catch (Exception e) {
            Log.d("Yologram", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);

        if (prSupportedPreviewSizes != null) {
            prPreviewSize =
                    getOptimalPreviewSize(prSupportedPreviewSizes, width, height);
        }
    }

    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    protected void captureImage() {

        if (prCamera != null) {
            prCamera.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);
        }

    }


    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {

        public void onShutter() {
        }
    };

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] byteArray, Camera arg1) {


        }
    };

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] byteArray, Camera arg1) {
            int angleToRotate = getRoatationAngle(Camera.CameraInfo.CAMERA_FACING_FRONT);
            // Solve image inverting problem
            angleToRotate = angleToRotate + 180;

            myCaptureClick.captureClick(byteArray);


        }
    };

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    protected byte[] getByteArray(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public int getRoatationAngle(int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


}