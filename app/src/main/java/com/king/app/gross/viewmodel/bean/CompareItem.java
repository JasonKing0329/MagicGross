package com.king.app.gross.viewmodel.bean;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/13 10:02
 */
public class CompareItem {
    private String key;
    private List<String> values;
    private List<SimpleGross> grossList;
    private int winIndex;
    private boolean isDay;
    private boolean isTotal;
    private Object bean;
    private String imageUrl;
    private boolean isGroup;

    public CompareItem() {
        winIndex = -1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public int getWinIndex() {
        return winIndex;
    }

    public void setWinIndex(int winIndex) {
        this.winIndex = winIndex;
    }

    public boolean isDay() {
        return isDay;
    }

    public void setDay(boolean day) {
        isDay = day;
    }

    public boolean isTotal() {
        return isTotal;
    }

    public void setTotal(boolean total) {
        isTotal = total;
    }

    public List<SimpleGross> getGrossList() {
        return grossList;
    }

    public void setGrossList(List<SimpleGross> grossList) {
        this.grossList = grossList;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
