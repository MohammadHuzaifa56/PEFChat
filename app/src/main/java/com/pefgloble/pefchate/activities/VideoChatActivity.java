package com.pefgloble.pefchate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.pefgloble.pefchate.R;

public class VideoChatActivity extends AppCompatActivity {
    private static final String TAG = VideoChatActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    private RtcEngine mRtcEngine;
    private IRtcEngineEventHandler mRtcEventHandler;

    private FrameLayout mLocalContainer;
    private FrameLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;


    private Button mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;

    private void setupRemoteVideo(int uid) {

        // Create a SurfaceView object.
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
        Log.d("agora","Remote CHannel"+uid);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        initUI();
        mRtcEventHandler = new IRtcEngineEventHandler() {


            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("agora","On Join Channel Sucess");
                        setupLocalVideo();
                    }
                });
            }

            @Override
            public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupRemoteVideo(uid);
                        Log.d("agora","On Remote Video");
                    }
                });
            }

            @Override
            public void onError(int err) {
                super.onError(err);
                Log.d("agora","error code : "+err);
            }
        };

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.mLocalContainer);
        mRemoteContainer = findViewById(R.id.mRemoteContainer);

        mCallBtn = findViewById(R.id.btnCall);

         }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupLocalVideo();
        joinChannel();

        //joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(),getString(R.string.agora_app_id), mRtcEventHandler);
            joinChannel();
            setupVideoProfile();
            setupLocalVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
 //       setUpSession();
    }


    private void setupVideoProfile() {

        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);

    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {

        mRtcEngine.enableAudio();
        mRtcEngine.enableVideo();
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoSurface.setZOrderMediaOverlay(true);
        mLocalContainer.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_HIDDEN, 0));

        Log.d("agora","Local Video" + mRtcEngine.toString());

    }

    private void joinChannel() {

/*        String token =null;
        mRtcEngine.joinChannel(token, "channel123", null, 0);*/
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
            leaveChannel();
            RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }



    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
    }
    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }

}