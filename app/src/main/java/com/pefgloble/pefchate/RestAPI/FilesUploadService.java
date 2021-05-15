package com.pefgloble.pefchate.RestAPI;

import com.pefgloble.pefchate.JsonClasses.messags.FilesResponse;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;


import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface FilesUploadService {


    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_IMAGE)
    Observable<FilesResponse> uploadImageFile(@Part MultipartBody.Part file);


    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_VIDEO)
    Observable<FilesResponse> uploadVideoFile(@Part MultipartBody.Part video);


    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_AUDIO)
    Observable<FilesResponse> uploadAudioFile(@Part MultipartBody.Part audio);


    /**
     * method to upload document
     *
     * @param document this is  parameter for  uploadMessageDocument method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_DOCUMENT)
    Observable<FilesResponse> uploadDocumentFile(@Part MultipartBody.Part document);

    /**
     * method to upload gif
     *
     * @param gif this is  parameter for  uploadMessageGif method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_GIF)
    Observable<FilesResponse> uploadGifFile(@Part MultipartBody.Part gif);


    /**
     * method to upload images
     *
     * @param image this is  the second parameter for  uploadMessageImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_GROUP_IMAGE)
    Observable<FilesResponse> uploadGroupImage(@Part MultipartBody.Part image);
    /**
     * method to upload user image
     *
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_USER_IMAGE)
    Observable<FilesResponse> uploadUserImage(@Part MultipartBody.Part image, @Path("userId") String id);


}
