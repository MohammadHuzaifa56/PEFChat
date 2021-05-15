package com.pefgloble.pefchate.RegistrationScreens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.HomeScreens.HomeScreen;
import com.pefgloble.pefchate.JsonClasses.auth.JoinModelResponse;
import com.pefgloble.pefchate.JsonClasses.auth.LoginModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.APIAuthentication;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;

import org.jetbrains.annotations.NotNull;

public class Firebase_OTP extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button btnVerify;
    TextView resend_code,count;
    EditText edtPin;
    final Context context = this;
    String number,verificationid,name,mail,org,desig,country;
    ProgressDialog progressDialog;
   // PinView pinCode;
    int time = 50;

    public static final String storage = "UserDataStorage";

    CountDownTimer mCountDownTimer = new CountDownTimer(80 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

            if((millisUntilFinished / 1000)>=60){
                count.setText("00 : " + String.valueOf(time) + " sec left");
                time --;
            }else{
                count.setText("00 : "+ (millisUntilFinished / 1000) + " sec left");
            }

        }

        @Override
        public void onFinish() {

           // sendVerificationCode(number);
          //  count.setText("Code is sent again.");
            resend_code.setVisibility(View.VISIBLE);
            mCountDownTimer.cancel();
            count.setVisibility(View.GONE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_otp);

        progressDialog = new ProgressDialog(context);


        //------------------------------------ Getting user info -----------------------------------
        SharedPreferences sharedPreferences = getSharedPreferences(storage, Context.MODE_PRIVATE);
        number = sharedPreferences.getString("number","");
        country=sharedPreferences.getString("country","");

        sendVerificationCode(number,country);

   //     pinCode = findViewById(R.id.firstPinView);
        edtPin=findViewById(R.id.edtVerify);
        resend_code = findViewById(R.id.resend_code);
        count = findViewById(R.id.count);
 //       userNum=findViewById(R.id.user_number);
   //     userNum.setText(number);
        mCountDownTimer.start();
        mAuth = FirebaseAuth.getInstance();





        SpannableString ss = new SpannableString(getResources().getString(R.string.resend_code));
        ClickableSpan span1 = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(context, R.color.theme_green));
                ds.setUnderlineText(false);
            }
            @Override
            public void onClick(View textView) {

                resendVerificationCode(number);

                //Toast.makeText(Firebase_OTP.this, "Under Construction", Toast.LENGTH_SHORT).show();

            }
        };

        ss.setSpan(span1, 22, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        resend_code.setText(ss);
        resend_code.setMovementMethod(LinkMovementMethod.getInstance());
        resend_code.setHighlightColor(getResources().getColor(android.R.color.transparent));

    //    resendVerificationCode(number);

        btnVerify=findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = edtPin.getText().toString().trim();

                if ((code.isEmpty() || code.length() < 6)) {

                    edtPin.setError("Please enter 6-digit code");
                    edtPin.requestFocus();
                    return;
                }

                verifyCode(code);

                }
        });

    }

    private void resendVerificationCode(String number) {
        AppHelper.ShowProgressDialog(this,"Resending OTP");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIAuthentication apiAuthentication=retrofit.create(APIAuthentication.class);
        Call<JoinModelResponse> exampleCall= (Call<JoinModelResponse>) apiAuthentication.resend(number);

        exampleCall.enqueue(new Callback<JoinModelResponse>() {
            @Override
            public void onResponse(Call<JoinModelResponse> call, Response<JoinModelResponse> response) {
                AppHelper.HideProgressDialog(Firebase_OTP.this);
                resend_code.setVisibility(View.GONE);
                count.setVisibility(View.VISIBLE);
                mCountDownTimer.start();
                JoinModelResponse joinModelResponse=response.body();
                Toast.makeText(Firebase_OTP.this,joinModelResponse.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JoinModelResponse> call, Throwable t) {
                AppHelper.HideProgressDialog(Firebase_OTP.this);
                Toast.makeText(Firebase_OTP.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyCode(String code) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIAuthentication apiAuthentication=retrofit.create(APIAuthentication.class);
        Call<JoinModelResponse> exampleCall= (Call<JoinModelResponse>) apiAuthentication.verifyUser(code);

        exampleCall.enqueue(new Callback<JoinModelResponse>() {
            @Override
            public void onResponse(Call<JoinModelResponse> call, Response<JoinModelResponse> response) {
                JoinModelResponse joinModelResponse=response.body();
                Toast.makeText(Firebase_OTP.this,joinModelResponse.getMessage(),Toast.LENGTH_SHORT).show();

                if (joinModelResponse.isSuccess()){
                    String token=joinModelResponse.getToken();
                    String userID=joinModelResponse.getUserID();

                    PreferenceManager preferenceManager=new PreferenceManager();

                    preferenceManager.setID(Firebase_OTP.this,userID);
                    preferenceManager.setToken(Firebase_OTP.this,token);
                    preferenceManager.setPhone(Firebase_OTP.this,number);
                    if (token!=null) {
                        preferenceManager.setRegistered(getApplicationContext(), true);
                    }
                    SocketConnectionManager.getInstance().connectSocket(AGApplication.getInstance());
                    startActivity(new Intent(getApplicationContext(),HomeScreen.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JoinModelResponse> call, Throwable t) {

            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mCountDownTimer != null) mCountDownTimer.cancel();
                            progressDialog.dismiss();

                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                            if (isNewUser) {

                                startActivity(new Intent(context, RegistrationScreen.class));
                                finish();

                            } else {

                                startActivity(new Intent(context, HomeScreen.class));
                                finish();

                            }

                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }

    private void sendVerificationCode(String number,String country) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoginModel loginModel=new LoginModel(number,country);
        APIAuthentication apiAuthentication=retrofit.create(APIAuthentication.class);
        Call<JoinModelResponse> exampleCall= (Call<JoinModelResponse>) apiAuthentication.join(loginModel);

        exampleCall.enqueue(new Callback<JoinModelResponse>() {
            @Override
            public void onResponse(Call<JoinModelResponse> call, Response<JoinModelResponse> response) {
                JoinModelResponse joinModelResponse=response.body();
                if (joinModelResponse!=null) {
                    Toast.makeText(Firebase_OTP.this, joinModelResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(Firebase_OTP.this, "Response is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JoinModelResponse> call, Throwable t) {
                Toast.makeText(Firebase_OTP.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

/*        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);*/

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            Log.d("onVerfication", "Code outer:" + code);
            if (code != null) {
                verifyCode(code);
                //pinCode.setText(code);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationid = s;

        }
    };
}