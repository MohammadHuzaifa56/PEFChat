package com.pefgloble.pefchate.app.interfaces;


import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface UploadCallbacks {

    void onStart(String type, String messageId);

    void onUpdate(int percentage, String type, String messageId);

    void onError(String type, String messageId);


    void onFinish(String type, MessageModel messagesModel);

}
