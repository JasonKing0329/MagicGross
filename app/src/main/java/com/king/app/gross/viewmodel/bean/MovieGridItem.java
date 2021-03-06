package com.king.app.gross.viewmodel.bean;

import com.king.app.gross.model.entity.Movie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/13 16:38
 */
public class MovieGridItem {

    private Movie bean;

    private String name;

    private String subName;

    private String chnName;

    private String date;

    private String gross;

    private String flag;

    private String grossUs;

    private String grossCn;

    private String grossWorld;

    private int indexColor;

    private long grossUsNum;
    private long grossCnNum;
    private long grossWorldNum;

    private String imageUrl;

    public long getGrossUsNum() {
        return grossUsNum;
    }

    public void setGrossUsNum(long grossUsNum) {
        this.grossUsNum = grossUsNum;
    }

    public long getGrossCnNum() {
        return grossCnNum;
    }

    public void setGrossCnNum(long grossCnNum) {
        this.grossCnNum = grossCnNum;
    }

    public long getGrossWorldNum() {
        return grossWorldNum;
    }

    public void setGrossWorldNum(long grossWorldNum) {
        this.grossWorldNum = grossWorldNum;
    }

    public String getGrossUs() {
        return grossUs;
    }

    public void setGrossUs(String grossUs) {
        this.grossUs = grossUs;
    }

    public String getGrossCn() {
        return grossCn;
    }

    public void setGrossCn(String grossCn) {
        this.grossCn = grossCn;
    }

    public String getGrossWorld() {
        return grossWorld;
    }

    public void setGrossWorld(String grossWorld) {
        this.grossWorld = grossWorld;
    }
    public Movie getBean() {
        return bean;
    }

    public void setBean(Movie bean) {
        this.bean = bean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getChnName() {
        return chnName;
    }

    public void setChnName(String chnName) {
        this.chnName = chnName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getIndexColor() {
        return indexColor;
    }

    public void setIndexColor(int indexColor) {
        this.indexColor = indexColor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
