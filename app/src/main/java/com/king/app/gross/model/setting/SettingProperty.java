package com.king.app.gross.model.setting;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.king.app.gross.base.MApplication;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 14:40
 */
public class SettingProperty {

    private static final String getString(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        return sp.getString(key, "");
    }

    private static final void setString(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static final int getInt(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        return sp.getInt(key, -1);
    }

    private static final int getInt(String key, int defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        return sp.getInt(key, defaultValue);
    }

    private static final void setInt(String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static final long getLong(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        return sp.getLong(key, -1);
    }

    private static final void setLong(String key, long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static final boolean getBoolean(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        return sp.getBoolean(key, false);
    }

    private static final void setBoolean(String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setEnableFingerPrint(boolean enable) {
        setBoolean("enable_finger_print", enable);
    }

    public static boolean isEnableFingerPrint() {
        return getBoolean("enable_finger_print");
    }

    public static void setEditGrossPref(EditGrossPref pref) {
        setString("pref_edit_gross", new Gson().toJson(pref));
    }

    public static EditGrossPref getEditGrossPref() {
        String pref = getString("pref_edit_gross");
        EditGrossPref bean = null;
        try {
            bean = new Gson().fromJson(pref, EditGrossPref.class);
        } catch (Exception e) { }
        if (bean == null) {
            bean = new EditGrossPref();
        }
        return bean;
    }

    public static void setRegionTypeInMovieList(int enable) {
        setInt("region_type_in_movie_list", enable);
    }

    public static int getRegionTypeInMovieList() {
        return getInt("region_type_in_movie_list", 0);
    }
}
