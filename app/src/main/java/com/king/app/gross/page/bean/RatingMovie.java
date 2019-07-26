package com.king.app.gross.page.bean;

import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieRating;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:31
 */
public class RatingMovie {

    private String score;
    private String person;
    private String imageUrl;
    private Movie movie;
    private MovieRating rating;
    private String name;

    private RatingData rottenPro;

    private RatingData rottenAud;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public MovieRating getRating() {
        return rating;
    }

    public void setRating(MovieRating rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RatingData getRottenPro() {
        return rottenPro;
    }

    public void setRottenPro(RatingData rottenPro) {
        this.rottenPro = rottenPro;
    }

    public RatingData getRottenAud() {
        return rottenAud;
    }

    public void setRottenAud(RatingData rottenAud) {
        this.rottenAud = rottenAud;
    }
}
