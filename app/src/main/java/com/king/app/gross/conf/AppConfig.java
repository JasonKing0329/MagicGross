package com.king.app.gross.conf;

import android.os.Environment;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/3/23 15:46
 */
public class AppConfig {
    public static final String DB_NAME = "gross.db";

    public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();

    public static final String DEF_CONTENT = SDCARD + "/gross";

    public static final String EXPORT_BASE = DEF_CONTENT + "/export";
    public static final String HISTORY_BASE = DEF_CONTENT + "/history";
    public static final String HTML_BASE = DEF_CONTENT + "/html";

    public static final String FILE_HTML_FOREIGN = HTML_BASE + "/foreign.html";

    public static final String[] DIRS = new String[] {
            DEF_CONTENT, EXPORT_BASE, HISTORY_BASE, HTML_BASE
    };
}
