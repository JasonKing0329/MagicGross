package com.king.app.gross.page.bean;

import com.king.app.gross.model.entity.MovieRating;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/19 16:48
 */
public class RatingData {

    private String score;
    private String person;
    private MovieRating rating;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public MovieRating getRating() {
        return rating;
    }

    public void setRating(MovieRating rating) {
        this.rating = rating;
    }
}
