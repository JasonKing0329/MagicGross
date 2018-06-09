package com.king.app.gross.viewmodel.bean;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/9 0009 11:10
 */

public class WeekGross {

    private String week;

    private String dayRange;

    private String gross;

    private String drop;

    private String grossSum;

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getDrop() {
        return drop;
    }

    public void setDrop(String drop) {
        this.drop = drop;
    }

    public String getGrossSum() {
        return grossSum;
    }

    public void setGrossSum(String grossSum) {
        this.grossSum = grossSum;
    }

    public String getDayRange() {
        return dayRange;
    }

    public void setDayRange(String dayRange) {
        this.dayRange = dayRange;
    }
}
