package com.pefgloble.pefchate.jobs;

import android.Manifest;
import android.content.Context;


import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SyncingContactsWithServerWorker extends Worker {

    public static final String TAG = SyncingContactsWithServerWorker.class.getSimpleName();
    private CompositeDisposable mDisposable;
    private CountDownLatch latch;
    private int mPendingTasks = 0;

    public SyncingContactsWithServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        mDisposable = new CompositeDisposable();
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            syncingContacts();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Result.success();

        } else {
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();

        if (mDisposable != null)
            mDisposable.dispose();

        boolean needsReschedule = (mPendingTasks > 0);
        AppHelper.LogCat("Job stopped. Needs reschedule: " + needsReschedule);
        if (!needsReschedule) {
            WorkManager.getInstance().cancelAllWorkByTag(TAG);
            mPendingTasks = 0;
        }
    }

    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending messages to send.
     */
    private void checkCompletion() {


        //  if any sending is not successful, reschedule job for remaining files
        boolean needsReschedule = (mPendingTasks > 0);
        AppHelper.LogCat("Job finished. Pending files: " + mPendingTasks);
        if (!needsReschedule)
            WorkManager.getInstance().cancelAllWorkByTag(TAG);


    }

    private void syncingContacts() {
        AppHelper.LogCat("completeJob: " + "jobStarted");

        if (Permissions.hasAny(getApplicationContext(), Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {

            latch = new CountDownLatch(1);
            mPendingTasks = 1;
            mDisposable.addAll(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {

                try {
                    List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                    subscriber.onNext(contactsModels);
                    subscriber.onComplete();
                } catch (Exception throwable) {
                    subscriber.onError(throwable);
                }
            }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
                AppHelper.LogCat("completeJob: " + "jobFinished");
                AppHelper.LogCat("  size contact ScheduledJobService " + contacts.size());
                //Tell the framework that the job has completed and  needs to be reschedule
                APIHelper.initialApiUsersContacts().updateContacts(contacts).subscribe(contactsModelList -> {
                    //Tell the framework that the job has completed and doesnot needs to be reschedule


                    mPendingTasks--;
                    checkCompletion();
                    latch.countDown();
                }, throwable -> {

                    //Tell the framework that the job has completed and  needs to be reschedule
                    checkCompletion();
                }, this::checkCompletion);

            }, throwable -> {
                AppHelper.LogCat("completeJob: " + "jobFinished");
                //Tell the framework that the job has completed and  needs to be reschedule

                checkCompletion();
                AppHelper.LogCat(" " + throwable.getMessage());
            }));
        }

    }
}