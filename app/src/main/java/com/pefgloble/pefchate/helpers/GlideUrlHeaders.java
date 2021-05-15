package com.pefgloble.pefchate.helpers;

import android.content.Context;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.app.WhatsCloneApplication;

/**
 * Created by Abderrahim El imame on 10/4/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GlideUrlHeaders {



   public static GlideUrl getUrlWithHeaders(String url){
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", PreferenceManager.getInstance().getToken(AGApplication.getInstance()))
                .build());
    }
    public static GlideUrl getUrlWithHeaders(String url, Context context){
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", PreferenceManager.getInstance().getToken(context))
                .build());

    }
}
