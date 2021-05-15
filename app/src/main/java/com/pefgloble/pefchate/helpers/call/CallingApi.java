package com.pefgloble.pefchate.helpers.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.pefgloble.pefchate.AgoraVideo.openvcall.ui.CallActivity;
import com.pefgloble.pefchate.activities.call.AudioCallView;
import com.pefgloble.pefchate.activities.call.IncomingCallScreen;
import com.pefgloble.pefchate.activities.call.VideoCallView;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.presenter.CallsController;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;


public class CallingApi {


    public static void OpenIncomingCallScreen(JSONObject data, Context context) {
        try {

            Intent incomingScreen = new Intent(context, IncomingCallScreen.class);
            incomingScreen.putExtra("call_from", data.getString("call_from"));
            incomingScreen.putExtra("callId", data.getString("callId"));
            incomingScreen.putExtra("call_id", data.getString("call_id"));
            incomingScreen.putExtra("callType", data.getString("callType"));
            incomingScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(incomingScreen);

        } catch (JSONException e3) {
            AppHelper.LogCat("JSONException " + e3.getMessage());
            EventBus.getDefault().post("Cancel Call");
        }
    }

    public static void sendCallEvent(String status, String recipientId, String call_id, boolean videoCall) {
        try {


            JSONObject updateMessage = new JSONObject();
            updateMessage.put("call_from", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
            updateMessage.put("call_id", call_id);
            if (videoCall) {
                updateMessage.put("callType", AppConstants.VIDEO_CALL);
            } else {
                updateMessage.put("callType", AppConstants.VOICE_CALL);
            }
            updateMessage.put("status", status);
            updateMessage.put("recipientId", recipientId);

            //emit by socket to other user
            AppHelper.runOnUIThread(() -> {

                Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                if (mSocket != null) {
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_NEW_USER_CALL_TO_SERVER, updateMessage);
                }

            });
        } catch (JSONException e2) {
            AppHelper.LogCat("JSONException "+e2.getMessage());
        }
    }

    @SuppressLint("CheckResult")
    public static void callInit(Context context, String recipientId, String type) {
        CallsController.getInstance().saveCallToLocalDB(context, recipientId, type);
    }


    public static void startCall(Context context, String callType, String roomId, boolean accepted, String caller_id, String newCallInfoID) {

        boolean videoCallEnabled = true;
        Intent intent;
        if (callType.contentEquals(AppConstants.VOICE_CALL)) {
            videoCallEnabled = false;
            intent = new Intent(context, AudioCallView.class);
        } else {
            intent = new Intent(context, VideoCallView.class);
        }

        if (newCallInfoID != null)
            intent.putExtra("newCallInfoID", newCallInfoID);
        intent.putExtra("isAccepted", accepted);
        intent.putExtra("caller_id", caller_id);
        intent.putExtra(WhatsCloneApplication.getInstance().getPackageName() + ".ROOMID", roomId);
        intent.putExtra(WhatsCloneApplication.getInstance().getPackageName() + ".VIDEO_CALL", videoCallEnabled);
        context.startActivity(intent);
    }

    static void initiateCall(Context context, String to, String callType) {
        callInit(context, to, callType);
    }


}
