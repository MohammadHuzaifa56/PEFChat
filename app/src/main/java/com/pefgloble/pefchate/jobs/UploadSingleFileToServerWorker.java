package com.pefgloble.pefchate.jobs;

import android.Manifest;
import android.content.Context;
import android.util.Log;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.app.interfaces.UploadCallbacks;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
public class UploadSingleFileToServerWorker extends Worker implements UploadCallbacks {


    public static final String TAG = "UploadSingleFileToServerWorker";


    private CompositeDisposable compositeDisposable;
    private static WeakReference<UploadCallbacks> mWaitingListenerWeakReference;
    private String messageId;

    static void setUploadCallbacks(UploadCallbacks uploadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(uploadCallbacks);
    }

    static void updateUploadCallbacks(UploadCallbacks uploadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(uploadCallbacks);
    }

    public UploadSingleFileToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + TAG);

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            if (Permissions.hasAny(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                compositeDisposable = new CompositeDisposable();
                messageId = getInputData().getString("messageId");
                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                try {
                    if (PendingFilesTask.containsFile(messageId, getApplicationContext())) {
                        MessageModel messageModel = realm.where(MessageModel.class).equalTo("_id", messageId)
                                .equalTo("file_downLoad", true)
                                .equalTo("file_upload", false).findFirst();
                        if (messageModel != null) {
                            messageId = messageModel.get_id();
                            AppHelper.LogCat("messageId " + messageId);
                            try {
                                return uploadFile(messageModel);
                            } catch (Exception e) {
                                AppHelper.LogCat("Exception " + e.getMessage());
                                Log.d("FileEx","File Ex "+e.getMessage());
                                return Result.failure();
                            }

                        } else {
                            Log.d("FileEx","File Ex message null");
                            return Result.failure();
                        }
                    } else {
                        Log.d("FileEx","File Ex not contains");
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

    private void sendStartStatus(String type, String messageId) {

        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStart(type, messageId);
            }
        }
    }

    private void sendErrorStatus(String type, String messageId) {
        NotificationsManager.getInstance().cancelNotification(messageId);
        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {

                listener.onError(type, messageId);

            }
        }
    }

    private void sendFinishStatus(String type, MessageModel messageModel) {

        NotificationsManager.getInstance().cancelNotification(messageModel.get_id());
        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onFinish(type, messageModel);
            }
        }
    }

    private Result uploadFile(MessageModel messagesModel) {

        // If the job has been cancelled, stop working; the job will be rescheduled.
        if (isStopped())
            return Result.failure();


        String messageId = messagesModel.get_id();
        if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

            sendStartStatus("image", messagesModel.get_id());

            if (messagesModel.isIs_group()) {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getGroup().getName(),
                        messagesModel.get_id(),
                        messagesModel.getGroup().get_id(),
                        messagesModel.getConversationId());
            } else {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getRecipient().getUsername(),
                        messagesModel.getRecipient().getPhone(),
                        messagesModel.get_id(),
                        messagesModel.getRecipient().get_id(),
                        messagesModel.getConversationId());
            }
            if (uploadImageFile(messagesModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("image", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
            sendStartStatus("gif", messagesModel.get_id());
            if (messagesModel.isIs_group()) {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getGroup().getName(),
                        messagesModel.get_id(),
                        messagesModel.getGroup().get_id(),
                        messagesModel.getConversationId());
            } else {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getRecipient().getUsername(),
                        messagesModel.getRecipient().getPhone(),
                        messagesModel.get_id(),
                        messagesModel.getRecipient().get_id(),
                        messagesModel.getConversationId());
            }
            if (uploadGifFile(messagesModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("gif", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {

            sendStartStatus("video", messagesModel.get_id());
            if (messagesModel.isIs_group()) {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getGroup().getName(),
                        messagesModel.get_id(),
                        messagesModel.getGroup().get_id(),
                        messagesModel.getConversationId());
            } else {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getRecipient().getUsername(),
                        messagesModel.getRecipient().getPhone(),
                        messagesModel.get_id(),
                        messagesModel.getRecipient().get_id(),
                        messagesModel.getConversationId());
            }
            if (uploadVideoFile(messagesModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("video", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

            sendStartStatus("audio", messagesModel.get_id());
            if (messagesModel.isIs_group()) {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getGroup().getName(),
                        messagesModel.get_id(),
                        messagesModel.getGroup().get_id(),
                        messagesModel.getConversationId());
            } else {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getRecipient().getUsername(),
                        messagesModel.getRecipient().getPhone(),
                        messagesModel.get_id(),
                        messagesModel.getRecipient().get_id(),
                        messagesModel.getConversationId());
            }
            if (uploadAudioFile(messagesModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("audio", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
            sendStartStatus("document", messagesModel.get_id());
            if (messagesModel.isIs_group()) {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getGroup().getName(),
                        messagesModel.get_id(),
                        messagesModel.getGroup().get_id(),
                        messagesModel.getConversationId());
            } else {

                NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                        messagesModel.getRecipient().getUsername(),
                        messagesModel.getRecipient().getPhone(),
                        messagesModel.get_id(),
                        messagesModel.getRecipient().get_id(),
                        messagesModel.getConversationId());
            }
            if (uploadDocumentFile(messagesModel.getFile())) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("document", messageId);
                }
                return Result.retry();
            }
        } else {
            return Result.failure();
        }
    }


    private boolean uploadVideoFile(String fileMessage) {
        String type = "video";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadVideoFile(createMultipartBody(fileMessage, "video/*", type))
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
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {

                                realm.executeTransactionAsync(realm1 -> {
                                            MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            messagesModel1.setFile_upload(true);
                                            messagesModel1.setCreated(String.valueOf(new DateTime()));
                                            messagesModel1.setFile(filesResponse.getFilename());
                                            messagesModel1.setFile_type(AppConstants.MESSAGES_VIDEO);
                                            realm1.copyToRealmOrUpdate(messagesModel1);
                                        }, () -> {
                                            File file1 = new File(fileMessage);
                                            try {
                                                FilesManager.copyFile(file1, FilesManager.getFileVideoSent(getApplicationContext(), filesResponse.getFilename()));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            sendFinishStatus(type, messagesModel1);
                                            returnItem.set(true);
                                            latch.countDown();
                                            AppHelper.LogCat("finish realm video");
                                        }
                                        , error -> {
                                            AppHelper.LogCat("error realm video " + error.getMessage());
                                            sendErrorStatus(type, messageId);
                                            returnItem.set(false);
                                            latch.countDown();


                                        });

                            } finally {

                                if (!realm.isClosed())
                                    realm.close();

                            }

                        });
                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
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

    private boolean uploadGifFile(String fileMessage) {
        String type = "gif";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadGifFile(createMultipartBody(fileMessage, "image/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed gif " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());
                        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                            MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            messagesModel1.setFile_upload(true);
                                            messagesModel1.setCreated(String.valueOf(new DateTime()));
                                            messagesModel1.setFile(filesResponse.getFilename());
                                            messagesModel1.setFile_type(AppConstants.MESSAGES_GIF);
                                            realm1.copyToRealmOrUpdate(messagesModel1);
                                        }, () -> {
                                            File file1 = new File(fileMessage);
                                            try {
                                                FilesManager.copyFile(file1, FilesManager.getFileGifSent(getApplicationContext(), filesResponse.getFilename()));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }


                                            MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            sendFinishStatus(type, messagesModel1);
                                            returnItem.set(true);
                                            latch.countDown();
                                            AppHelper.LogCat("finish realm gif");
                                        }
                                        , error -> {

                                            returnItem.set(false);
                                            sendErrorStatus(type, messageId);
                                            latch.countDown();
                                            AppHelper.LogCat("error realm gif");
                                        });

                            } finally {

                                if (!realm.isClosed())
                                    realm.close();

                            }

                        });
                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  gif" + throwable.getMessage());
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

    private boolean uploadImageFile(String fileMessage) {
        String type = "image";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadImageFile(createMultipartBody(fileMessage, "image/*", type))
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
                                        MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                        messagesModel1.setFile_upload(true);
                                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                                        messagesModel1.setFile(filesResponse.getFilename());
                                        messagesModel1.setFile_type(AppConstants.MESSAGES_IMAGE);
                                        realm1.copyToRealmOrUpdate(messagesModel1);
                                    }, () -> {

                                        File file1 = new File(fileMessage);
                                        try {
                                            FilesManager.copyFile(file1, FilesManager.getFileImageSent(getApplicationContext(), filesResponse.getFilename()));
                                        } catch (IOException e) {
                                            //e.printStackTrace();
                                            AppHelper.LogCat("IOException " + e.getMessage());
                                        }

                                        MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                        sendFinishStatus(type, messagesModel1);

                                        returnItem.set(true);
                                        latch.countDown();
                                        AppHelper.LogCat("finish realm image");
                                    }
                                    , error -> {
                                        AppHelper.LogCat("error realm image");
                                        sendErrorStatus(type, messageId);
                                        returnItem.set(false);
                                        latch.countDown();

                                    });

                        } finally {

                            if (!realm.isClosed())
                                realm.close();

                        }
                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
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

    private boolean uploadAudioFile(String fileMessage) {
        String type = "audio";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadAudioFile(createMultipartBody(fileMessage, "audio/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed audio" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());
                        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {

                                realm.executeTransactionAsync(realm1 -> {
                                            MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            messagesModel1.setFile_upload(true);
                                            messagesModel1.setCreated(String.valueOf(new DateTime()));
                                            messagesModel1.setFile(filesResponse.getFilename());
                                            messagesModel1.setFile_type(AppConstants.MESSAGES_AUDIO);
                                            realm1.copyToRealmOrUpdate(messagesModel1);
                                        }, () -> {

                                            File file1 = new File(fileMessage);
                                            try {
                                                FilesManager.copyFile(file1, FilesManager.getFileAudioSent(getApplicationContext(), filesResponse.getFilename()));
                                                file1.delete();
                                            } catch (IOException e) {
                                                AppHelper.LogCat("Exception "+e.getMessage());
                                                e.printStackTrace();
                                            }


                                            MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            sendFinishStatus(type, messagesModel1);
                                            returnItem.set(true);
                                            latch.countDown();
                                            AppHelper.LogCat("finish realm audio");
                                        }
                                        , error -> {
                                            sendErrorStatus(type, messageId);
                                            returnItem.set(false);
                                            latch.countDown();
                                            AppHelper.LogCat("error realm audio");
                                        });


                            } finally {

                                if (!realm.isClosed())
                                    realm.close();

                            }

                        });
                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
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

    private boolean uploadDocumentFile(String fileMessage) {
        String type = "document";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadDocumentFile(createMultipartBody(fileMessage, "application/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());
                        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                            MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            messagesModel1.setFile_upload(true);
                                            messagesModel1.setCreated(String.valueOf(new DateTime()));
                                            messagesModel1.setFile(filesResponse.getFilename());
                                            messagesModel1.setFile_type(AppConstants.MESSAGES_DOCUMENT);
                                            realm1.copyToRealmOrUpdate(messagesModel1);
                                        }, () -> {

                                            File file1 = new File(fileMessage);
                                            try {
                                                FilesManager.copyFile(file1, FilesManager.getFileDocumentSent(getApplicationContext(), filesResponse.getFilename()));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                            sendFinishStatus(type, messagesModel1);
                                            //    checkCompletion(parameters, false);
                                            AppHelper.LogCat("finish realm document");
                                            returnItem.set(true);
                                            latch.countDown();
                                        }
                                        , error -> {
                                            sendErrorStatus(type, messageId);
                                            //  checkCompletion(parameters, true);
                                            AppHelper.LogCat("error realm document" + error.getMessage());
                                            returnItem.set(false);
                                            latch.countDown();
                                        });


                            } finally {

                                if (!realm.isClosed())
                                    realm.close();

                            }

                        });
                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
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

             AppHelper.LogCat("createCountingRequestBody " + (int) progress);

            if (mWaitingListenerWeakReference != null) {
                UploadCallbacks listener = mWaitingListenerWeakReference.get();
                    if (listener != null) {
                        listener.onUpdate((int) progress, mType, messageId);
                        NotificationsManager.getInstance().updateUpDownNotification(getApplicationContext(), messageId, (int) progress);
                    }
            }
        }, messageId);
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


    @Override
    public void onStart(String type, String messageId) {

    }

    @Override
    public void onUpdate(int percentage, String type, String messageId) {

    }

    @Override
    public void onError(String type, String messageId) {

    }

    @Override
    public void onFinish(String type, MessageModel messagesModel) {

    }
}
