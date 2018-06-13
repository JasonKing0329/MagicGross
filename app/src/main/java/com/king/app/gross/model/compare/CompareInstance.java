package com.king.app.gross.model.compare;

import com.king.app.gross.model.entity.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/13 10:08
 */
public class CompareInstance {

    private static CompareInstance instance;

    public static CompareInstance getInstance() {
        if (instance == null) {
            instance = new CompareInstance();
        }
        return instance;
    }

    private List<Movie> movieList;

    private CompareInstance() {
        movieList = new ArrayList<>();
    }

    public void destroy() {
        instance = null;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

    public void addCompareMovie(Movie data) {
        for (Movie movie:movieList) {
            if (movie.getId() == data.getId()) {
                return;
            }
        }
        movieList.add(data);
    }

    public void removeCompareMovie(Movie data) {
        for (int i = 0; i < movieList.size(); i ++) {
            Movie movie = movieList.get(i);
            if (movie.getId() == data.getId()) {
                movieList.remove(i);
                break;
            }
        }
    }
}
