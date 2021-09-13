package com.lapluma.knowledg.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lapluma.knowledg.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class MainPref {
    /** main shared preferences. most are user authorization related.
     * shared preferences: easily store and load data through xml.
     * */
    private Context context;
    private SharedPreferences sharedPreferences;

    /** these are from the original framework, as refs */
    private static final String FIRST_LAUNCH = "_.FIRST_LAUNCH";
    private static final String CLICK_OFFER = "_.MAX_CLICK_OFFER";
    private static final String CLICK_INTERS = "_.MAX_CLICK_INTERS";
    private static final String CLICK_SWITCH = "_.MAX_CLICK_SWITCH";
    private static final String NEED_REGISTER = "_.NEED_REGISTER";
    private static final String FCM_PREF_KEY = "_.FCM_PREF_KEY";

    /** these are added by delphynium */
    private static final String SIGNED_IN = "_.SIGNED_IN";
    private static final String USERNAME = "_.USERNAME";
    private static final String TOKEN = "_.TOKEN";

    /** these are added by nldxtd */
    private static final String AVA_LIST = "._AVA_LIST";
    private static final String UNAVA_LIST = "._UNAVA_LIST";

    private static final int MAX_CLICK_OFFER = BuildConfig.DEBUG ? Integer.MAX_VALUE : 10;
    private static final int MAX_CLICK_INTERS = BuildConfig.DEBUG ? Integer.MAX_VALUE : 10;

    public MainPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("PREF_MAIN", Context.MODE_PRIVATE);
    }

    public void clearAll() {
        /** only for debugging */
        sharedPreferences.edit().clear().apply();
    }
    public void setSignedIn(boolean flag) {
        sharedPreferences.edit().putBoolean(SIGNED_IN, flag).apply();
    }

    public boolean isSignedIn() {
        return sharedPreferences.getBoolean(SIGNED_IN, false); // 2nd arg is default value
    }

    public void setUsername(String s) {
        sharedPreferences.edit().putString(USERNAME, s).apply();
    }

    public void setToken(String s) {
        sharedPreferences.edit().putString(TOKEN, s).apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(USERNAME, "");
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN, "");
    }

    public ArrayList<String> getList() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("chinese");
        titles.add("math");
        titles.add("english");
        titles.add("physics");
        titles.add("chemistry");
        titles.add("geo");
        titles.add("biology");
        titles.add("history");
        titles.add("politics");
        Gson gson = new Gson();
        String defaultString = gson.toJson(titles);
        String jsonString = sharedPreferences.getString(AVA_LIST, defaultString);
        ArrayList<String> returnTitles = gson.fromJson(jsonString, titles.getClass());
        return returnTitles;
    }

    public void setList(ArrayList<String> items) {
        Gson gson = new Gson();
        String str = gson.toJson(items);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AVA_LIST, str);
        editor.commit();
    }

    public ArrayList<String> getUnavaiList() {
        ArrayList<String> titles = new ArrayList<>();
        Gson gson = new Gson();
        String defaultString = gson.toJson(titles);
        String jsonString = sharedPreferences.getString(UNAVA_LIST, defaultString);
        ArrayList<String> returnTitles = gson.fromJson(jsonString, titles.getClass());
        return returnTitles;
    }

    public void setUnavaList(ArrayList<String> items) {
        Gson gson = new Gson();
        String str = gson.toJson(items);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UNAVA_LIST, str);
        editor.commit();
    }

    public void setFirstLaunch(boolean flag) {
        sharedPreferences.edit().putBoolean(FIRST_LAUNCH, flag).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }

    public boolean actionClickOffer() {
        int current = sharedPreferences.getInt(CLICK_OFFER, 1);
        boolean is_reset = false;
        if (current < MAX_CLICK_OFFER) {
            current++;
        } else {
            current = 1;
            is_reset = true;
        }
        sharedPreferences.edit().putInt(CLICK_OFFER, current).apply();
        return is_reset;
    }

    public boolean actionClickInters() {
        int current = sharedPreferences.getInt(CLICK_INTERS, 1);
        boolean is_reset = false;
        if (current < MAX_CLICK_INTERS) {
            current++;
        } else {
            current = 1;
            is_reset = true;
        }
        sharedPreferences.edit().putInt(CLICK_INTERS, current).apply();
        return is_reset;
    }

    public boolean getClickSwitch() {
        return sharedPreferences.getBoolean(CLICK_SWITCH, true);
    }

    public void setClickSwitch(Boolean val) {
        sharedPreferences.edit().putBoolean(CLICK_SWITCH, val).apply();
    }

    public void setFcmRegId(String fcmRegId) {
        sharedPreferences.edit().putString(FCM_PREF_KEY, fcmRegId).apply();
    }

    public String getFcmRegId() {
        return sharedPreferences.getString(FCM_PREF_KEY, null);
    }

    public void setNeedRegister(boolean value) {
        sharedPreferences.edit().putBoolean(NEED_REGISTER, value).apply();
    }

    public boolean isNeedRegister() {
        return sharedPreferences.getBoolean(NEED_REGISTER, true);
    }

    public void setSubscibeNotif(boolean value) {
        sharedPreferences.edit().putBoolean("SUBSCRIBE_NOTIF", value).apply();
    }

    public boolean isSubscibeNotif() {
        return sharedPreferences.getBoolean("SUBSCRIBE_NOTIF", false);
    }

}
