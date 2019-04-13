package com.king.app.gross.utils;

import android.content.pm.PackageManager;
import android.os.Build;

import com.king.app.gross.base.MApplication;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 10:06
 */
public class AppUtil {

    public static String getAppVersionName() {
        try {
            return MApplication.getInstance().getPackageManager().getPackageInfo(MApplication.getInstance().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * android P (9.0)
     * @return
     */
    public static boolean isAndroidP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return true;
        }
        return false;
    }
}
