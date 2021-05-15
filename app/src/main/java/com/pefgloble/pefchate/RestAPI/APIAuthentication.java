package com.pefgloble.pefchate.RestAPI;


import com.pefgloble.pefchate.JsonClasses.auth.JoinModelResponse;
import com.pefgloble.pefchate.JsonClasses.auth.LoginModel;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface APIAuthentication {

     //@param loginModel this is parameter for join method


    @POST(EndPoints.JOIN)
    Call<JoinModelResponse> join(@Body LoginModel loginModel);


    @FormUrlEncoded
    @POST(EndPoints.RESEND_REQUEST_SMS)
    Call<JoinModelResponse> resend(@Field("phone") String phone);

    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.VERIFY_USER)
    Call<JoinModelResponse> verifyUser(@Field("code") String code);
    /**
     * method to check if account kit is enabled
     *
     * @return this is what method will return boolean value
     */

    @GET(EndPoints.CHECK_ACCOUNT_KIT)
    Observable<JoinModelResponse> checkAccountKit();


}
