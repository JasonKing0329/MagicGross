package com.king.app.gross.viewmodel.bean;

import com.king.app.gross.model.entity.Gross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 16:44
 */
public class SimpleGross {

    private int day;
    private int dayOfWeek;
    private String grossDay;
    private String dropDay;
    private String dropWeek;
    private String grossSum;

    private Gross bean;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getGrossDay() {
        return grossDay;
    }

    public void setGrossDay(String grossDay) {
        this.grossDay = grossDay;
    }

    public String getDropDay() {
        return dropDay;
    }

    public void setDropDay(String dropDay) {
        this.dropDay = dropDay;
    }

    public String getDropWeek() {
        return dropWeek;
    }

    public void setDropWeek(String dropWeek) {
        this.dropWeek = dropWeek;
    }

    public String getGrossSum() {
        return grossSum;
    }

    public void setGrossSum(String grossSum) {
        this.grossSum = grossSum;
    }

    public Gross getBean() {
        return bean;
    }

    public void setBean(Gross bean) {
        this.bean = bean;
    }
}
