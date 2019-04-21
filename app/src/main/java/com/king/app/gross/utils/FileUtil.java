package com.king.app.gross.utils;

import android.database.sqlite.SQLiteDatabase;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 14:29
 */
public class FileUtil {

    /**
     * 从assets目录复制的方法
     * @param dbFile
     */
    public static void copyDbFromAssets(String dbFile) {

        SQLiteDatabase db = null;
        //先检查是否存在，不存在才复制
        String dbPath = MApplication.getInstance().getFilesDir().getParent() + "/databases";
        try {
            db = SQLiteDatabase.openDatabase(dbPath + "/" + dbFile
                    , null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            db = null;
        }
        if (db == null) {
            try {
                InputStream assetsIn = MApplication.getInstance().getAssets().open(dbFile);
                File file = new File(dbPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                OutputStream fileOut = new FileOutputStream(dbPath + "/" + dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = assetsIn.read(buffer))>0){
                    fileOut.write(buffer, 0, length);
                }

                fileOut.flush();
                fileOut.close();
                assetsIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (db != null) {
            db.close();
        }
    }

    public static boolean replaceDatabase(File source) {
        if (source == null || !source.exists()) {
            return false;
        }

        // 删除源目录database
        String dbPath = MApplication.getInstance().getFilesDir().getParent() + "/databases";
        File targetFolder = new File(dbPath);
        if (targetFolder.exists()) {
            File[] files = targetFolder.listFiles();
            for (File f:files) {
                f.delete();
            }
        }
        try {
            InputStream in = new FileInputStream(source);
            File file = new File(dbPath + "/" + AppConfig.DB_NAME);
            OutputStream fileOut = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer))>0){
                fileOut.write(buffer, 0, length);
            }

            fileOut.flush();
            fileOut.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static File saveFile(InputStream inputStream, String path) {
        File file = new File(path);

        OutputStream outputStream = null;

        try {
            byte[] fileReader = new byte[4096];
            outputStream = new FileOutputStream(file);
            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
            }
            outputStream.flush();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
