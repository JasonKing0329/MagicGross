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
    public static final int MOVIE_SORT_TOTAL = 5;
    public static final int MOVIE_SORT_OPENING = 6;

    public static final int GROSS_IS_TOTAL = 1;
    public static final int GROSS_IS_OPENING = 2;

    public static final int MARKET_GROSS_SORT_MARKET = 0;
    public static final int MARKET_GROSS_SORT_TOTAL = 1;
    public static final int MARKET_GROSS_SORT_OPENING = 2;
    public static final int MARKET_GROSS_SORT_DEBUT = 3;

    public static final int RATING_SORT_DEBUT = 0;
    public static final int RATING_SORT_NAME = 1;
    public static final int RATING_SORT_RATING = 2;
    public static final int RATING_SORT_PERSON = 3;
    public static final int RATING_SORT_RATING_PRO = 4;
    public static final int RATING_SORT_PERSON_PRO = 5;
    public static final int RATING_SORT_RATING_AUD = 6;
    public static final int RATING_SORT_PERSON_AUD = 7;

    public static final String[] REGION_TITLES = new String[] {
            "North America", "China", "Oversea except China", "Oversea", "World Wide", "Market"
    };

    public static final String[] RANK_TYPE_TITLES = new String[] {
            "Total", "Opening", "后劲指数", "首周", "第二周", "第三周", "第四周", "第五周", "余量", "10天", "20天", "30天", "Budget", "投资回报率", "最高单日"
    };

    public static final String COMPARE_TITLE_WEEK = "Week ";
    public static final String COMPARE_TITLE_LEFT = "Left";
    public static final String COMPARE_TITLE_TOTAL_CHN = "累计";
    public static final String COMPARE_TITLE_ZERO_CHN = "零点场";

    public static final int LABEL_STYLE = 0;
    public static final int LABEL_SERIES = 1;

    public static final String TAG_YEAR_ALL = "全部";
    public static final String TAG_YEAR_AFTER = "以后";

    public static final long MARKET_TOTAL_ID = 0;

    public static final String MARKET_UNDISCLOSED_NAME = "Undisclosed";
    public static final long MARKET_UNDISCLOSED_ID = -1;

    public static final int COMPARE_TYPE_DAILY = 0;
    public static final int COMPARE_TYPE_ACCU = 1;

}
