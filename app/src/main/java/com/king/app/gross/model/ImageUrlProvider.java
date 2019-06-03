package com.king.app.gross.model;

import android.text.TextUtils;

import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.Movie;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/13 15:32
 */
public class ImageUrlProvider {

    public static String getMovieImageRandom(Movie movie) {
        return getMovieImageRandom(movie, new Random());
    }

    public static String getMovieImageRandom(Movie movie, Random random) {
        String url = null;
        String folder = AppConfig.IMG_MOVIE + "/" + movie.getName();
        if (!TextUtils.isEmpty(movie.getSubName())) {
            folder = folder + "_" + movie.getSubName();
        }
        File file = new File(folder);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                url = files[Math.abs(random.nextInt()) % files.length].getPath();
            }
        }
        return url;
    }

    public static String getMarketImage(Market market) {
        // 优先取中文名
        // 优先取country目录图片，其次取flag目录图片
        String name = market.getNameChn();
        if (TextUtils.isEmpty(name)) {
            name = market.getName();
        }
        if (!TextUtils.isEmpty(name)) {
            File folder = new File(AppConfig.RACE_IMG_COUNTRY + "/" + name);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles(new ImageFilter());
                if (files.length > 0) {
                    int index = Math.abs(new Random().nextInt()) % files.length;
                    return files[index].getPath();
                }
            }
            String[] extras = new String[]{".jpg", ".jpeg", ".png", ".bmp", ".gif"};
            for (String extra:extras) {
                File file = new File(AppConfig.RACE_IMG_FLAG + "/" + name + extra);
                if (file.exists()) {
                    return file.getPath();
                }
            }
        }
        return null;
    }

    public static String getMarketFlag(Market market) {
        // 优先取中文名
        // 然后取flag目录图片
        String name = market.getNameChn();
        if (TextUtils.isEmpty(name)) {
            name = market.getName();
        }
        if (!TextUtils.isEmpty(name)) {
            String[] extras = new String[]{".jpg", ".jpeg", ".png", ".bmp", ".gif"};
            for (String extra:extras) {
                File file = new File(AppConfig.RACE_IMG_FLAG + "/" + name + extra);
                if (file.exists()) {
                    return file.getPath();
                }
            }
        }
        return null;
    }

    public static class ImageFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            String[] extras = new String[]{".jpg", ".jpeg", ".png", ".bmp", ".gif"};
            for (String extra:extras) {
                if (file.getName().endsWith(extra)) {
                    return true;
                }
            }
            return false;
        }
    }
}
