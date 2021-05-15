package com.pefgloble.pefchate.helpers.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.TaskCallback;
import com.pefgloble.pefchate.helpers.files.FilesManager;

import java.io.File;
import java.io.FileOutputStream;


public final class ProcessingImage extends AsyncTask<Void, Void, String> {
    private final Bitmap srcBitmap;
    private final String imagePath;
    File fileName;
    private final TaskCallback<String> callback;

    public ProcessingImage(Bitmap srcBitmap, String imagePath,File fileName, TaskCallback<String> taskCallback) {
        this.srcBitmap = srcBitmap;
        this.callback = taskCallback;
        this.imagePath = imagePath;
        this.fileName=fileName;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return AppHelper.saveBitmap(srcBitmap, imagePath,fileName);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (callback != null) {
            callback.onTaskDone(s);
        }
    }
  /*  Context context;
    public ProcessingImage(Bitmap srcBitmap, String imagePath, Context context) {
        this.srcBitmap = srcBitmap;
        this.context=context;
        this.imagePath = imagePath;
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.d("My Tag","Do in Back Called");
        try {
            File outputFile = new File(context.getExternalCacheDir(),imagePath);
            //save the resized and compressed file to disk cache
            FileOutputStream bmpFile = new FileOutputStream(outputFile);
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile);

            bmpFile.flush();
            bmpFile.close();
            String path = FilesManager.getPath(context, FilesManager.getFile(outputFile,context));
            if (path == null) {
                path = FilesManager.copyDocumentToCache(FilesManager.getFile(outputFile), ".jpg",context);
            }
            return path;
        } catch (Exception e) {
             Log.d("MyTag",e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(context,"Image : "+s,Toast.LENGTH_SHORT).show();
        }*/
    }
