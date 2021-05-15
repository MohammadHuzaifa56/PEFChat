package com.pefgloble.pefchate.jobs;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import static com.google.android.exoplayer2.upstream.cache.CacheDataSink.DEFAULT_BUFFER_SIZE;

public class ProgressRequestBody extends RequestBody {

    private File mFile;
    private UploadCallbacks mListener;

    public ProgressRequestBody(File mFile, UploadCallbacks mListener) {
        this.mFile = mFile;
        this.mListener = mListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                bufferedSink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        void onError();
        void onFinish();
    }
    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int)(100 * mUploaded / mTotal));
        }
    }

}
