package com.pefgloble.pefchate.JsonClasses.auth;


public class LoginModel {

    private String phone;
    private String country;
    private boolean account_kit;
    private String client_token;
    private String access_token;
    private String account_id;

    public LoginModel(String phone, String country) {
        this.phone = phone;
        this.country = country;
    }

    public String getClient_token() {
        return client_token;
    }

    public void setClient_token(String client_token) {
        this.client_token = client_token;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public boolean getAccount_kit() {
        return account_kit;
    }

    public void setAccount_kit(boolean account_kit) {
        this.account_kit = account_kit;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
