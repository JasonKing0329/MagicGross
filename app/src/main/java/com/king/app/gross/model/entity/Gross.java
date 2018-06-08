package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 13:21
 */
@Entity(nameInDb = "gross")
public class Gross {

    @Id(autoincrement = true)
    private Long id;

    private long movieId;

    private int region;

    private int symbol;

    private int day;

    private int dayOfWeek;

    private long gross;

    private int isLeftAfterDay;

    @Generated(hash = 147928116)
    public Gross(Long id, long movieId, int region, int symbol, int day,
            int dayOfWeek, long gross, int isLeftAfterDay) {
        this.id = id;
        this.movieId = movieId;
        this.region = region;
        this.symbol = symbol;
        this.day = day;
        this.dayOfWeek = dayOfWeek;
        this.gross = gross;
        this.isLeftAfterDay = isLeftAfterDay;
    }

    @Generated(hash = 757174660)
    public Gross() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMovieId() {
        return this.movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public int getRegion() {
        return this.region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDayOfWeek() {
        return this.dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public long getGross() {
        return this.gross;
    }

    public void setGross(long gross) {
        this.gross = gross;
    }

    public int getIsLeftAfterDay() {
        return this.isLeftAfterDay;
    }

    public void setIsLeftAfterDay(int isLeftAfterDay) {
        this.isLeftAfterDay = isLeftAfterDay;
    }

    public int getSymbol() {
        return this.symbol;
    }

    public void setSymbol(int symbol) {
        this.symbol = symbol;
    }
}
