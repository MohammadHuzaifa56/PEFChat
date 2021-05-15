package com.pefgloble.pefchate.activities.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.status.StatusResponse;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.APIContact;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.activities.EditProfileActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.ui.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class EditUsernameActivity extends BaseActivity implements InputGeneralPanel.Listener {

    @BindView(R.id.cancelStatus)
    TextView cancelStatusBtn;

    @BindView(R.id.OkStatus)
    TextView OkStatusBtn;

    @BindView(R.id.layout_container)
    LinearLayout container;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EditText composeText;

    private EmojiPopup emojiPopup;

    ProgressBar progressBar;

    private String oldName,oldDesig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("currentUsername")) {
            oldName = getIntent().getStringExtra("currentUsername");
        }
        else {
            oldDesig = getIntent().getStringExtra("currentDesignation");
        }
        initializerView();
        progressBar=findViewById(R.id.toolbar_progress_bar);
        composeText.setText(oldName);

    }
    /**
     * method to initialize the view
     */
    private void initializerView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (oldName!=null) {
            getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        }
        else {
            getSupportActionBar().setTitle("Enter Your Designation");
        }
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String newUsername = composeText.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Enter Valid Name",Toast.LENGTH_SHORT).show();
            } else {

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BACKEND_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                PreferenceManager preferenceManager = new PreferenceManager();
                String token = preferenceManager.getToken(getApplicationContext());

                Call<StatusResponse> exampleCall;
                APIContact apiContact = retrofit.create(APIContact.class);
                if (oldName!=null) {
                    exampleCall = apiContact.editUsername(token, newUsername);
                }
                else {
                    exampleCall=apiContact.editUserDesignation(token,newUsername);
                }
                exampleCall.enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        StatusResponse statusResponse=response.body();
                        if (oldName!=null) {
                            PreferenceManager.getInstance().setUserName(getApplicationContext(), newUsername);
                        }
                        else {
                            PreferenceManager.getInstance().setUserDesig(getApplicationContext(), newUsername);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("ownerId", PreferenceManager.getInstance().getID(EditUsernameActivity.this));
                            jsonObject.put("is_group", false);
                            jsonObject.put("desig",newUsername);
                            jsonObject.put("image",PreferenceManager.getInstance().getImageUrl(AGApplication.getInstance()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                        if (mSocket != null)
                            mSocket.emit(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED, jsonObject);
                        }

                        Toast.makeText(getApplicationContext(),statusResponse.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<StatusResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });

        inputPanel.setListener(this);
        EmojiManager.install(new GoogleEmojiProvider());
        emojiPopup = EmojiPopup.Builder.fromRootView(container).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEmojiToggle() {

        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) emojiPopup.dismiss();
        else
            super.onBackPressed();
    }
}
