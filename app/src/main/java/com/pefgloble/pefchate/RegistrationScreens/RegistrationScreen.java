package com.pefgloble.pefchate.RegistrationScreens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pefgloble.pefchate.HomeScreens.HomeScreen;
import com.pefgloble.pefchate.R;

public class RegistrationScreen extends AppCompatActivity {

EditText edtName,edtDesignation,edtMail,edtOrg,edtPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);

        edtName=findViewById(R.id.user_name);
        edtDesignation=findViewById(R.id.user_designation);
        edtMail=findViewById(R.id.user_email);
        edtOrg=findViewById(R.id.user_organization);
        edtPhone=findViewById(R.id.edtPhone);

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=edtName.getText().toString();
                String designation=edtDesignation.getText().toString();
                String mail=edtMail.getText().toString();
                String org=edtMail.getText().toString();
                String phone=edtPhone.getText().toString();


                Intent intent=new Intent(RegistrationScreen.this,HomeScreen.class);
                intent.putExtra("mName",name);
                intent.putExtra("mDesig",designation);
                intent.putExtra("mMail",mail);
                intent.putExtra("mOrg",org);
                intent.putExtra("mPhone",phone);
                startActivity(intent);
                finish();
            }
        });
    }
}