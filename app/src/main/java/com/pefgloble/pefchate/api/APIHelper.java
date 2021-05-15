package com.pefgloble.pefchate.api;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.RestAPI.FilesDownloadService;
import com.pefgloble.pefchate.RestAPI.FilesUploadService;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.jobs.DownloadProgressResponseBody;



/**
 * Created by Abderrahim El imame on 4/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class APIHelper {

    public static UsersService initialApiUsersContacts() {
        APIService mApiService = APIService.with(AGApplication.getInstance());
        return new UsersService(WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()), AGApplication.getInstance(), mApiService);
    }


    public static FilesUploadService initializeUploadFiles() {
        APIService mApiService = APIService.with(AGApplication.getInstance());
        return mApiService.UploadService(FilesUploadService.class, BuildConfig.BACKEND_BASE_URL);
    }


    public static FilesDownloadService initializeDownloadFiles(DownloadProgressResponseBody.DownloadProgressListener listener) {
        APIService mApiService = APIService.with(AGApplication.getInstance());
        return mApiService.DownloadService(FilesDownloadService.class, BuildConfig.BACKEND_BASE_URL, listener);


    }


    public static GroupsService initializeApiGroups() {
        APIService mApiService = APIService.with(AGApplication.getInstance());
        return new GroupsService(WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()), AGApplication.getInstance(), mApiService);
    }

   /* public static ConversationsService initializeConversationsService() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new ConversationsService(WhatsCloneApplication.getRealmDatabaseInstance(), mApiService);
    }*/

    public static MessagesService initializeMessagesService() {
        return new MessagesService(WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()));
    }

  /*  public static AuthService initializeAuthService() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new AuthService(WhatsCloneApplication.getInstance(), mApiService);
    }*/

   /* public static Observable<GiphyResponse> getGiphy(int offset, String query) {
        APIService apiService = new APIService(WhatsCloneApplication.getInstance());
        APIContact apiUsers = apiService.ApiService(APIContact.class, EndPoints.GET_GIF_BASE);
        return apiUsers.getGiphy(PreferenceManager.getInstance().getGiphyKey(WhatsCloneApplication.getInstance()), offset, GiphyLoader.PAGE_SIZE, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }*/

 /*   public static Observable<GiphyResponse> getGiphy(int offset) {
        APIService apiService = new APIService(WhatsCloneApplication.getInstance());
        APIContact apiUsers = apiService.ApiService(APIContact.class, EndPoints.GET_GIF_BASE);
        return apiUsers.getGiphy(PreferenceManager.getInstance().getGiphyKey(WhatsCloneApplication.getInstance()), offset, GiphyLoader.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }*/
}
