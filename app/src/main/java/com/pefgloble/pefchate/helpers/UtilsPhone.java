package com.pefgloble.pefchate.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;


import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 03/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UtilsPhone {
    private static volatile UtilsPhone Instance = null;
    private String[] projectionPhones = {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };
    private String[] projectionNames = {
            ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
    };
    public ConcurrentHashMap<Integer, Contact> contactsMap = new ConcurrentHashMap<Integer, Contact>();


    private ArrayList<UsersModel> mListContacts = new ArrayList<UsersModel>();
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private static String name = null;


    public static UtilsPhone getInstance() {
        UtilsPhone localInstance = Instance;
        if (localInstance == null) {
            synchronized (UtilsPhone.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UtilsPhone();
                }
            }
        }
        return localInstance;
    }

    /**
     * method to retrieve all contacts from the book
     *
     * @return return value
     *//*
    public ArrayList<ContactsModel> GetPhoneContacts() {
        String country = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) WhatsCloneApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                country = telephonyManager.getSimCountryIso().toUpperCase();
            } else {
                country = AppConstants.DEFAULT_COUNTRY_CODE;//"JO";
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        contactsMap = readContactsFromPhoneBook();
        for (Map.Entry entry : contactsMap.entrySet()) {

            Contact c = (Contact) entry.getValue();
            String phoneNumber = "";
            int cc = -1;
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber NumberProto = phoneUtil.parse(c.phones.get(0), country);
                if (String.valueOf(NumberProto.getNationalNumber()).length() < 9) {
                    continue;
                }
                phoneNumber = phoneUtil.format(NumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);


            } catch (NumberParseException e) {
                AppHelper.LogCat("NumberParseException was thrown: " + e.toString());
            }
//            AppHelper.LogCat(phoneNumber);

            //FileLog.e("contacts:",x +" - locale:"+country+" name:"+c.first_name +" "+c.last_name+" sPhone:"+phone);

 *//*       ContentResolver contentResolver = WhatsCloneApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cur = contentResolver.query(ContactMobileNumbQuery.CONTENT_URI, ContactMobileNumbQuery.PROJECTION, ContactMobileNumbQuery.SELECTION, null, ContactMobileNumbQuery.SORT_ORDER);
        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    ContactsModel contactsModel = new ContactsModel();
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    String image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


                    //     AppHelper.LogCat("number phone --> " + phoneNumber);
                    if (name.contains("\\s+")) {
                        String[] nameArr = name.split("\\s+");
                        contactsModel.setUsername(nameArr[0] + nameArr[1]);
                        // AppHelper.LogCat("Fname --> " + nameArr[0]);
                        // AppHelper.LogCat("Lname --> " + nameArr[1]);
                    } else {
                        contactsModel.setUsername(name);
                        //AppHelper.LogCat("name" + name);
                    }*//*
            if (phoneNumber != null) {

                String Regex = "[^\\d]";
          *//*      String PhoneDigits = phoneNumber.replaceAll(Regex, "");
                boolean isValid = !(PhoneDigits.length() < 6 || PhoneDigits.length() > 13);
                String phNumberProto = PhoneDigits.replaceAll("-", "");
                String PhoneNo;
                if (PhoneDigits.length() != 10) {
                    PhoneNo = "+";
                    PhoneNo = PhoneNo.concat(phNumberProto);
                } else {
                    PhoneNo = phNumberProto;
                }*//*


                // AppHelper.LogCat("phoneNumber --> " + phoneNumber);
                String phoneNumberTmpFinal;
                Phonenumber.PhoneNumber phoneNumberInter = getPhoneNumber(phoneNumber);
                if (phoneNumberInter != null) {
                    //  AppHelper.LogCat("phoneNumberInter --> " + phoneNumberInter.getNationalNumber());
                    phoneNumberTmpFinal = String.valueOf(phoneNumberInter.getNationalNumber());

//                    AppHelper.LogCat("phoneNumberInter " + phoneNumberTmpFinal);
                    //      AppHelper.LogCat("PhoneNo " + phoneNumber);

                    // AppHelper.LogCat("phoneNumberTmpFinal --> " + phoneNumberTmpFinal);
                    //    if (isValid) {
                    ContactsModel contactsModel = new ContactsModel();
                    //    AppHelper.LogCat("PhoneNo --> " + PhoneNo);
                    contactsModel.setPhoneTmp(phoneNumberTmpFinal);
                    contactsModel.setPhone(phoneNumber.trim());
                    contactsModel.setUsername(c.first_name + " " + c.last_name);
                    contactsModel.setContactID(c.id);
                    contactsModel.setImage(c.user_image);

                    int flag = 0;
                    int arraySize = mListContacts.size();
                    if (arraySize == 0) {
                        if (!phoneNumber.equals(PreferenceManager.getInstance().getPhone(WhatsCloneApplication.getInstance())))
                            mListContacts.add(contactsModel);
                    }
                    //remove duplicate numbers
                    for (int i = 0; i < arraySize; i++) {

                        if (!mListContacts.get(i).getPhone().trim().equals(phoneNumber.trim())) {
                            flag = 1;

                        } else {
                            flag = 0;
                            break;
                        }
                    }

                    if (flag == 1) {
                        if (!phoneNumber.equals(PreferenceManager.getInstance().getPhone(WhatsCloneApplication.getInstance())))
                            mListContacts.add(contactsModel);
                    }


                   *//* } else {
                        //   AppHelper.LogCat("invalid phone --> ");
                    }*//*
                }
            }


        }
                *//*}
                cur.close();
            }
        }*//*
        AppHelper.LogCat("mListContacts " + mListContacts.size());
        return mListContacts;
    }*/

    /**
     * method to retrieve all contacts from the book
     *
     * @return return value
     */
    public ArrayList<UsersModel> GetPhoneContacts() {
        ContentResolver contentResolver = WhatsCloneApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.LABEL, ContactsContract.CommonDataKinds.Phone.PHOTO_URI, ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY}, null, null, null);

        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    UsersModel contactsModel = new UsersModel();
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    String image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


                    //     AppHelper.LogCat("number phone --> " + phoneNumber);
                    if (name.contains("\\s+")) {
                        String[] nameArr = name.split("\\s+");
                        contactsModel.setUsername(nameArr[0] + nameArr[1]);
                        // AppHelper.LogCat("Fname --> " + nameArr[0]);
                        // AppHelper.LogCat("Lname --> " + nameArr[1]);
                    } else {
                        contactsModel.setUsername(name);
                        //AppHelper.LogCat("name" + name);
                    }
                    if (phoneNumber != null) {

                        String Regex = "[^\\d]";
                        String PhoneDigits = phoneNumber.replaceAll(Regex, "");
                        boolean isValid = !(PhoneDigits.length() < 6 || PhoneDigits.length() > 13);
                        String phNumberProto = PhoneDigits.replaceAll("-", "");
                        String PhoneNo;
                        if (PhoneDigits.length() != 10) {
                            PhoneNo = "+";
                            PhoneNo = PhoneNo.concat(phNumberProto);
                        } else {
                            PhoneNo = phNumberProto;
                        }
                        // AppHelper.LogCat("phoneNumber --> " + phoneNumber);
                        String phoneNumberTmpFinal;
                        Phonenumber.PhoneNumber phoneNumberInter = getPhoneNumber(phoneNumber);
                        if (phoneNumberInter != null) {
                            //  AppHelper.LogCat("phoneNumberInter --> " + phoneNumberInter.getNationalNumber());
                            phoneNumberTmpFinal = String.valueOf(phoneNumberInter.getNationalNumber());

                            // AppHelper.LogCat("phoneNumberTmpFinal --> " + phoneNumberTmpFinal);
                            if (isValid) {
                                //    AppHelper.LogCat("PhoneNo --> " + PhoneNo);
                                contactsModel.setPhone_qurey(phoneNumberTmpFinal);
                                contactsModel.setPhone(PhoneNo.trim());
                                contactsModel.setContactId(Integer.parseInt(id));
                                contactsModel.setImage(image_uri);

                                int flag = 0;
                                int arraySize = mListContacts.size();
                                if (arraySize == 0) {
                                    mListContacts.add(contactsModel);
                                }
                                //remove duplicate numbers
                                for (int i = 0; i < arraySize; i++) {

                                    if (!mListContacts.get(i).getPhone().trim().equals(PhoneNo.trim())) {
                                        flag = 1;

                                    } else {
                                        flag = 0;
                                        break;
                                    }
                                }

                                if (flag == 1) {
                                    mListContacts.add(contactsModel);
                                }


                            } else {
                                //   AppHelper.LogCat("invalid phone --> ");
                            }
                        }


                    }
                }
                cur.close();
            }
        }
        AppHelper.LogCat("mListContacts " + mListContacts.size());
        return mListContacts;
    }


    public static class Contact {
        public int id;
        public ArrayList<String> phones = new ArrayList<String>();
        public ArrayList<String> phoneTypes = new ArrayList<String>();
        public ArrayList<String> shortPhones = new ArrayList<String>();
        public ArrayList<Integer> phoneDeleted = new ArrayList<Integer>();
        public String first_name;
        public String last_name;
        public String user_image;
    }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid(String phone) {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber(phone);
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber(String phone) {
        final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
        try {
            return mPhoneUtil.parse(phone, DEFAULT_COUNTRY);
        } catch (NumberParseException ignored) {
            return null;
        }
    }

    /**
     * method to get contact ID
     *
     * @param mActivity this is the first parameter for getContactID  method
     * @param phone     this is the second parameter for getContactID  method
     * @return return value
     */
    public static long getContactID(Activity mActivity, String phone) {
        if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)) {
            AppHelper.LogCat("Read contact data permission already granted.");
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mActivity.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            long idPhone = 0;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        idPhone = Long.valueOf(mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                    }
                }
            } finally {
                mcursor.close();
            }
            return idPhone;
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");

            return 0;
        }

    }


    /**
     * method to check for contact name
     *
     * @param phone this is the second parameter for getContactName  method
     * @return return value
     */
    @SuppressLint("CheckResult")
    public static String getContactName(String phone) {

        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {

            try {

                // CONTENT_FILTER_URI allow to search contact by phone number
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
                // This query will return NAME and ID of contact, associated with phone //number.
                Cursor mcursor = WhatsCloneApplication.getInstance().getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
                //Now retrieve _ID from query result
                String name = null;
                try {
                    if (mcursor != null) {
                        if (mcursor.moveToFirst()) {
                            name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        }
                    }
                } finally {
                    mcursor.close();
                }


                if (name == null)
                    subscriber.onNext(phone);
                else
                    subscriber.onNext(name);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).blockingFirst();

    }

    /**
     * method to check if user contact exist
     *
     * @param phone this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String phone) {
        try {
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            String name = null;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                mcursor.close();
            }

            return name != null;
        } catch (Exception e) {
            AppHelper.LogCat(e);
            return false;
        }
    }
}
