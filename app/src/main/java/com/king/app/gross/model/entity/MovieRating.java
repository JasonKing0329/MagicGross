package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/19 13:58
 */
@Entity(nameInDb = "movie_rating")
public class MovieRating {

    @Id(autoincrement = true)
    private Long id;

    private long movieId;

    private long systemId;

    private double score;

    private int person;

    private String updateDate;

    @Generated(hash = 558464284)
    public MovieRating(Long id, long movieId, long systemId, double score,
            int person, String updateDate) {
        this.id = id;
        this.movieId = movieId;
        this.systemId = systemId;
        this.score = score;
        this.person = person;
        this.updateDate = updateDate;
    }

    @Generated(hash = 11150201)
    public MovieRating() {
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

    public long getSystemId() {
        return this.systemId;
    }

    public void setSystemId(long systemId) {
        this.systemId = systemId;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPerson() {
        return this.person;
    }

    public void setPerson(int person) {
        this.person = person;
    }

    public String getUpdateDate() {
        return this.updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

}
