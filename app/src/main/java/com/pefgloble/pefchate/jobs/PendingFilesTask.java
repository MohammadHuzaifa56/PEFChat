package com.pefgloble.pefchate.jobs;

import android.content.Context;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.app.interfaces.UploadCallbacks;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.interfaces.DownloadCallbacks;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Created by Abderrahim El imame on 10/16/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PendingFilesTask extends ResponseBody {


    public static void initUploadListener(String messageId) {
       PendingFilesTask.addFile(messageId);

    }

    public static void initUploadListener(String messageId, UploadCallbacks waitingListener) {
       PendingFilesTask.addFile(messageId, waitingListener);



    }

    public static void initDownloadListener(String messageId, DownloadCallbacks waitingListener) {
       PendingFilesTask.addFile(messageId, waitingListener);
    }


    public static void updateUploadListener(@Nullable UploadCallbacks uploadCallbacks) {
        UploadSingleFileToServerWorker.updateUploadCallbacks(uploadCallbacks);

    }

    public static void updateDownloadListener(@Nullable DownloadCallbacks downloadCallbacks) {
        DownloadSingleFileFromServerWorker.updateDownloadCallbacks(downloadCallbacks);
    }


    /**
     * add new file for download service
     *
     * @param messageId
     * @param waitingListener
     */
    private static void addFile(String messageId, DownloadCallbacks waitingListener) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(WhatsCloneApplication.getInstance());
        try {

            if (!containsFile(messageId)) {
                AppHelper.LogCat(" not containsFile");
                realm.executeTransactionAsync(realm1 -> {
                    UploadInfo uploadInfo = new UploadInfo();
                    uploadInfo.setUploadId(messageId);
                    realm1.copyToRealmOrUpdate(uploadInfo);
                }, () -> {
                    AppHelper.LogCat(" file added  ");
                    WorkJobsManager.getInstance().downloadFileToServer(messageId);
                    DownloadSingleFileFromServerWorker.setDownloadCallbacks(waitingListener);
                }, error -> {
                    AppHelper.LogCat("error add file " + error.getMessage());
                });

            } else {
                AppHelper.LogCat("  containsFile");
                WorkJobsManager.getInstance().downloadFileToServer(messageId);
                DownloadSingleFileFromServerWorker.setDownloadCallbacks(waitingListener);
            }
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }

    /**
     * add new file for upload service
     *
     * @param messageId
     * @param waitingListener
     */
    private static void addFile(String messageId, UploadCallbacks waitingListener) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            if (!containsFile(messageId)) {
                AppHelper.LogCat(" not containsFile");
                realm.executeTransactionAsync(realm1 -> {
                    UploadInfo uploadInfo = new UploadInfo();
                    uploadInfo.setUploadId(messageId);
                    realm1.copyToRealmOrUpdate(uploadInfo);
                }, () -> {
                    AppHelper.LogCat(" file added  ");
                    WorkJobsManager.getInstance().uploadFileToServer(messageId);
                    UploadSingleFileToServerWorker.setUploadCallbacks(waitingListener);
                }, error -> {
                    AppHelper.LogCat("error add file " + error.getMessage());
                });

            } else {
                AppHelper.LogCat("  containsFile");
                WorkJobsManager.getInstance().uploadFileToServer(messageId);
                UploadSingleFileToServerWorker.setUploadCallbacks(waitingListener);

            }
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    /**
     * add new file for upload service
     *
     * @param storyId
     */
    private static void addFile(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            if (!containsFile(storyId)) {
                AppHelper.LogCat(" not containsFile");
                realm.executeTransactionAsync(realm1 -> {
                    UploadInfo uploadInfo = new UploadInfo();
                    uploadInfo.setUploadId(storyId);
                    realm1.copyToRealmOrUpdate(uploadInfo);
                }, () -> {
                    AppHelper.LogCat(" file added  ");
                    WorkJobsManager.getInstance().uploadFileStoryToServer(storyId);

                }, error -> {
                    AppHelper.LogCat("error add file " + error.getMessage());
                });

            } else {
                AppHelper.LogCat("  containsFile");
                WorkJobsManager.getInstance().uploadFileStoryToServer(storyId);


            }
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }

    /**
     * method to remove file
     *
     * @param messageId this is the first parameter for removeFile  method
     * @param isFinish
     */
    public static void removeFile(String messageId, boolean isFinish, boolean isDownload) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(WhatsCloneApplication.getInstance());
        try {
            if (containsFile(messageId)) {

                realm.executeTransactionAsync(realm1 -> {
                    UploadInfo uploadInfo = realm1.where(UploadInfo.class).equalTo("uploadId", messageId).findFirst();
                    uploadInfo.deleteFromRealm();
                }, () -> {
                    if (!isFinish) {
                        if (isDownload) {
                            WorkJobsManager.getInstance().cancelJob(DownloadSingleFileFromServerWorker.TAG + "_" + messageId);
                        } else {
                            WorkJobsManager.getInstance().cancelJob(UploadSingleFileToServerWorker.TAG + "_" + messageId);
                        }
                    }
                }, error -> {
                    AppHelper.LogCat("error " + error.getMessage());
                });

            } else {

                if (!isFinish) {
                    if (isDownload) {
                        WorkJobsManager.getInstance().cancelJob(DownloadSingleFileFromServerWorker.TAG + "_" + messageId);
                    } else {
                        WorkJobsManager.getInstance().cancelJob(UploadSingleFileToServerWorker.TAG + "_" + messageId);
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public static void removeFile(String messageId) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            if (containsFile(messageId)) {

                realm.executeTransactionAsync(realm1 -> {
                    UploadInfo uploadInfo = realm1.where(UploadInfo.class).equalTo("uploadId", messageId).findFirst();
                    uploadInfo.deleteFromRealm();
                }, () -> {
                    WorkJobsManager.getInstance().cancelJob(UploadSingleStoryFileToServerWorker.TAG + "_" + messageId);
                }, error -> {
                    AppHelper.LogCat("error " + error.getMessage());
                });

            } else {

                WorkJobsManager.getInstance().cancelJob(UploadSingleStoryFileToServerWorker.TAG + "_" + messageId);
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public static boolean containsFile(String uploadId) {
//        AppHelper.LogCat("containsFile " + uploadId);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<UploadInfo> query = realm.where(UploadInfo.class).equalTo("uploadId", uploadId);
            count = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        return count != 0;
    }
    public static boolean containsFile(String uploadId,Context context) {
//        AppHelper.LogCat("containsFile " + uploadId);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(context);
        long count;
        try {
            RealmQuery<UploadInfo> query = realm.where(UploadInfo.class).equalTo("uploadId", uploadId);
            count = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        return count != 0;
    }

    /**
     * method to clear files
     */

    public static void clearFiles(Context mContext) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(WhatsCloneApplication.getInstance());
        try {
            realm.executeTransactionAsync(realm1 -> {

                RealmResults<UploadInfo> uploadInfo = realm1.where(UploadInfo.class).findAll();
                uploadInfo.deleteAllFromRealm();
            });
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public long contentLength() {
        return 0;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public MediaType contentType() {
        return null;
    }

    @NotNull
    @Override
    public BufferedSource source() {
        return null;
    }
}
