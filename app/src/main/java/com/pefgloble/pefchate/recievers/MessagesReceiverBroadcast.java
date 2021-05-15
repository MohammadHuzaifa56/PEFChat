package com.pefgloble.pefchate.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.notification.NotificationsModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Abderrahim El imame on 29/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesReceiverBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {
        String action = intent.getAction();

        if (action.equals("new_user_notification_whatsclone")) {

            String actionType = intent.getExtras().getString("actionType");

            switch (actionType) {
                case "new_user_message_notification":
                    AppHelper.runOnUIThread(() -> {

                        new Handler().postDelayed(() -> {
                            String Application = intent.getExtras().getString("app");
                            String file = intent.getExtras().getString("file");
                            String userphone = intent.getExtras().getString("phone");
                            String messageBody = intent.getExtras().getString("message");
                            String recipientId = intent.getExtras().getString("recipientID");
                            String senderId = intent.getExtras().getString("senderId");
                            String conversationID = intent.getExtras().getString("conversationID");
                            String userImage = intent.getExtras().getString("userImage");


                            if (Application != null && Application.equals(mContext.getPackageName())) {
                                if (AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity")) {
                                    NotificationsModel notificationsModel = new NotificationsModel();
                                    notificationsModel.setConversationID(conversationID);
                                    notificationsModel.setFile(file);
                                    notificationsModel.setGroup(false);
                                    notificationsModel.setImage(userImage);
                                    notificationsModel.setPhone(userphone);
                                    notificationsModel.setMessage(messageBody);
                                    notificationsModel.setRecipientId(recipientId);
                                    notificationsModel.setSenderId(senderId);
                                    notificationsModel.setAppName(Application);
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION, notificationsModel));
                                } else {
                                    if (file != null) {
                                        NotificationsManager.getInstance().showUserNotification(mContext, conversationID, userphone, file, senderId, userImage);
                                    } else {
                                        NotificationsManager.getInstance().showUserNotification(mContext, conversationID, userphone, messageBody, senderId, userImage);
                                    }
                                }
                            }

                        }, 1000);
                    });


                    break;
                case "new_group_message_notification":
                    String application = intent.getExtras().getString("app");
                    String File = intent.getExtras().getString("file");
                    String userPhone = intent.getExtras().getString("senderPhone");
                    String groupName = UtilsString.unescapeJava(intent.getExtras().getString("groupName"));
                    String messageGroupBody = intent.getExtras().getString("message");
                    String state = intent.getExtras().getString("state");
                    String groupID = intent.getExtras().getString("groupID");
                    String groupImage = intent.getExtras().getString("groupImage");
                    String conversationId = intent.getExtras().getString("conversationID");
                    String memberName;
                    String name = UtilsPhone.getContactName(userPhone);
                    if (name != null) {
                        memberName = name;
                    } else {
                        memberName = userPhone;
                    }


                    String message;
                    String userName = UtilsPhone.getContactName(userPhone);
                    switch (state) {
                        case AppConstants.CREATE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_created_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_created_this_group);
                            }

                            break;
                        case AppConstants.LEFT_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_left);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_left);
                            }


                            break;
                        case AppConstants.ADD_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_added_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_added_this_group);
                            }


                            break;

                        case AppConstants.REMOVE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_removed_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_removed_this_group);
                            }


                            break;
                        case AppConstants.ADMIN_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_make_admin_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_make_admin_this_group);
                            }


                            break;
                        case AppConstants.MEMBER_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_make_member_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_make_member_this_group);
                            }


                            break;
                        case AppConstants.EDITED_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + mContext.getString(R.string.he_edited_this_group);
                            } else {
                                message = "" + userPhone + " " + mContext.getString(R.string.he_edited_this_group);
                            }


                            break;
                        default:
                            message = messageGroupBody;
                            break;
                    }

                    /**
                     * this for default activity
                     */
                    Intent messagingGroupIntent = new Intent(mContext, MessagesActivity.class);
                    messagingGroupIntent.putExtra("conversationID", conversationId);
                    messagingGroupIntent.putExtra("groupID", groupID);
                    messagingGroupIntent.putExtra("isGroup", true);
                    messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    /**
                     * this for popup activity
                     */
                    Intent messagingGroupPopupIntent = new Intent(mContext, MessagesActivity.class);
                    messagingGroupPopupIntent.putExtra("conversationID", conversationId);
                    messagingGroupPopupIntent.putExtra("groupID", groupID);
                    messagingGroupPopupIntent.putExtra("isGroup", true);
                    messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (application != null && application.equals(mContext.getPackageName())) {
                        if (AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity")) {
                            NotificationsModel notificationsModel = new NotificationsModel();
                            notificationsModel.setConversationID(conversationId);
                            notificationsModel.setFile(File);
                            notificationsModel.setGroup(true);
                            notificationsModel.setImage(groupImage);
                            notificationsModel.setPhone(userPhone);
                            notificationsModel.setMessage(messageGroupBody);
                            notificationsModel.setState(state);
                            notificationsModel.setMemberName(memberName);
                            notificationsModel.setGroupID(groupID);
                            notificationsModel.setGroupName(groupName);
                            notificationsModel.setAppName(application);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION, notificationsModel));
                        } else {
                            if (File != null) {
                                NotificationsManager.getInstance().showGroupNotification(mContext, messagingGroupIntent, messagingGroupPopupIntent, groupName, memberName + " : " + File, groupID, groupImage);
                            } else {
                                NotificationsManager.getInstance().showGroupNotification(mContext, messagingGroupIntent, messagingGroupPopupIntent, groupName, memberName + " : " + message, groupID, groupImage);
                            }
                        }
                    }


                    break;


                case "new_user_call_notification":
                    String call_message = intent.getExtras().getString("message");
                    String userId = intent.getExtras().getString("userId");
                    boolean isVideo = intent.getExtras().getBoolean("isVideo");
                    if (isVideo)
                        NotificationsManager.getInstance().showSimpleNotification(mContext, true, mContext.getString(R.string.video_call_notify), call_message, userId, null);
                    else
                        NotificationsManager.getInstance().showSimpleNotification(mContext, true, mContext.getString(R.string.audio_call_notify), call_message, userId, null);
                    break;


            }

        }
    }

}
