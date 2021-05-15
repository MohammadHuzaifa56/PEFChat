package com.pefgloble.pefchate.HomeScreens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pefgloble.pefchate.JsonClasses.otherClasses.MediaPicker;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.media.ImageEditActivity;
import com.pefgloble.pefchate.activities.media.PickerActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ShowCamera;
import com.pefgloble.pefchate.helpers.files.FilesManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.pefgloble.pefchate.helpers.files.FilesManager.getCacheDir;


public class CameraPage extends Fragment {
    Camera camera;
    FrameLayout frameLayout;
    Button btnCapture, btnSelect;
    SavePicTask savePicTask;
    File folder=null;
    ShowCamera showCamera;
    private Set<MediaPicker> selectionList = new HashSet<>();
    String path;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.camera_page, container, false);


        frameLayout = view.findViewById(R.id.camFrame);
        btnCapture=view.findViewById(R.id.btnCapture);

        folder = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        {
            try {
                camera = android.hardware.Camera.open();
                showCamera = new ShowCamera(getContext(), camera);
                frameLayout.addView(showCamera);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 101);
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(camera!=null) {
                        camera.takePicture(null, null, imgCallBack);
                    }
            }
        });


        return view;
    }

    Camera.PictureCallback imgCallBack=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            cancelSavePicTaskIfNeed();
            savePicTask = new SavePicTask(bytes, getPhotoRotation());
            savePicTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;
        private int rotation = 0;

        public SavePicTask(byte[] data, int rotation) {
            this.data = data;
            this.rotation = rotation;
        }

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                return saveToSDCard(data, rotation);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //activeCameraCapture();

            File tempFile = new File(result);

            new Handler().postDelayed(() -> {
                selectionList.clear();
                selectionList.add(new MediaPicker("", "", tempFile.getAbsolutePath(), "", "image"));
                getResults();

            }, 100);


        }
    }
    private void cancelSavePicTaskIfNeed() {
        if (savePicTask != null && savePicTask.getStatus() == AsyncTask.Status.RUNNING) {
            savePicTask.cancel(true);
        }
    }
    public String saveToSDCard(byte[] data, int rotation) throws IOException {
        String imagePath = "";
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int reqHeight = metrics.heightPixels;
            int reqWidth = metrics.widthPixels;

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (rotation != 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                if (bitmap != bitmap1) {
                    bitmap.recycle();
                }
                imagePath = getSavePhotoLocal(bitmap1);
                if (bitmap1 != null) {
                    bitmap1.recycle();
                }
            } else {
                imagePath = getSavePhotoLocal(bitmap);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Save Img Exception "+e.getMessage());
            e.printStackTrace();
        }
        return imagePath;
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
    private String getSavePhotoLocal(Bitmap bitmap) {
        String path = "";
        try {
            OutputStream output;
            File file = new File(folder.getAbsolutePath(), "IMG_" + System.currentTimeMillis() + ".jpg");
            try {
                output = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                path = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
    public void getResults() {
        List<String> list = new ArrayList<>();
        for (MediaPicker i : selectionList) {
            list.add(i.getUrl());
            // Log.e("PickerActivity images", "img " + i.getUrl());
        }

        String mimeType = FilesManager.getMimeType(getContext(), Uri.parse(list.get(0)));
        if (FilesManager.isVideo(mimeType)) {
            /*Intent intent = new Intent(this, VideoEditorActivity.class);
            intent.putExtra(AppConstants.MediaConstants.EXTRA_VIDEO_PATH, list.get(0));
            intent.putExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, forStory);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);*/
        } else if (FilesManager.isImageType(mimeType)) {

            Intent intent = new Intent(getContext(), ImageEditActivity.class);
            intent.putExtra(AppConstants.MediaConstants.EXTRA_IMAGE_PATH, list.get(0));
            intent.putExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
        }

    }
    private int getPhotoRotation() {
        int rotation;
        int orientation = 90;

        Camera.CameraInfo info = new Camera.CameraInfo();

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }
    private void inActiveCameraCapture() {
        if (btnCapture != null) {
            btnCapture.setAlpha(0.5f);
            btnCapture.setOnClickListener(null);
        }
    }
}