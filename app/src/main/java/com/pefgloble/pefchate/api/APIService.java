package com.pefgloble.pefchate.api;


import android.content.Context;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.DownloadProgressInterceptor;
import com.pefgloble.pefchate.jobs.DownloadProgressResponseBody;


import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.pefgloble.pefchate.helpers.files.FilesManager.getCacheDir;


/**
 * Created by Abderrahim El imame on 27/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class APIService {

    private int cacheSize = 10 * 1024 * 1024; // 10 MB

    protected Cache cache = new Cache(getCacheDir(), cacheSize);
    protected final Context context;

    public static APIService with(Context context) {
        return new APIService(context);
    }

    public APIService(Context context) {
        this.context = context;
    }

    private static Gson gson = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();


    public <S> S RootService(Class<S> serviceClass, String baseUrl) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().cache(cache);


        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Authorization", PreferenceManager.getInstance().getToken(context))
                    .method(original.method(), original.body())
                    .build();
            // Customize or return the response
            return chain.proceed(request);
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (AppConstants.DEBUGGING_MODE) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient client = httpClient
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        return builder.create(serviceClass);
    }


    public <S> S AuthService(Class<S> serviceClass, String baseUrl) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .method(original.method(), original.body())
                    .build();

            // Customize or return the response
            return chain.proceed(request);
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (AppConstants.DEBUGGING_MODE) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient client = httpClient
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client);

        Retrofit adapter = builder.build();

        return adapter.create(serviceClass);
    }


    public <S> S DownloadService(Class<S> serviceClass, String baseUrl, DownloadProgressResponseBody.DownloadProgressListener listener) {
        DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(listener);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Authorization", PreferenceManager.getInstance().getToken(context))
                    .method(original.method(), original.body())
                    .build();
            // Customize or return the response
            return chain.proceed(request);
        });


        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
       if (AppConstants.DEBUGGING_MODE) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient client = httpClient
               // .addInterceptor(loggingInterceptor)
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        return builder.create(serviceClass);
    }

    public <S> S UploadService(Class<S> serviceClass, String baseUrl) {


        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Authorization", PreferenceManager.getInstance().getToken(context))
                    .method(original.method(), original.body())
                    .build();
            // Customize or return the response
            return chain.proceed(request);
        });

/*
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (AppConstants.DEBUGGING_MODE) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }*/

        OkHttpClient client = httpClient
             //   .addInterceptor(loggingInterceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        return builder.create(serviceClass);
    }

    public <S> S ApiService(Class<S> serviceClass, String url) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().cache(cache);

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    //.header("Content-Type", "application/json")
                    .method(original.method(), original.body())
                    .build();
            // Customize or return the response
            return chain.proceed(request);
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (AppConstants.DEBUGGING_MODE) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient client = httpClient
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)

                .addConverterFactory(GsonConverterFactory.create())/* Converter Factory to convert your Json to gson */
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();


        return builder.create(serviceClass);
    }

}