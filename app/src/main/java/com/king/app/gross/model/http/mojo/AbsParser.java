package com.king.app.gross.model.http.mojo;

import android.os.Build;

import java.util.Locale;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/5/15 14:41
 */
public abstract class AbsParser {

    public String getLocalUserAgent() {
        StringBuffer buffer = new StringBuffer();

        final String version = Build.VERSION.RELEASE;
        if (version.length() > 0) {
            if (Character.isDigit(version.charAt(0))) {
                buffer.append(version);
            }
            else {
                buffer.append("4.1.1");
            }
        }
        else {
            buffer.append("1.0");
        }
        buffer.append("; ");
        Locale locale = Locale.getDefault();
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(convertObsoleteLanguageCodeToNew(language));
            String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country.toLowerCase());
            }
        }
        else {
            buffer.append("en");
        }
        buffer.append(";");
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            buffer.append(" ");
            buffer.append(model);
        }
        String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        String mobile = "Mobile ";
        String base = "Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 %s Safara/534.30";
        return String.format(base, buffer, mobile);
    }

    private static String convertObsoleteLanguageCodeToNew(String lan) {
        if (lan == null) {
            return null;
        }
        if ("iw".equals(lan)) {
            return "he";
        }
        else if ("in".equals(lan)) {
            return "id";
        }
        else if ("ji".equals(lan)) {
            return "yi";
        }
        return lan;
    }

}
