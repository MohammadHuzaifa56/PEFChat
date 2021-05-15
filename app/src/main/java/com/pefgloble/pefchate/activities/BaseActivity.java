package com.pefgloble.pefchate.activities;


import android.annotation.SuppressLint;
import android.content.Intent;

import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    boolean isMainActivity = false;

    @Override
    protected void onResume() {
        super.onResume();
        SocketConnectionManager.getInstance().checkSocketConnection();
    }

    @Override
    public void finish() {
        super.finish();
        if (isMainActivity) {
            AppHelper.LogCat("MainActivity");
            isMainActivity = false;
        } else {
            AppHelper.LogCat(" not MainActivity");
            overridePendingTransitionExit();
        }

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    public void setMainActivity(boolean mainActivity) {
        isMainActivity = mainActivity;
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        if (AppConstants.ENABLE_ANIMATIONS)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        if (AppConstants.ENABLE_ANIMATIONS)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
