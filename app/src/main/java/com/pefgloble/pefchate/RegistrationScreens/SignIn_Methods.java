package com.pefgloble.pefchate.RegistrationScreens;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.pefgloble.pefchate.R;

public class SignIn_Methods extends AppCompatActivity {

    final Context context = this;
    LinearLayout btnPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in__methods);
        btnPhone = findViewById(R.id.btnPhone);

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Sign_In_Number.class));
                finish();
            }
        });

    }
}