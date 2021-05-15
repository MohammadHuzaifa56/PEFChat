package com.pefgloble.pefchate.jobs;

import android.Manifest;
import android.content.Context;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.stories.StoryModel;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class UploadSingleStoryFileToServerWorker extends Worker {


    public static final String TAG = "UploadSingleStoryFileToServerWorker";


    private CompositeDisposable compositeDisposable;

    private String storyId;

    public UploadSingleStoryFileToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + TAG);

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            if (Permissions.hasAny(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                compositeDisposable = new CompositeDisposable();
                storyId = getInputData().getString("storyId");

                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                try {
                    if (PendingFilesTask.containsFile(storyId)) {
                        StoryModel storyModel = realm.where(StoryModel.class).equalTo("_id", storyId)
                                .equalTo("downloaded", true)
                                .equalTo("uploaded", false).findFirst();
                        if (storyModel != null) {
                            storyId = storyModel.get_id();
                            return uploadFile(storyModel);
                        } else {
                            return Result.failure();
                        }
                    } else {
                        return Result.failure();
                    }

                } finally {

                    if (!realm.isClosed())
                        realm.close();
                }


            } else {
                return Result.retry();
            }
        } else {
            return Result.failure();
        }

    }

    private void sendStartStatus(String type, String storyId) {

      /*  if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStart(type, storyId);
            }
        }*/
    }

    private void sendErrorStatus(String type, String storyId) {
        NotificationsManager.getInstance().cancelNotification(storyId);
     

            AppHelper.CustomToast(getApplicationContext(), getApplicationContext().getString(R.string.oops_something));


   /*     if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {

                listener.onError(type, storyId);

            }
        }*/
    }

    private void sendFinishStatus(String type, StoryModel storyModel) {
        NotificationsManager.getInstance().cancelNotification(storyModel.get_id());
        PendingFilesTask.removeFile(storyModel.get_id());
        WorkJobsManager.getInstance().sendUserStoriesToServer();
    }

    private Result uploadFile(StoryModel storyModel) {

        // If the job has been cancelled, stop working; the job will be rescheduled.
        if (isStopped())
            return Result.failure();


        String storyId = storyModel.get_id();
        if (storyModel.getType() != null && storyModel.getType().equals("image")) {

            sendStartStatus("image", storyModel.get_id());

            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                    storyModel.get_id(),
                    storyModel.getUserId());
            if (uploadImageFile(storyModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("image", storyId);
                }
                return Result.retry();
            }
        } else if (storyModel.getType() != null && storyModel.getType().equals("video")) {

            sendStartStatus("video", storyModel.get_id());

            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                    storyModel.get_id(),
                    storyModel.getUserId());
            if (uploadVideoFile(storyModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("video", storyId);
                }
                return Result.retry();
            }
        } else {
            return Result.failure();
        }
    }


    private boolean uploadVideoFile(String fileStory) {
        String type = "video";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadVideoFile(createMultipartBody(fileStory, "video/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed video " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());
                        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                            try {

                                realm.executeTransactionAsync(realm1 -> {
                                            StoryModel storyModel = realm1.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                                            storyModel.setUploaded(true);
                                            storyModel.setFile(filesResponse.getFilename());
                                            storyModel.setType("video");
                                            realm1.copyToRealmOrUpdate(storyModel);
                                        }, () -> {
                                            File file1 = new File(fileStory);
                                            file1.delete();

                                            StoryModel storyModel = realm.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                                            sendFinishStatus(type, storyModel);
                                            returnItem.set(true);
                                            latch.countDown();
                                            AppHelper.LogCat("finish realm video");
                                        }
                                        , error -> {
                                            AppHelper.LogCat("error realm video " + error.getMessage());
                                            sendErrorStatus(type, storyId);
                                            returnItem.set(false);
                                            latch.countDown();


                                        });

                            } finally {

                                if (!realm.isClosed())
                                    realm.close();

                            }

                        });
                    } else {
                        sendErrorStatus(type, storyId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, storyId);
                    AppHelper.LogCat("error  video" + throwable.getMessage());
                    returnException.set(throwable);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        try {
            if (returnException.get() != null) {
                Exceptions.propagate(returnException.get());
            }
        } catch (Exception e) {
            returnItem.set(false);
        }

        return returnItem.get();
    }


    private boolean uploadImageFile(String fileStory) {
        String type = "image";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);
        Disposable disposable = APIHelper.initializeUploadFiles().uploadImageFile(createMultipartBody(fileStory, "image/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed image " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());
                        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                        try {

                            realm.executeTransactionAsync(realm1 -> {
                                        StoryModel storyModel = realm1.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                                        storyModel.setUploaded(true);
                                        storyModel.setFile(filesResponse.getFilename());
                                        storyModel.setType("image");
                                        realm1.copyToRealmOrUpdate(storyModel);
                                    }, () -> {

                                        File file1 = new File(fileStory);
                                        file1.delete();

                                        StoryModel storyModel = realm.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                                        sendFinishStatus(type, storyModel);

                                        returnItem.set(true);
                                        latch.countDown();
                                        AppHelper.LogCat("finish realm image");
                                    }
                                    , error -> {
                                        AppHelper.LogCat("error realm image");
                                        sendErrorStatus(type, storyId);
                                        returnItem.set(false);
                                        latch.countDown();

                                    });

                        } finally {

                            if (!realm.isClosed())
                                realm.close();

                        }
                    } else {
                        sendErrorStatus(type, storyId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, storyId);
                    AppHelper.LogCat("error  image" + throwable.getMessage());
                    returnException.set(throwable);
                    latch.countDown();

                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        try {
            if (returnException.get() != null) {
                Exceptions.propagate(returnException.get());
            }
        } catch (Exception e) {
            returnItem.set(false);
        }
        return returnItem.get();
    }


    private MultipartBody.Part createMultipartBody(String filePath, String mimeType, String mType) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("file", file.getName(), createCountingRequestBody(file, mimeType, mType));
    }

    private RequestBody createRequestBody(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, String mType) {
        RequestBody requestBody = createRequestBody(file, mimeType);
        //  private CountDownLatch latch;
        return new UploadProgressRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (100 * bytesWritten) / contentLength;

            // AppHelper.LogCat("createCountingRequestBody " + (int) progress);

            //if (mWaitingListenerWeakReference != null) {
            //   UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (PendingFilesTask.containsFile(storyId) && !isStopped()) {
                // if (listener != null) {
                // listener.onUpdate((int) progress, mType, storyId);
                NotificationsManager.getInstance().updateUpDownNotification(getApplicationContext(), storyId, (int) progress);
                //   }
            }
            // }
        }, storyId);
    }

    @Override
    public void onStopped() {
        super.onStopped();
        AppHelper.LogCat("onStopJob: " + "onStopJob");
        if (isStopped())
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }

    }


}
