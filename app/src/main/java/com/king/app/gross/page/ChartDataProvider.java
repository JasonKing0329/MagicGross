package com.king.app.gross.page;

import com.king.app.gross.conf.Region;
import com.king.app.gross.model.compare.CompareChart;
import com.king.app.gross.model.entity.Movie;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/9 17:40
 */
public class ChartDataProvider {

    private static CompareChart chartData;

    private static List<Movie> movieList;

    private static List<Integer> colorList;


    private static Region region;

    public static void setChartData(CompareChart chartData) {
        ChartDataProvider.chartData = chartData;
    }

    public static CompareChart getChartData() {
        return chartData;
    }

    public static void setMovieList(List<Movie> movieList) {
        ChartDataProvider.movieList = movieList;
    }

    public static List<Movie> getMovieList() {
        return movieList;
    }

    public static void release() {
        chartData = null;
        movieList = null;
        region = null;
        colorList = null;
    }

    public static Region getRegion() {
        return region;
    }

    public static void setRegion(Region region) {
        ChartDataProvider.region = region;
    }

    public static List<Integer> getColorList() {
        return colorList;
    }

    public static void setColorList(List<Integer> colorList) {
        ChartDataProvider.colorList = colorList;
    }
}
