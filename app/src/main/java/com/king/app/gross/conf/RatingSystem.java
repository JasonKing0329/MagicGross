package com.king.app.gross.conf;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/19 14:12
 */
public class RatingSystem {

    public static final long IMDB = 1000;
    public static final long ROTTEN_PRO = 2000;
    public static final long ROTTEN_AUD = 2001;
    public static final long META = 3000;
    public static final long DOUBAN = 4000;
    public static final long MAOYAN = 5000;
    public static final long TAOPP = 6000;

    /**
     * 烂番茄烂的标准是60以下
     */
    public static final long ROTTEN_SCORE_ROTTEN = 60;

    /**
     * Metacritic的标准
     * 61-100 绿色
     * 40-60 黄色
     * 0-39 红色
     */
    public static final long META_GREEN = 61;
    public static final long META_YELLOW = 40;
}
