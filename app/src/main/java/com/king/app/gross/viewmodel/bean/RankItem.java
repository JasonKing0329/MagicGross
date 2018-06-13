package com.king.app.gross.viewmodel.bean;

import com.king.app.gross.model.entity.Movie;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 20:50
 */

public class RankItem {

    private String rank;
    private String name;
    private String value;
    private String rate;
    private Movie movie;
    private double sortValue;
    private String year;

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public double getSortValue() {
        return sortValue;
    }

    public void setSortValue(double sortValue) {
        this.sortValue = sortValue;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
