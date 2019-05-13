package com.king.app.gross.model;

import android.text.TextUtils;

import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.model.entity.Movie;

import java.io.File;
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

}
