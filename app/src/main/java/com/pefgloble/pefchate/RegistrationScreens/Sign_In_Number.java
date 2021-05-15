package com.pefgloble.pefchate.RegistrationScreens;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class Sign_In_Number extends AppCompatActivity {

    Button btnSignin;
    CountryCodePicker cop;
    EditText etPhoneNumber;
    String number,countryCode,country;
    CountryCodePicker ccp;
    final Context context = this;

    public static final String storage = "UserDataStorage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign__in__number);


        ccp = findViewById(R.id.ccp);
        etPhoneNumber=findViewById(R.id.etPhoneNumber);
        btnSignin =findViewById(R.id.btnVerify);

        //------------------------------------ Getting user info -----------------------------------
        SharedPreferences sharedPreferences = getSharedPreferences(storage, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        countryCode = ccp.getSelectedCountryCode();
        country=ccp.getSelectedCountryName();

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                countryCode = selectedCountry.getPhoneCode();
                country=selectedCountry.getName();
                AppHelper.LogCat("Country "+country);
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                number = etPhoneNumber.getText().toString().trim();

                if(TextUtils.isEmpty(number)){
                    etPhoneNumber.setError("Please enter number");
                    etPhoneNumber.requestFocus();
                    return;
                }

                String full_number = "+" + countryCode + number;

                String number_data = "We are about to verify your phone number:\n\n"+full_number+"\n\nIs this number correct?";

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Confirm Phone Number");
                alertDialog.setMessage(number_data);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                editor.putString("number",full_number);
                                editor.putString("country",country.toLowerCase());
                                editor.apply();
                                startActivity(new Intent(context, Firebase_OTP.class));
                                finish();

                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "EDIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        });
    }
}