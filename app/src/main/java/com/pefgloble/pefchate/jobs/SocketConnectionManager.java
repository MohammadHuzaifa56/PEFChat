package com.pefgloble.pefchate.jobs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.call.CallingApi;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.presenter.CallsController;
import com.pefgloble.pefchate.presenter.MessagesController;
import com.pefgloble.pefchate.presenter.StoriesController;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketConnectionManager {


    private static volatile SocketConnectionManager Instance = null;
    private Socket mSocket;
    private int mTries = 0;



    public static SocketConnectionManager getInstance() {

        SocketConnectionManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (SocketConnectionManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new SocketConnectionManager();
                }
            }
        }
        return localInstance;

    }
    public Socket getSocket() {
        return mSocket;
    }

    public void initializerSocket() {

        if (mSocket != null && mSocket.connected()) {
            return;
        }
        if (mSocket == null) {

            Log.d("SocTag","Options Called");
            IO.Options options = new IO.Options();
            options.forceNew = false; // will create two distinct connections if set it to true
            options.timeout = (long) (60 * 1000); //set -1 to  disable it
            options.reconnection = true;
            options.reconnectionDelay = (long) 3000;
            options.reconnectionDelayMax = (long) 60000;
            options.reconnectionAttempts = 99999;
            options.query = "authorization=" +PreferenceManager.getInstance().getToken(AGApplication.getInstance());

            try {
                mSocket = IO.socket(new URI(BuildConfig.MAIN_URL), options);
            } catch (Exception e) {
                Log.d("SocTag","URISyntaxException" + e.getMessage());
            }
        }
    }

    /**
     * method for server connection connect
     */
    public void connectSocket(Context mContext) {
        Log.d("SocTag","Connect is called");

        initializerSocket();

       // mSocket.connect();

        mSocket.once(io.socket.client.Socket.EVENT_CONNECT, args -> {
            mTries = 0;
            Log.d("SocTag","New Connection Chat is created"+mSocket.id());

            if (mSocket != null && mSocket.id() != null) {
                PreferenceManager.getInstance().setSocketID(mContext, mSocket.id());
                JSONObject json = new JSONObject();
                try {
                    json.put("connected", true);
                    json.put("connectedId", PreferenceManager.getInstance().getID(mContext));
                    json.put("socketId", PreferenceManager.getInstance().getSocketID(mContext));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mSocket != null)
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_CONNECTED, json);
                    emitSEvent();
            } else {
                reconnect();
            }
        }).on(io.socket.client.Socket.EVENT_CONNECT_ERROR, args -> {
            //  AppHelper.LogCat("EVENT_CONNECT_ERROR");
            Log.d("SocTag","Socket Error"+args[0].toString());
        }).once(io.socket.client.Socket.EVENT_DISCONNECT, args -> {
            try {
                AppHelper.LogCat("You  lost connection with chat server " + mSocket.id());
            } catch (Exception e) {
                AppHelper.LogCat("You  lost connection with chat server " + e.getMessage());
            }
            JSONObject jsonConnected = new JSONObject();
            try {
                jsonConnected.put("connectedId", PreferenceManager.getInstance().getID(mContext));
                jsonConnected.put("socketId", PreferenceManager.getInstance().getSocketID(mContext));
            } catch (JSONException e) {
                // e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SocketConstants.SOCKET_DISCONNECTED, jsonConnected);

        }).on("connect_timeout", args -> {
            AppHelper.LogCat("Socket EVENT_CONNECT_TIMEOUT ");
            reconnect();
        }).on("reconnect", args -> {
            AppHelper.LogCat("Reconnect  EVENT_RECONNECT ");
            reconnect();
        }).on(AppConstants.SocketConstants.SOCKET_NEW_USER_MESSAGE, args -> {
            Log.d("SocTag","Socket Recieve Msg Called");
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_NEW_USER_MESSAGE " + data.toString());
            try {
                    if (data.getBoolean("is_group")) {
                        MessagesController.getInstance().saveNewMessageGroup(data, mContext);
                    } else {
                        MessagesController.getInstance().saveNewUserMessage(data, mContext);
                    }
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }
        }).on(AppConstants.SocketConstants.SOCKET_IS_MESSAGE_DELIVERED, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_IS_MESSAGE_DELIVERED " + data.toString());
            try {
                MessagesController.getInstance().updateDeliveredStatus(data.getString("messageId"), data.getString("ownerId"));
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }
        }).on(AppConstants.SocketConstants.SOCKET_IS_MESSAGE_SEEN, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_IS_MESSAGE_SEEN " + data.toString());
            try {
                MessagesController.getInstance().updateSeenStatus(data.getString("messageId"), data.getString("ownerId"), data.getString("recipientId"), data.getBoolean("is_group"));
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }

        }).on(AppConstants.SocketConstants.SOCKET_EVENT_PING, args -> {
            // AppHelper.LogCat("socket ping " + mSocket.connected());

            JSONObject data = (JSONObject) args[0];
            String ping;
            try {
                ping = data.getString("beat");
            } catch (JSONException e) {
                return;
            }
            if (ping.equals("1")) {
                mSocket.emit(AppConstants.SocketConstants.SOCKET_EVENT_PONG);
            }
        }).on(AppConstants.SocketConstants.SOCKET_IS_ONLINE, args -> {
            final JSONObject data = (JSONObject) args[0];
            try {
                String senderID = data.getString("senderId");
                if (senderID.equals(PreferenceManager.getInstance().getID(mContext))) return;
                if (data.getBoolean("connected")) {
                    AppHelper.LogCat("connected online "+senderID);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_IS_ONLINE, senderID));
                } else {
                    AppHelper.LogCat("not connected");
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_IS_OFFLINE, senderID));
                }
            } catch (JSONException e) {
                AppHelper.LogCat(e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_IS_TYPING, args -> {
            AppHelper.LogCat("SOCKET_IS_TYPING ");
            JSONObject data = (JSONObject) args[0];
            try {
                String senderID = data.getString("senderId");
                String recipientID = data.getString("recipientId");
               /* if (MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;*/
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USER_TYPING, recipientID, senderID));
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_IS_STOP_TYPING, args -> {
            AppHelper.LogCat("SOCKET_IS_STOP_TYPING ");
            JSONObject data = (JSONObject) args[0];
            try {
                String senderID = data.getString("senderId");
                String recipientID = data.getString("recipientId");
               // if (MessagesController.getInstance().checkIfUserBlockedExist(senderID))
               //     return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USER_STOP_TYPING, recipientID, senderID));
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_MEMBER_IS_TYPING, args -> {
            AppHelper.LogCat("SOCKET_MEMBER_IS_TYPING ");

            JSONObject data = (JSONObject) args[0];
            try {
                String senderID = data.getString("senderId");
                String groupId = data.getString("groupId");

                if (!MessagesController.getInstance().checkIfGroupExist(groupId) || MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MEMBER_TYPING, groupId, senderID));
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_MEMBER_IS_STOP_TYPING, args -> {
            AppHelper.LogCat("SOCKET_MEMBER_IS_STOP_TYPING ");
            JSONObject data = (JSONObject) args[0];
            try {
                String senderID = data.getString("senderId");
                String groupId = data.getString("groupId");
                 if (!MessagesController.getInstance().checkIfGroupExist(groupId) || MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MEMBER_STOP_TYPING, groupId, senderID));
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED, args -> {
            AppHelper.LogCat("SOCKET_IMAGE_PROFILE_UPDATED ");
            JSONObject data = (JSONObject) args[0];
            try {
                String ownerId = data.getString("ownerId");
                String desig=data.getString("desig");
                boolean is_group = data.getBoolean("is_group");
                String imageUrl=data.getString("image");


                MessagesController.getInstance().getNotifyForImageProfileChanged(ownerId, is_group,desig,imageUrl);
            } catch (Exception e) {
                AppHelper.LogCat("Desig Exception" +e);
            }
        }).on(AppConstants.SocketConstants.SOCKET_NEW_USER_JOINED, args -> {

           // JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_NEW_USER_JOINED " );
        /*    try {
                String ownerId = data.getString("senderId");
                String phone = data.getString("phone");

                if (ownerId.equals(PreferenceManager.getInstance().getID(AGApplication.getInstance())))
                    return;

                if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), phone))
                    NotificationsManager.getInstance().showSimpleNotification(mContext, false, phone, AppConstants.JOINED_MESSAGE_SMS, ownerId, null);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }*/
        }).on(AppConstants.SocketConstants.SOCKET_NEW_USER_STORY, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_NEW_USER_STORY " + data.toString());
            Log.d("SocTag","New Story Event Called");
            StoriesController.getInstance().saveNewUserStory(data);

              /*   else {

                    String senderId = data.getJSONObject("owner").getString("_id");
                    String storyId = data.getString("_id");
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("storyId", storyId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(AGApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_SEEN, updateMessage, (Ack) args1 -> {

                            JSONObject data1 = (JSONObject) args1[0];
                            try {
                                if (data1.getBoolean("success")) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> duplicate story Recipient mark story as  seen <--");
                                } else {
                                    AppHelper.LogCat(" duplicate story   seen ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    }
                }*/

        }).on(AppConstants.SocketConstants.SOCKET_NEW_EXPIRED_STORY_TO_SERVER, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_NEW_EXPIRED_STORY_TO_SERVER " + data.toString());
            try {
                StoriesController.getInstance().deleteExpiredStory(data.getString("_id"), data.getJSONObject("owner").getString("_id"));
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }

        }).on(AppConstants.SocketConstants.SOCKET_IS_STORY_SEEN, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_IS_STORY_SEEN " + data.toString());
            try {
                StoriesController.getInstance().updateSeenStatus(data.getString("storyId"), data.getJSONArray("users"));
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }

        })
        .on(AppConstants.SocketConstants.SOCKET_NEW_USER_CALL, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_NEW_USER_CALL " + data.toString());
            try {

                AppHelper.LogCat("SOCKET_NEW_USER_CALL checkIfUserBlockedExist ");

                if (data.getString("status").equals("init_call")) {
                    AppHelper.LogCat("onCallEventStatus" + "init_call");
                    DateTime current = new DateTime();
                    // if (AppHelper.isWithinRangeDate(current, UtilsTime.getCorrectDate(data.getString("date")).plusSeconds(6))) {

                    if (!data.getBoolean("missed")) {

                        CallingApi.OpenIncomingCallScreen(data, WhatsCloneApplication.getInstance());

                    } else {
                        if (CallsController.getInstance().checkIfSingleCallExist(data.getString("callId"))) {
                            AppHelper.LogCat("checkIfSingleCallExist ");
                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("callId", data.getString("callId"));
                            } catch (JSONException e) {
                                // e.printStackTrace();
                                AppHelper.LogCat("JSONException " + e.getMessage());
                            }


                            Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                            if (mSocket != null) {
                                mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage);
                            }
                        } else {
                            boolean video;
                            video = data.getString("callType").equals(AppConstants.VIDEO_CALL);
                            try {
                                CallsController.getInstance().saveToDataBase(data.getString("call_from"),
                                        data.getString("callId"),
                                        video, UtilsTime.getCorrectDate(data.getString("date")),
                                        data.getJSONObject("owner").getString("phone"),
                                        data.getJSONObject("owner").getString("username"));
                                Intent mIntent = new Intent("new_user_notification_whatsclone");
                                mIntent.putExtra("actionType", "new_user_call_notification");
                                mIntent.putExtra("isVideo", video);
                                mIntent.putExtra("message", WhatsCloneApplication.getInstance().getString(R.string.new_missed_call));
                                mIntent.putExtra("userId", String.valueOf(data.getString("call_from")));
                                mIntent.putExtra("app", WhatsCloneApplication.getInstance().getPackageName());
                                WhatsCloneApplication.getInstance().sendBroadcast(mIntent);
                            } catch (Exception e) {
                                AppHelper.LogCat(e.getMessage());
                            }


                        }
                    }

                } else {
                    AppHelper.LogCat("onCallEventStatus" + "jawb" + data.toString());
                    try {
                        JSONObject object = new JSONObject();
                        object.put("eventName", "CallEventChange");
                        object.put("status", data.getString("status"));
                        EventBus.getDefault().post(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

             /*    else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("callId", data.getString("callId"));
                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage);
                    }
                }*/
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }


        }).on(AppConstants.SocketConstants.SOCKET_IS_CALL_SEEN, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_IS_CALL_SEEN " + data.toString());
            try {
                StoriesController.getInstance().updateSeenStatus(data.getString("storyId"), data.getJSONArray("users"));
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }

        });


        //   if (!mSocket.connected())
        mSocket.connect();
       // emitSEvent();
        emitUserIsOnline();


    }

    private void emitSEvent() {
        JSONObject json = new JSONObject();
        try {
            json.put("connected", true);
            json.put("connectedId", PreferenceManager.getInstance().getID(AGApplication.getInstance()));
            json.put("socketId",mSocket.id());
            Log.d("SocTag","New SocId "+mSocket.id());
            Log.d("SocTag","Stored SocId "+PreferenceManager.getInstance().getSocketID(AGApplication.getInstance()));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (mSocket != null)
            mSocket.emit("s", json);
        //mSocket.emit(AppConstants.SocketConstants.SOCKET_IS_TYPING,json);
    }


    private void emitUserIsOnline() {

        JSONObject json = new JSONObject();
        try {
            json.put("connected", true);
            json.put("senderId", PreferenceManager.getInstance().getID(AGApplication.getInstance()));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (mSocket != null)
            mSocket.emit(AppConstants.SocketConstants.SOCKET_IS_ONLINE, json);
            //mSocket.emit(AppConstants.SocketConstants.SOCKET_IS_TYPING,json);

    }


    /**
     * method to disconnect user form server
     */
    /**
     * method to disconnect user form server
     */
    public void disconnectSocket() {
        emitUserIsOffline();
        if (mSocket != null) {
            mSocket.off(io.socket.client.Socket.EVENT_CONNECT);
            mSocket.off(io.socket.client.Socket.EVENT_DISCONNECT);
            mSocket.off("connect_timeout");
            mSocket.off("reconnect");

            mSocket.off(AppConstants.SocketConstants.SOCKET_CHECK_STATE);
            mSocket.off(AppConstants.SocketConstants.SOCKET_CONNECTED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_DISCONNECTED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_EVENT_PING);
            mSocket.off(AppConstants.SocketConstants.SOCKET_EVENT_PONG);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_ONLINE);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_TYPING);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_STOP_TYPING);
            mSocket.off(AppConstants.SocketConstants.SOCKET_MEMBER_IS_TYPING);
            mSocket.off(AppConstants.SocketConstants.SOCKET_MEMBER_IS_STOP_TYPING);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_MESSAGE);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_MESSAGE_TO_SERVER);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_DELIVERED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_MESSAGE_DELIVERED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_MESSAGE_SEEN);

            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_STORY);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_STORY_TO_SERVER);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_EXPIRED_STORY_TO_SERVER);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_SEEN);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_EXIST_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_STORY_SEEN);

            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_CALL);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_CALL_TO_SERVER);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_AS_SEEN);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_IS_CALL_SEEN);

            mSocket.off(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED);
            mSocket.off(AppConstants.SocketConstants.SOCKET_NEW_USER_JOINED);


            mSocket.disconnect();
            mSocket.close();
            mSocket = null;

        }

        AppHelper.LogCat("disconnect in service");
    }

    private void emitUserIsOffline() {

        JSONObject json = new JSONObject();
        try {
            json.put("connected", false);
            json.put("senderId", "6041b7d102ca2d000410939e");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mSocket != null)
            mSocket.emit(AppConstants.SocketConstants.SOCKET_IS_ONLINE, json);

    }

    /**
     * method to reconnect sockets
     */
    public void reconnect() {
        if (mTries < 5) {
            mTries++;
            // AppHelper.restartService();
            //
            //start();
            // handler.postDelayed(() -> updateStatusDeliveredOffline(mContext), 1500);
            checkSocketConnection();
        } else {
            //disconnectSocket();
            // WhatsCloneApplication.getInstance().stopService(new Intent(WhatsCloneApplication.getInstance(), MainService.class));
        }

    }



    public void checkSocketConnection() {
        AppHelper.LogCat("Check Socket Called");
        Socket mSocket = SocketConnectionManager.getInstance().getSocket();
        JSONObject checkObject = new JSONObject();
        try {
            checkObject.put("ownerId",PreferenceManager.getInstance().getID(AGApplication.getInstance()));

            if (mSocket != null) {

                mSocket.emit(AppConstants.SocketConstants.SOCKET_CHECK_STATE, checkObject, (Ack) args -> {

                    JSONObject data = (JSONObject) args[0];

                    if (data == null) {
                        reconnectSocket();
                    } else {
                        AppHelper.LogCat("data " + data.toString());
                        try {
                            if (!data.getBoolean("connected")) {
                                reconnectSocket();
                            } else {
                                emitUserIsOnline();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                reconnectSocket();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void reconnectSocket() {

        disconnectSocket();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectSocket(AGApplication.getInstance());
    }
}
