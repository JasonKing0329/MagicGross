package com.king.app.gross.conf;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/3/26 14:04
 */
public class AppConstants {

    public static final int MOVIE_REAL = 1;
    public static final int MOVIE_VIRTUAL = 0;

    public static final int MOVIE_SORT_DATE = 1;
    public static final int MOVIE_SORT_NAME = 0;
    public static final int MOVIE_SORT_NA = 2;
    public static final int MOVIE_SORT_CHN = 3;
    public static final int MOVIE_SORT_WW = 4;

    public static final int GROSS_IS_TOTAL = 1;
    public static final int GROSS_IS_OPENING = 2;

    public static final int MARKET_GROSS_SORT_MARKET = 0;
    public static final int MARKET_GROSS_SORT_TOTAL = 1;
    public static final int MARKET_GROSS_SORT_OPENING = 2;
    public static final int MARKET_GROSS_SORT_DEBUT = 3;

    public static final String[] REGION_TITLES = new String[] {
            "North America", "China", "Oversea except China", "Oversea", "World Wide", "Market"
    };

    public static final String[] RANK_TYPE_TITLES = new String[] {
            "Total", "Opening", "后劲指数", "首周", "第二周", "第三周", "第四周", "第五周", "余量", "10天", "20天", "30天", "Budget", "最高单日"
    };

    public static final String COMPARE_TITLE_WEEK = "Week ";
    public static final String COMPARE_TITLE_LEFT = "Left";
}
