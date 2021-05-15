package com.pefgloble.pefchate.jobs;

import android.Manifest;
import android.content.Context;


import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.interfaces.DownloadCallbacks;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.ResponseBody;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class DownloadSingleFileFromServerWorker extends Worker {


    public static final String TAG = "DownloadSingleFileFromServerWorker";


    private CompositeDisposable compositeDisposable;
    private static WeakReference<DownloadCallbacks> mWaitingListenerWeakReference;
    private String messageId;

    static void setDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(downloadCallbacks);
    }

    static void updateDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(downloadCallbacks);
    }

    public DownloadSingleFileFromServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
                    if (PendingFilesTask.containsFile(messageId)) {
                        MessageModel messageModel = realm.where(MessageModel.class).equalTo("_id", messageId)
                                .equalTo("file_downLoad", false)
                                .equalTo("file_upload", true).findFirst();
                        if (messageModel != null) {
                            messageId = messageModel.get_id();
                            return downloadFile(messageModel);
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

    private void sendStartStatus(String type, String messageId) {

        if (mWaitingListenerWeakReference != null) {
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStartDownload(type, messageId);
            }
        }
    }

    private void sendErrorStatus(String type, String messageId) {
        if (mWaitingListenerWeakReference != null) {
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onErrorDownload(type, messageId);

            }
        }
    }

    private void sendFinishStatus(String type, MessageModel messageModel) {

        NotificationsManager.getInstance().cancelNotification(messageModel.get_id());
        if (mWaitingListenerWeakReference != null) {
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onFinishDownload(type, messageModel);
            }
        }
    }

    private Result downloadFile(MessageModel messagesModel) {

        // If the job has been cancelled, stop working; the job will be rescheduled.
        if (isStopped())
            return Result.failure();


        String messageId = messagesModel.get_id();
        if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

            sendStartStatus("image", messagesModel.get_id());

            if (downloadImageFile(messagesModel)) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("image", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
            sendStartStatus("gif", messagesModel.get_id());

            if (downloadGifFile(messagesModel)) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("gif", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {

            sendStartStatus("video", messagesModel.get_id());

            if (downloadVideoFile(messagesModel)) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("video", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

            sendStartStatus("audio", messagesModel.get_id());

            if (downloadAudioFile(messagesModel)) {
                return Result.success();
            } else {
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("audio", messageId);
                }
                return Result.retry();
            }
        } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
            sendStartStatus("document", messagesModel.get_id());

            if (downloadDocumentFile(messagesModel)) {
                return Result.success();
            } else {
                AppHelper.LogCat("downloadDocumentFile " + compositeDisposable.isDisposed());
                if (compositeDisposable.isDisposed()) {
                    sendErrorStatus("document", messageId);
                }
                return Result.retry();
            }
        } else {
            return Result.failure();
        }
    }


    private boolean downloadVideoFile(MessageModel messageModel) {
        String type = "video";

        String fileUrl = EndPoints.MESSAGE_VIDEO_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            com.pefgloble.pefchate.jobs.DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {
                        AppHelper.runOnUIThread(() -> {

                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    messagesModel1.setFile_downLoad(true);
                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                }, () -> {
                                    MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    sendFinishStatus(type, messagesModel1);
                                    returnItem.set(true);
                                    latch.countDown();
                                }, error -> {
                                    sendErrorStatus(type, messageId);
                                    returnItem.set(false);
                                    latch.countDown();
                                });
                            } finally {
                                if (!realm.isClosed()) realm.close();
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
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    private boolean downloadGifFile(MessageModel messageModel) {
        String type = "gif";

        String fileUrl = EndPoints.MESSAGE_GIF_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {

                        AppHelper.runOnUIThread(() -> {

                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    messagesModel1.setFile_downLoad(true);
                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                }, () -> {
                                    MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    sendFinishStatus(type, messagesModel1);
                                    returnItem.set(true);
                                    latch.countDown();
                                }, error -> {
                                    sendErrorStatus(type, messageId);
                                    returnItem.set(false);
                                    latch.countDown();
                                });
                            } finally {
                                if (!realm.isClosed()) realm.close();
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
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    private boolean downloadImageFile(MessageModel messageModel) {
        String type = "image";

        String fileUrl = EndPoints.MESSAGE_IMAGE_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            com.pefgloble.pefchate.jobs.DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {
                        AppHelper.runOnUIThread(() -> {

                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    messagesModel1.setFile_downLoad(true);
                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                }, () -> {

                                    MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    sendFinishStatus(type, messagesModel1);
                                    returnItem.set(true);
                                    latch.countDown();
                                }, error -> {
                                    sendErrorStatus(type, messageId);
                                    returnItem.set(false);
                                    latch.countDown();
                                });
                            } finally {
                                if (!realm.isClosed()) realm.close();
                            }
                        });

                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }
                }, throwable -> {

                    AppHelper.LogCat("error  image" + throwable.getMessage());
                    sendErrorStatus(type, messageId);
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        return returnItem.get();
    }

    private boolean downloadAudioFile(MessageModel messageModel) {
        String type = "audio";
        String fileUrl = EndPoints.MESSAGE_AUDIO_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            com.pefgloble.pefchate.jobs.DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {
                        AppHelper.runOnUIThread(() -> {

                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    messagesModel1.setFile_downLoad(true);
                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                }, () -> {


                                    MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    sendFinishStatus(type, messagesModel1);
                                    returnItem.set(true);
                                    latch.countDown();
                                }, error -> {
                                    sendErrorStatus(type, messageId);
                                    returnItem.set(false);
                                    latch.countDown();
                                });
                            } finally {
                                if (!realm.isClosed()) realm.close();
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
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }


    private boolean downloadDocumentFile(MessageModel messageModel) {

        String type = "document";
        String fileUrl = EndPoints.MESSAGE_DOCUMENT_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            com.pefgloble.pefchate.jobs.DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {
                        AppHelper.LogCat("server contacted and has file ");
                        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationModel messagesModel1 = realm1.where(ConversationModel.class).equalTo("_id", messageId).findFirst();
                                    messagesModel1.getLatestMessage().setFile_downLoad(true);
                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                }, () -> {
                                    MessageModel messagesModel1 = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                                    sendFinishStatus(type, messagesModel1);
                                    returnItem.set(true);
                                    latch.countDown();
                                }, error -> {
                                    sendErrorStatus(type, messageId);
                                    returnItem.set(false);
                                    latch.countDown();
                                });
                            } finally {
                                if (!realm.isClosed()) realm.close();
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
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        AppHelper.LogCat("onStopJob: " + "onStopJob " + isStopped());
        if (isStopped())
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }
    }


}
