package com.pefgloble.pefchate.helpers.filter;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.pefgloble.pefchate.JsonClasses.otherClasses.ThumbnailFilter;
import com.pefgloble.pefchate.helpers.TaskCallback;


public final class ApplyFilterTask extends AsyncTask<ThumbnailFilter, Void, Bitmap> {
    private final TaskCallback<Bitmap> listenerRef;
    private Bitmap srcBitmap;

    public ApplyFilterTask(TaskCallback<Bitmap> taskCallbackWeakReference, Bitmap srcBitmap) {
        this.srcBitmap = srcBitmap;
        this.listenerRef = taskCallbackWeakReference;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (listenerRef != null) {
            listenerRef.onTaskDone(result);
        }
    }

    @Override
    protected Bitmap doInBackground(ThumbnailFilter... thumbnailItems) {
        if (thumbnailItems != null && thumbnailItems.length > 0) {
            return thumbnailItems[0].filter.processFilter(srcBitmap);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}