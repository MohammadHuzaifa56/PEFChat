package com.pefgloble.pefchate.RegistrationScreens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pefgloble.pefchate.HomeScreens.HomeScreen;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.helpers.PreferenceManager;

public class SplashScreen extends AppCompatActivity {

    ImageView circle1, circle2, logo;
    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isReg=PreferenceManager.getInstance().getRegistered(getApplicationContext());

        if (isReg){
            startActivity(new Intent(SplashScreen.this,HomeScreen.class));
            finish();
            return;
        }

        setContentView(R.layout.splash_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        logo = findViewById(R.id.SplashScrenn);
        circle1 = findViewById(R.id.light_circle);
        circle2 = findViewById(R.id.dark_circle);

     /*   motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {

                motionLayout.setVisibility(View.GONE);
                PreferenceManager preferenceManager=new PreferenceManager();
                boolean isReg=preferenceManager.getRegistered(getApplicationContext());

                if (isReg){
                    startActivity(new Intent(SplashScreen.this,HomeScreen.class));
                    overridePendingTransition(R.anim.fade_in_medium,R.anim.fade_out);
                }
                else {
                    startActivity(new Intent(SplashScreen.this,SignIn_Methods.class));
                    overridePendingTransition(R.anim.fade_in_medium,R.anim.fade_out);
                }
                finish();

            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });*/

        //Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);
        //Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);

  /*     aniSlide.setStartOffset(200);
       logo.startAnimation(aniSlide);
       circle2.startAnimation(animation2);*/

       // startActivity(new Intent(getApplicationContext(),HomeScreen.class));
       // finish();

        ObjectAnimator objectAnimator=ObjectAnimator.ofPropertyValuesHolder(
                logo,
                PropertyValuesHolder.ofFloat("scaleX",1.2f),
                PropertyValuesHolder.ofFloat("scaleY",1.2f)
        );
        objectAnimator.setDuration(500);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this,SignIn_Methods.class));
            }
        }, 4000);

    }

}