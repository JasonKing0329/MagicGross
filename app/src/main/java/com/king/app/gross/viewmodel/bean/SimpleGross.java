package com.king.app.gross.viewmodel.bean;

import com.king.app.gross.model.entity.Gross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 16:44
 */
public class SimpleGross {

    private String day;
    private String dayOfWeek;
    private String grossDay;
    private String dropDay;
    private String dropWeek;
    private String grossSum;
    private long grossValue;
    private long grossSumValue;
    private boolean isLeft;

    private Gross bean;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
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

    public long getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(long grossValue) {
        this.grossValue = grossValue;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setLeft(boolean left) {
        isLeft = left;
    }

    public long getGrossSumValue() {
        return grossSumValue;
    }

    public void setGrossSumValue(long grossSumValue) {
        this.grossSumValue = grossSumValue;
    }
}
