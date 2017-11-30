package com.g5team.healthtracking.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Toof on 11/20/2017.
 */

public class SessionManager {
    // Shared preferences file name
    private static final String PREF_NAME = "Login";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    private static String TOKEN_TYPE = "TOKEN_TYPE";
    private static String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static String EMAIL = "EMAIL";
    private static String FULLNAME = "FULLNAME";
    private static String FIRST = "FIRST";
    private static String HEIGHT = "HEIGHT";
    private static String WEIGHT = "WEIGHT";
    private static String DOB = "DOB";
    private static String AGE = "AGE";
    private static String SEX = "SEX";
    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }



    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);


        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }
    public void setFirst(){
        editor.putBoolean(FIRST, AppConfig.FIRST);
        editor.commit();
    }
    public void setProfile(){
        editor.putString(EMAIL, AppConfig.EMAIL);
        editor.putString(FULLNAME, AppConfig.FULLNAME);


        editor.putString(DOB, AppConfig.DOB);
        editor.putBoolean(SEX, AppConfig.SEX);
        editor.putInt(AGE,  AppConfig.AGE);
        editor.commit();
    }
    public void setToken(){
        editor.putString(TOKEN_TYPE, AppConfig.TOKEN_TYPE);
        editor.putString(ACCESS_TOKEN, AppConfig.ACCESS_TOKEN);
        editor.putString(REFRESH_TOKEN, AppConfig.REFRESH_TOKEN);
        editor.commit();

    }
    public void setWH(){
        editor.putInt(WEIGHT, AppConfig.WEIGTH);
        editor.putInt(HEIGHT, AppConfig.HEIGHT);
        editor.commit();

    }
    public boolean getFirst(){
        return pref.getBoolean(FIRST, true);
    }
    public int getHeight(){
        return pref.getInt(HEIGHT, 0);
    }
    public int getWeight(){
        return pref.getInt(WEIGHT, 0);
    }
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public String getType(){
        return pref.getString(TOKEN_TYPE, "");
    }
    public String getKey(){
        return pref.getString(ACCESS_TOKEN,"");
    }
    public String getName() {
        return pref.getString(FULLNAME,"");
    }
    public String getRefreshToken(){return pref.getString(REFRESH_TOKEN,"");}
    public String getEmail() {
        return pref.getString(EMAIL,"");
    }
    public String getDob(){return pref.getString(DOB, "");}
    public int getAge(){return pref.getInt(AGE, 0);}
    public boolean getSex(){return pref.getBoolean(SEX, false);}
}