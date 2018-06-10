package com.king.app.gross.model.gross;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/9 0009 22:00
 */

public class WeekendGross {

    private int week;

    private int dayStart;

    private int dayEnd;

    private long gross;

    private int leftAfterDay;

    private int region;

    private int symbol;

    private long totalGross;

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getDayStart() {
        return dayStart;
    }

    public void setDayStart(int dayStart) {
        this.dayStart = dayStart;
    }

    public int getDayEnd() {
        return dayEnd;
    }

    public void setDayEnd(int dayEnd) {
        this.dayEnd = dayEnd;
    }

    public long getGross() {
        return gross;
    }

    public void setGross(long gross) {
        this.gross = gross;
    }

    public int getLeftAfterDay() {
        return leftAfterDay;
    }

    public void setLeftAfterDay(int leftAfterDay) {
        this.leftAfterDay = leftAfterDay;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public int getSymbol() {
        return symbol;
    }

    public void setSymbol(int symbol) {
        this.symbol = symbol;
    }

    public long getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(long totalGross) {
        this.totalGross = totalGross;
    }
}
