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
}
