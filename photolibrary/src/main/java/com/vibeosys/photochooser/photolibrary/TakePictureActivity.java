package com.vibeosys.photochooser.photolibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TakePictureActivity extends AppCompatActivity implements View.OnTouchListener, ImageClickListener, OnCaptureClick {

    private SwipeRefreshLayout swipeContainer;
    public static final int REQUEST_ID = 2;
    public static final String FILE_PATH = "file.path";

    private CameraPreview mCameraPreview;
    private FrameLayout mLayoutPreview;
    private Camera mCamera;
    private RelativeLayout mGalleryLayout;
    private RelativeLayout mMainContentView;
    private ImageView mCaptureBtn;
    private GestureDetector mGestureDetector;
    private RecyclerView mRecyclerView;


    private ImagesAdapter mImagesAdapter;
    private ImageView mCancelBtn;
    private ImageView mNextBtn;
    private ImageView mBackBtn;
    private ImageView mArrowBtn;
    private Boolean isUp = false;
    private ArrayList<String> mImageList = new ArrayList<>();
    private ArrayList<String> mImageListBackUp = new ArrayList<>();
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private LoadingDialog mLoadingDialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        checkPermissions();

        //mRecyclerView.setOnTouchListener(this);

    }
    @SuppressLint("NewApi")
    private void initView()
    {
        isUp = false;
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        mArrowBtn = (ImageView) findViewById(R.id.arrowBtn);
        mBackBtn = (ImageView) findViewById(R.id.backBtn);


        mCaptureBtn = (ImageView) findViewById(R.id.captureBtn);
        mCancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        mNextBtn = (ImageView) findViewById(R.id.correctBtn);
        mLoadingDialog = new LoadingDialog(this);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));
        ArrayList<String> imageList = GetImageList.Companion.getAllImages(getContentResolver());
        mImageListBackUp.addAll(imageList);
        mImageList.addAll(imageList);
        mImagesAdapter = new ImagesAdapter(this, mImageList, this);
        mRecyclerView.setAdapter(mImagesAdapter);
        mMainContentView = (RelativeLayout) findViewById(R.id.mainContent);
        mLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mGalleryLayout = (RelativeLayout) findViewById(R.id.galleryLayout);


        mCameraPreview = new CameraPreview(this, mCamera, this);

        Point displayDim = getDisplayWH();
        Point layoutPreviewDim = calcCamPrevDimensions(displayDim,
                mCameraPreview.getOptimalPreviewSize(mCameraPreview.prSupportedPreviewSizes,
                        displayDim.x, displayDim.y));
        if (layoutPreviewDim != null) {
            RelativeLayout.LayoutParams layoutPreviewParams =
                    (RelativeLayout.LayoutParams) mLayoutPreview.getLayoutParams();
            layoutPreviewParams.width = layoutPreviewDim.x;
            layoutPreviewParams.height = layoutPreviewDim.y;
            mLayoutPreview.setLayoutParams(layoutPreviewParams);
        }
        mLayoutPreview.addView(mCameraPreview);

        mGestureDetector = new GestureDetector(this, new OnSwipeListener() {

            @Override
            public boolean onSwipe(Direction direction) {
                if (direction == Direction.up) {
                    //do your stuff
                    showLayout();
                }

                if (direction == Direction.down) {
                    //do your stuff
                    hideLayout();
                }
                return true;
            }


        });
        mMainContentView.setOnTouchListener(this);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        try {
            Field f = swipeContainer.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView) f.get(swipeContainer);
            img.setAlpha(0.0f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
                hideLayout();
            }
        });
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraPreview.captureImage();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isUp) {
                    hideLayout();
                } else {
                    showLayout();
                }
            }
        });
    }
    void showLayout() {
        isUp = true;
        releasePreview();
        mBackBtn.setVisibility(View.VISIBLE);
        mArrowBtn.setVisibility(View.GONE);
        mCaptureBtn.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(TakePictureActivity.this, 3));
        mImageList.clear();
        mImagesAdapter.setIsUp(true);
        AnimUtilClass.Companion.slideUp(mGalleryLayout, mMainContentView.getHeight());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageList.addAll(mImageListBackUp);
                mImagesAdapter.setIsUp(true);
                mGalleryLayout.setBackgroundColor(getResources().getColor(R.color.white));

            }
        },100);


    }


    private void checkPermissions()
    {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            else
            {
                initView();
            }
        } else {
            initView();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(this, "Cannot use this feature without requested permission", Toast.LENGTH_SHORT).show();
                } else {
                    initView();
                }
            }
        }
    }

    void hideLayout() {
        isUp = false;
        mBackBtn.setVisibility(View.GONE);
        mArrowBtn.setVisibility(View.VISIBLE);
        releasePreview();
        mCaptureBtn.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(TakePictureActivity.this, LinearLayoutManager.HORIZONTAL, false));
                mImagesAdapter.setIsUp(false);
            }
        }, 600);

        mGalleryLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        AnimUtilClass.Companion.slideDown(mGalleryLayout);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private Point getDisplayWH() {

        Display display = this.getWindowManager().getDefaultDisplay();
        Point displayWH = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(displayWH);
            return displayWH;
        }
        displayWH.set(display.getWidth(), display.getHeight());
        return displayWH;
    }

    private Point calcCamPrevDimensions(Point disDim, Camera.Size camDim) {

        Point displayDim = disDim;
        Camera.Size cameraDim = camDim;

        double widthRatio = (double) displayDim.x / cameraDim.width;
        double heightRatio = (double) displayDim.y / cameraDim.height;

        // use ">" to zoom preview full screen
        if (widthRatio > heightRatio) {
            Point calcDimensions = new Point();
            calcDimensions.x = displayDim.x;
            calcDimensions.y = (displayDim.x * cameraDim.height) / cameraDim.width;
            return calcDimensions;
        }
        // use "<" to zoom preview full screen
        if (widthRatio < heightRatio) {
            Point calcDimensions = new Point();
            calcDimensions.x = (displayDim.y * cameraDim.width) / cameraDim.height;
            calcDimensions.y = displayDim.y;
            return calcDimensions;
        }
        return null;
    }

    @Override
    public void onBackPressed() {

        if (isUp) {
            hideLayout();
        } else {
            finish();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    @Override
    public void imageClick(String filepath, View imageView) {

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, (View) imageView, "image");

        Intent intent = new Intent(TakePictureActivity.this, ShowImageActivity.class);
        intent.putExtra("myImage", filepath);
        startActivityForResult(intent, TakePictureActivity.REQUEST_ID, options.toBundle());

    }


    @Override
    public void captureClick(final byte[] byteArray) {

        mCaptureBtn.setVisibility(View.GONE);
        mCancelBtn.setVisibility(View.VISIBLE);
        mNextBtn.setVisibility(View.VISIBLE);

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              releasePreview();
            }
        });


        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoadingDialog.show();

                Bitmap orignalImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                Bitmap bitmapImage = mCameraPreview.rotate(orignalImage, 90);
                GetByteArrayAsync getByteArrayAsync = new GetByteArrayAsync();
                getByteArrayAsync.execute(bitmapImage);


            }
        });

    }

    private void releasePreview()
    {
        mCameraPreview.releasePreview();
        mCaptureBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.GONE);
        mNextBtn.setVisibility(View.GONE);
    }

    class GetByteArrayAsync extends AsyncTask<Bitmap, byte[], byte[]> {


        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {


            byte[] myByteArray = mCameraPreview.getByteArray(bitmaps[0]);

            return myByteArray;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            try {
                File file = createImageFile();
                FileOutputStream fos = new FileOutputStream(file.getPath());
                fos.write(bytes);
                fos.close();
                Intent intent = new Intent();
                intent.putExtra(TakePictureActivity.FILE_PATH, file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();
                mLoadingDialog.dismiss();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mCameraPreview!=null)
        {
            mCameraPreview.releasePreview();
            mCaptureBtn.setVisibility(View.VISIBLE);
            mCancelBtn.setVisibility(View.GONE);
            mNextBtn.setVisibility(View.GONE);
        }

    }

    private File createImageFile() throws IOException {


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == TakePictureActivity.REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
            String filePath = data.getStringExtra(TakePictureActivity.FILE_PATH);
            Intent intent = new Intent();
            intent.putExtra(TakePictureActivity.FILE_PATH, filePath);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }
}
