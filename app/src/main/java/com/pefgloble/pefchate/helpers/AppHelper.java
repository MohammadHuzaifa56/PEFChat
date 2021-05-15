package com.pefgloble.pefchate.helpers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.media.ImagePreviewActivity;
import com.pefgloble.pefchate.activities.media.VideoPlayerActivity;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.jobs.GcmTopicSubscribe;
import com.pefgloble.pefchate.util.Util;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppHelper {

    private static ProgressDialog mDialog;
    private static Dialog dialog;
    public static float density = 1;
    public static ProgressDialog progressDialog;

    public static Point displaySize = new Point();
    public static int statusBarHeight = 0;

    public static boolean usingHardwareInput;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();

    /*static {
        density = WhatsCloneApplication.getInstance().getResources().getDisplayMetrics().density;
        checkDisplaySize();
    }*/

    /**
     * method to show the progress dialog
     *
     * @param mContext this is parameter for showDialog method
     */
    public static void showDialog(Context mContext, String message) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(message);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(true);
        mDialog.show();
    }

    /**
     * method to show the progress dialog
     *
     * @param mContext this is parameter for showDialog method
     */
    public static void showDialog(Context mContext, String message, boolean cancelable) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(message);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    /**
     * method to hide the progress dialog
     */
    public static void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    public static String getCurrentTime() {
        DateTime current = new DateTime();
        return String.valueOf(current);
    }
    public static String getAppVersion(Context mContext) {
        PackageInfo packageinfo;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AppHelper.LogCat(" getAppVersion NameNotFoundException " + e.getMessage());
            return null;
        }
    }
    public static void ShowProgressDialog(final Activity activity, final String message) {
        activity.runOnUiThread(() -> {
            if (!activity.isFinishing()) {
                progressDialog = new ProgressDialog(activity);
                if (message != null) {
                    progressDialog.setMessage(message);
                }
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        });
    }

    public static void HideProgressDialog(Activity activity) {
        activity.runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        });
    }
    @SuppressLint("RestrictedApi")
    public static Drawable getVectorDrawable(Context mContext, @DrawableRes int id) {
        return AppCompatDrawableManager.get().getDrawable(mContext, id);
    }


    /**
     * method for get a custom CustomToast
     *
     * @param Message this is the second parameter for CustomToast  method
     */
    public static void CustomToast(Context mContext, String Message) {


             AppHelper.runOnUIThread(() -> {

            LinearLayout CustomToastLayout = new LinearLayout(mContext.getApplicationContext());
            CustomToastLayout.setBackgroundResource(R.drawable.bg_custom_toast);
            CustomToastLayout.setGravity(Gravity.TOP);
            TextView message = new TextView(mContext.getApplicationContext());
            message.setTextColor(Color.WHITE);
            message.setTextSize(13);
            message.setPadding(20, 20, 20, 20);
            message.setGravity(Gravity.CENTER);
            message.setText(Message);
            CustomToastLayout.addView(message);
            Toast toast = new Toast(mContext.getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(CustomToastLayout);
            toast.setGravity(Gravity.CENTER, 0, 50);
            toast.show();
        });
    }

    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAndroid5_1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * method to check if android version is Nougat
     *
     * @return this return value
     */
    public static boolean isAndroid7() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isAndroid8() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isJelly17() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * method to check if android version is Marsh
     *
     * @return this return value
     */
    public static boolean isAndroid6() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * method to check if android version is Kitkat
     *
     * @return this return value
     */
    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * method to get color
     *
     * @param context this is the first parameter for getColor  method
     * @param id      this is the second parameter for getColor  method
     * @return return value
     */
    public static int getColor(Context context, int id) {
        if (isAndroid5_1()) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    /**
     * method to get drawable
     *
     * @param context this is the first parameter for getDrawable  method
     * @param id      this is the second parameter for getDrawable  method
     * @return return value
     */
    public static Drawable getDrawable(Context context, int id) {
        if (isAndroid5_1()) {
            return ContextCompat.getDrawable(context, id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    public static boolean isOnline(Context mContext) {

        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();

        } catch (Exception e) {
            LogCat("CheckConnectivity Exception: " + e.getMessage());
        }
        return connected;
    }

    public static Drawable tintDrawable(Context context, @DrawableRes int drawableRes, int colorCode) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        if (drawable != null) {
            drawable.mutate();
            DrawableCompat.setTint(drawable, colorCode);
        }
        return drawable;
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity context) {
        if (context.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(Activity context, View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    /*public static String saveBitmap(Bitmap bitmap, String imagePath) {
        try {
            File outputFile = new File(imagePath);
            //save the resized and compressed file to disk cache
            FileOutputStream bmpFile = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile);

            bmpFile.flush();
            bmpFile.close();
            String path = FilesManager.getPath(WhatsCloneApplication.getInstance(), FilesManager.getFile(outputFile));
            if (path == null) {
                path = FilesManager.copyDocumentToCache(FilesManager.getFile(outputFile), ".jpg");
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }*/

    /**
     * shake EditText error
     *
     * @param mContext this is the first parameter for showErrorEditText  method
     * @param editText this is the second parameter for showErrorEditText  method
     */
/*    private void showErrorEditText(Context mContext, EditText editText) {
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        editText.startAnimation(shake);
    }*/

    /**
     * method for LogCat
     *

     */
    public static void LogCat(String message) {
        Log.d("MyTag",message);
    }


    /**
     * method for Log cat Throwable
     *
     // @param Message this is  parameter for LogCatThrowable  method
     */

    public static void LaunchImagePreviewActivity(Activity mContext, String ImageType, String identifier, String currentUserId) {
        Intent mIntent = new Intent(mContext, ImagePreviewActivity.class);
        mIntent.putExtra("ImageType", ImageType);
        mIntent.putExtra("Identifier", identifier);
        mIntent.putExtra("currentUserId", currentUserId);
        mIntent.putExtra("SaveIntent", false);
        mContext.startActivity(mIntent);
        AnimationsUtil.setTransitionAnimation(mContext);
    }

    public static void LogCat(Throwable Message) {
        if (AppConstants.DEBUGGING_MODE)
            LogCat("LogCatThrowable " + Message.getMessage());
    }

    /**
     * method to export realm database
     *
     * @param mContext this is parameter for CustomToast  method
     */
    public static void exportRealmDatabase(Context mContext) {

        // init realm
      //  Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();

        File exportRealmFile = null;
        // get or create an "WhatsClone.realm" file
        exportRealmFile = new File(mContext.getExternalCacheDir(), "WhatsClone.realm");

        // if "WhatsClone.realm" already exists, delete
        exportRealmFile.delete();

        // copy current realm to "export.realm"
    /*    realm.writeCopyTo(exportRealmFile);

        realm.close();*/

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "abderrahim.elimame@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "this is ur local realm database WhatsClone");
        intent.putExtra(Intent.EXTRA_TEXT, "Hi man");
      /*  Uri u = FilesManager.getFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);*/

        // start email intent
        mContext.startActivity(Intent.createChooser(intent, "Choose an application"));
    }


    /**
     * method to loadJSONFromAsset json files from asset directory
     *
     * @param mContext this is  parameter for loadJSONFromAsset  method
     * @return return value
     */
    public static String loadJSONFromAsset(Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("country_phones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String saveBitmap(Bitmap bitmap, String imagePath,File fileName) {
        try {
            File outputFile = new File(imagePath);
            //save the resized and compressed file to disk cache
            FileOutputStream bmpFile = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile);

            bmpFile.flush();
            bmpFile.close();
            String path = FilesManager.getPath(AGApplication.getInstance(), FilesManager.getFile(outputFile));
            if (path == null) {
                path = FilesManager.copyDocumentToCache(FilesManager.getFile(outputFile), ".jpg");
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * method to loadJSONFromAsset json files from asset directory
     *
     * @param mContext this is  parameter for loadJSONFromAsset  method
     * @return return value
     */
    public static String loadJSONFromAsset(Context mContext, String name) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * method to launch the activities
     *
     * @param mContext  this is the first parameter for LaunchActivity  method
     * @param mActivity this is the second parameter for LaunchActivity  method
     */
    public static void LaunchActivity(Activity mContext, Class mActivity) {
        Intent mIntent = new Intent(mContext, mActivity);
        mContext.startActivity(mIntent);
        AnimationsUtil.setTransitionAnimation(mContext);
    }

    /**
     * method to launch the activities
     *
     * @param mContext  this is the first parameter for LaunchActivity  method
     * @param ImageType this is the second parameter for LaunchActivity  method
     */
    /*public static void LaunchImagePreviewActivity(Activity mContext, String ImageType, String identifier, String currentUserId) {
        Intent mIntent = new Intent(mContext, ImagePreviewActivity.class);
        mIntent.putExtra("ImageType", ImageType);
        mIntent.putExtra("Identifier", identifier);
        mIntent.putExtra("currentUserId", currentUserId);
        mIntent.putExtra("SaveIntent", false);
        mContext.startActivity(mIntent);
        AnimationsUtil.setTransitionAnimation(mContext);
    }*/

    /**
     * method to launch the activities
     *
     * @param mContext   this is the first parameter for LaunchActivity  method
     * @param identifier this is the second parameter for LaunchActivity  method
     */
    public static void LaunchVideoPreviewActivity(Activity mContext, String identifier, boolean isSent) {
        Intent mIntent = new Intent(mContext, VideoPlayerActivity.class);
        mIntent.putExtra("Identifier", identifier);
        mIntent.putExtra("isSent", isSent);
        mContext.startActivity(mIntent);
        AnimationsUtil.setTransitionAnimation(mContext);
    }

    /**
     * method to convert dp  to pixel
     *
     * @param dp this is  parameter for dpToPx  method
     * @return return value
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to convert pixel to dp
     *
     * @param px this is  parameter for pxToDp  method
     * @return return value
     */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to show snack bar
     *
     * @param mContext    this is the first parameter for Snackbar  method
     * @param view        this is the second parameter for Snackbar  method
     * @param Message     this is the thirded parameter for Snackbar  method
     * @param colorId     this is the fourth parameter for Snackbar  method
     * @param TextColorId this is the fifth parameter for Snackbar  method
     */
    public static void Snackbar(Context mContext, View view, String Message, int colorId, int TextColorId) {
        Snackbar snackbar = Snackbar.make(view, Message, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ContextCompat.getColor(mContext, colorId));
        TextView snackbarTextView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(mContext, TextColorId));
        snackbar.show();
    }

    /**
     * method to check if activity is running or not
     *
     * @param mContext     this is the first parameter for isActivityRunning  method
     * @param activityName this is the second parameter for isActivityRunning  method
     * @return return value
     */
    public static boolean isActivityRunning(Context mContext, String activityName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            String myAct=mContext.getPackageName() + "." + activityName;
            String taskClass=task.topActivity.getClassName();

            Log.d("SeenTag","myAct "+myAct);
            Log.d("SeenTag","topClass "+taskClass);
            if ((mContext.getPackageName() + "." + activityName).equals(task.topActivity.getClassName())) {
                return true;
            }
        }

        return false;
    }


    /**
     * method to copy text
     *
     * @param context       this is the first parameter for copyText  method
     * @param messagesModel this is the second parameter for copyText  method
     * @return return value
     */
    public static boolean copyText(Context context, MessageModel messagesModel) {
        String message = UtilsString.unescapeJava(messagesModel.getMessage());
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(message);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.message_copy), message);
            clipboard.setPrimaryClip(clip);
        }
        return true;
    }


    public static void shareIntent(File Url, Activity mActivity, String subject, String type) {
        if (Url != null) {
            Uri bmpUri = FilesManager.getFile(Url);
            if (bmpUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                if (subject != null) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
                }
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                switch (type) {
                    case AppConstants.SENT_TEXT:
                        shareIntent.setType("text/*");
                        break;
                    case AppConstants.SENT_IMAGES:
                        shareIntent.setType("image/*");
                        break;
                    case AppConstants.SENT_VIDEOS:
                        shareIntent.setType("video/mp4");
                        break;
                    case AppConstants.SENT_AUDIO:
                        shareIntent.setType("audio/mp3");
                        break;
                    case AppConstants.SENT_DOCUMENTS:
                        shareIntent.setType("application/pdf");
                        break;
                }
                mActivity.startActivity(Intent.createChooser(shareIntent, mActivity.getString(R.string.shareItem)));
            } else {
                CustomToast(mActivity, mActivity.getString(R.string.oops_something));
            }
        } else {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            if (subject != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
            }

            if (type.equals(AppConstants.SENT_TEXT)) {
                shareIntent.setType("plain/text");
            }
            mActivity.startActivity(Intent.createChooser(shareIntent, mActivity.getString(R.string.shareItem)));
        }

    }
/*
    public static String getAppVersion(Context mContext) {
        PackageInfo packageinfo;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            com.strolink.whatsUp.helpers.AppHelper.LogCat(" getAppVersion NameNotFoundException " + e.getMessage());
            return null;
        }
    }*/
/*
    public static int getAppVersionCode(Context mContext) {
        PackageInfo packageinfo;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            com.strolink.whatsUp.helpers.AppHelper.LogCat(" getAppVersion NameNotFoundException " + e.getMessage());
            return 0;
        }
    }*/

    /**
     * method to paly sound
     *
     * @param context
     * @param sounds
     * @return
     */
    public static void playSound(Context context, String sounds) {
        MediaPlayer mMediaPlayer = new MediaPlayer();

        try {
            if (((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == 2) {
                AssetFileDescriptor afd = context.getAssets().openFd(sounds);
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mMediaPlayer.setVolume(0.1f, 0.1f);
                afd.close();
                mMediaPlayer.prepare();
                mMediaPlayer.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        boolean isRunning = false;
        final ActivityManager activityManager = (ActivityManager) AGApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                isRunning = true;
            }
        }
        return isRunning;
    }


    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * delete cache method
     *
     * @param context
     */
    public static void deleteCache(Context context) {
        LogCat("here deleteCache method");
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            LogCat(" deleteCache method Exception " + e.getMessage());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    public static void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }


    public static String fixRotation(File file) {
        int rotation = getRotation(file.getPath());
        if (rotation == 0 || rotation == ExifInterface.ORIENTATION_NORMAL) return file.getPath();
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap bitmapSource = BitmapFactory.decodeFile(file.getPath());
        Bitmap cropped = Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.getWidth(), bitmapSource.getHeight(), matrix, true);
        try {
            FileOutputStream out = new FileOutputStream(file);
            cropped.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            return file.getPath();
        } catch (Exception ignored) {
        }
        return file.getPath();
    }

    private static int getRotation(String filePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            LogCat(e);
            rotate = 0;
        }

        return rotate;
    }

    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{1,20}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;

    }


    public static boolean isValidUrl(String url) {
        return !(url == null || url.isEmpty()) && Patterns.WEB_URL.matcher(url).matches();
    }


    public static void reloadActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();

        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
    }

    public static boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }


    public static byte[] longToByteArray(long l) {
        byte[] bytes = new byte[8];
        longToByteArray(bytes, 0, l);
        return bytes;
    }

    public static int longToByteArray(byte[] bytes, int offset, long value) {
        bytes[offset + 7] = (byte) value;
        bytes[offset + 6] = (byte) (value >> 8);
        bytes[offset + 5] = (byte) (value >> 16);
        bytes[offset + 4] = (byte) (value >> 24);
        bytes[offset + 3] = (byte) (value >> 32);
        bytes[offset + 2] = (byte) (value >> 40);
        bytes[offset + 1] = (byte) (value >> 48);
        bytes[offset] = (byte) (value >> 56);
        return 8;
    }


    public static Bitmap convertToBitmap(final Drawable drawable, final int width, final int height) {
        final AtomicBoolean created = new AtomicBoolean(false);
        final Bitmap[] result = new Bitmap[1];

        Runnable runnable = () -> {
            if (drawable instanceof BitmapDrawable) {
                result[0] = ((BitmapDrawable) drawable).getBitmap();
            } else {
                int canvasWidth = drawable.getIntrinsicWidth();
                if (canvasWidth <= 0) canvasWidth = width;

                int canvasHeight = drawable.getIntrinsicHeight();
                if (canvasHeight <= 0) canvasHeight = height;

                Bitmap bitmap;

                try {
                    bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                    bitmap = null;
                }

                result[0] = bitmap;
            }

            synchronized (result) {
                created.set(true);
                result.notifyAll();
            }
        };

        Util.runOnMain(runnable);

        synchronized (result) {
            while (!created.get()) Util.wait(result, 0);
            return result[0];
        }
    }

    private final static Integer lock = 1;

    public static void runOnUIThread(Runnable runnable) {
        synchronized (lock) {
            AGApplication.applicationHandler.post(runnable);
        }
    }
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void wait(Object lock, long timeout) {
        try {
            lock.wait(timeout);
        } catch (InterruptedException ie) {
            throw new AssertionError(ie);
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isLowMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && activityManager.isLowRamDevice()) ||
                activityManager.getLargeMemoryClass() <= 64;
    }

    public static <T> T getRandomElement(T[] elements) {
        try {
            return elements[SecureRandom.getInstance("SHA1PRNG").nextInt(elements.length)];
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static int toIntExact(long value) {
        if ((int) value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int) value;
    }

/*
    public static String getCurrentTime() {
        DateTime current = new DateTime();
        return String.valueOf(current);
    }

    public static boolean isWithinRangeDate(DateTime callDate, DateTime endDate) {
        return callDate.isBefore(endDate);
    }*/

    /**
     * @param context
     * @return the screen height in pixels
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }

    /*public static void checkDisplaySize() {
        try {
            Configuration configuration = WhatsCloneApplication.getInstance().getResources().getConfiguration();
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) WhatsCloneApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                    com.strolink.whatsUp.helpers.AppHelper.LogCat("display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
                }
            }
        } catch (Exception e) {
            com.strolink.whatsUp.helpers.AppHelper.LogCat(e);
        }
    }*/

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

 /*   @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, GcmTopicSubscribe.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(3 * 1000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }*/

}
