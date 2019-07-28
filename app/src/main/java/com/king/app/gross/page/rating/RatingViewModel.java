package com.king.app.gross.page.rating;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.entity.MovieRating;
import com.king.app.gross.model.entity.MovieRatingDao;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.page.bean.RatingData;
import com.king.app.gross.page.bean.RatingMovie;
import com.king.app.gross.utils.FormatUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:45
 */
public class RatingViewModel extends BaseViewModel {

    public MutableLiveData<List<RatingMovie>> ratedMovies = new MutableLiveData<>();
    public MutableLiveData<List<RatingMovie>> unratedMovies = new MutableLiveData<>();

    private long systemId;
    private int mSortType;

    public RatingViewModel(@NonNull Application application) {
        super(application);
        mSortType = AppConstants.RATING_SORT_RATING;
    }

    public boolean isRottenSystem() {
        return systemId == RatingSystem.ROTTEN_PRO;
    }

    public void loadRatings(long systemId) {
        this.systemId = systemId;
        getPageData(systemId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Boolean> getPageData(long systemId) {
        if (isRottenSystem()) {
            return getRottenData();
        }
        else {
            return getData(systemId);
        }
    }

    private List<Movie> getAllMovies() {
        QueryBuilder<Movie> builder = getDaoSession().getMovieDao().queryBuilder();
        if (!SettingProperty.isEnableVirtualMovie()) {
            builder.where(MovieDao.Properties.IsReal.eq(AppConstants.MOVIE_REAL));
        }
        List<Movie> movies = builder.build().list();
        return movies;
    }

    private Observable<Boolean> getRottenData() {
        return Observable.create(e -> {
            List<MovieRating> pros = getDaoSession().getMovieRatingDao().queryBuilder()
                    .where(MovieRatingDao.Properties.SystemId.eq(RatingSystem.ROTTEN_PRO))
                    .where(MovieRatingDao.Properties.Score.gt(0))
                    .build().list();
            Map<Long, MovieRating> proMap = new HashMap<>();
            for (MovieRating rating:pros) {
                proMap.put(rating.getMovieId(), rating);
            }
            List<MovieRating> auds = getDaoSession().getMovieRatingDao().queryBuilder()
                    .where(MovieRatingDao.Properties.SystemId.eq(RatingSystem.ROTTEN_AUD))
                    .where(MovieRatingDao.Properties.Score.gt(0))
                    .build().list();
            Map<Long, MovieRating> audsMap = new HashMap<>();
            for (MovieRating rating:auds) {
                audsMap.put(rating.getMovieId(), rating);
            }

            List<Movie> movies = getAllMovies();
            List<RatingMovie> ratedList = new ArrayList<>();
            List<RatingMovie> unRatedList = new ArrayList<>();
            for (Movie movie:movies) {
                RatingMovie rm = new RatingMovie();
                rm.setMovie(movie);
                if (TextUtils.isEmpty(movie.getSubName())) {
                    rm.setName(movie.getName());
                }
                else {
                    rm.setName(movie.getName() + ": " + movie.getSubName());
                }
                rm.setImageUrl(ImageUrlProvider.getMovieImageRandom(movie));
                MovieRating pro = proMap.get(movie.getId());
                MovieRating aud = audsMap.get(movie.getId());
                if (aud == null && pro == null) {
                    unRatedList.add(rm);
                }
                else {
                    if (pro != null) {
                        RatingData data = new RatingData();
                        data.setScore(FormatUtil.formatNumber(pro.getScore()) + "%");
                        data.setPerson(FormatUtil.formatDivideNumber(pro.getPerson()));
                        data.setRating(pro);
                        rm.setRottenPro(data);
                    }
                    if (aud != null) {
                        RatingData data = new RatingData();
                        data.setScore(FormatUtil.formatNumber(aud.getScore()) + "%");
                        data.setPerson(FormatUtil.formatDivideNumber(aud.getPerson()));
                        data.setRating(aud);
                        rm.setRottenAud(data);
                    }
                    ratedList.add(rm);
                }
            }

            if (mSortType == AppConstants.RATING_SORT_DEBUT) {
                Collections.sort(ratedList, new DebutComparator());
            }
            else if (mSortType == AppConstants.RATING_SORT_NAME) {
                Collections.sort(ratedList, new NameComparator());
            }
            else {
                Collections.sort(ratedList, new RottenProComparator(mSortType));
            }
            Collections.sort(unRatedList, new DebutComparator());

            ratedMovies.postValue(ratedList);
            unratedMovies.postValue(unRatedList);
            e.onNext(true);
        });
    }

    private Observable<Boolean> getData(long systemId) {
        return Observable.create(e -> {
            List<MovieRating> ratings = getDaoSession().getMovieRatingDao().queryBuilder()
                    .where(MovieRatingDao.Properties.SystemId.eq(systemId))
                    .where(MovieRatingDao.Properties.Score.gt(0))
                    .build().list();
            Map<Long, MovieRating> ratingMap = new HashMap<>();
            for (MovieRating rating:ratings) {
                ratingMap.put(rating.getMovieId(), rating);
            }

            List<Movie> movies = getAllMovies();
            List<RatingMovie> ratedList = new ArrayList<>();
            List<RatingMovie> unRatedList = new ArrayList<>();
            for (Movie movie:movies) {
                RatingMovie rm = new RatingMovie();
                rm.setMovie(movie);
                if (TextUtils.isEmpty(movie.getSubName())) {
                    rm.setName(movie.getName());
                }
                else {
                    rm.setName(movie.getName() + ": " + movie.getSubName());
                }
                rm.setImageUrl(ImageUrlProvider.getMovieImageRandom(movie));
                MovieRating rating = ratingMap.get(movie.getId());
                if (rating == null) {
                    rm.setScore("");
                    unRatedList.add(rm);
                }
                else {
                    if (systemId == RatingSystem.META || systemId == RatingSystem.ROTTEN_AUD || systemId == RatingSystem.ROTTEN_PRO) {
                        rm.setScore(FormatUtil.formatNumber(rating.getScore()));
                    }
                    else {
                        rm.setScore(FormatUtil.pointZ(rating.getScore()));
                    }
                    if (rating.getPerson() > 0) {
                        rm.setPerson(FormatUtil.formatDivideNumber(rating.getPerson()) + "人评价");
                    }
                    rm.setRating(rating);
                    ratedList.add(rm);
                }
            }
            if (mSortType == AppConstants.RATING_SORT_DEBUT) {
                Collections.sort(ratedList, new DebutComparator());
            }
            else if (mSortType == AppConstants.RATING_SORT_NAME) {
                Collections.sort(ratedList, new NameComparator());
            }
            else {
                Collections.sort(ratedList, new ScoreComparator(mSortType));
            }
            Collections.sort(unRatedList, new DebutComparator());

            ratedMovies.postValue(ratedList);
            unratedMovies.postValue(unRatedList);
            e.onNext(true);
        });
    }

    public void updateScore(RatingMovie ratingMovie, double score, int person) {
        MovieRating rating = ratingMovie.getRating();
        if (rating == null) {
            rating = new MovieRating();
            rating.setSystemId(systemId);
            rating.setMovieId(ratingMovie.getMovie().getId());
        }
        rating.setPerson(person);
        rating.setScore(score);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rating.setUpdateDate(sdf.format(new Date()));
        getDaoSession().getMovieRatingDao().insertOrReplace(rating);
        getDaoSession().getMovieRatingDao().detachAll();

        loadRatings(systemId);
    }

    public void updateRottenScore(RatingMovie ratingMovie, double scorePro, int personPro, double scoreAud, int personAud) {
        MovieRating pro;
        if (ratingMovie.getRottenPro() == null) {
            pro = new MovieRating();
            pro.setSystemId(RatingSystem.ROTTEN_PRO);
            pro.setMovieId(ratingMovie.getMovie().getId());
        }
        else {
            pro = ratingMovie.getRottenPro().getRating();
        }
        pro.setPerson(personPro);
        pro.setScore(scorePro);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        pro.setUpdateDate(sdf.format(new Date()));
        getDaoSession().getMovieRatingDao().insertOrReplace(pro);

        MovieRating aud;
        if (ratingMovie.getRottenAud() == null) {
            aud = new MovieRating();
            aud.setSystemId(RatingSystem.ROTTEN_AUD);
            aud.setMovieId(ratingMovie.getMovie().getId());
        }
        else {
            aud = ratingMovie.getRottenAud().getRating();
        }
        aud.setPerson(personAud);
        aud.setScore(scoreAud);
        aud.setUpdateDate(sdf.format(new Date()));
        getDaoSession().getMovieRatingDao().insertOrReplace(aud);

        getDaoSession().getMovieRatingDao().detachAll();

        loadRatings(systemId);
    }

    public void changeSortType(int sortType) {
        mSortType = sortType;
        loadRatings(systemId);
    }

    private class RottenProComparator implements Comparator<RatingMovie> {

        private final int mSortType;

        public RottenProComparator(int mSortType) {
            this.mSortType = mSortType;
        }

        @Override
        public int compare(RatingMovie o1, RatingMovie o2) {
            double score1;
            double score2;
            switch (mSortType) {
                case AppConstants.RATING_SORT_PERSON_PRO:
                    score1 = o1.getRottenPro() == null ? 0:o1.getRottenPro().getRating().getPerson();
                    score2 = o2.getRottenPro() == null ? 0:o2.getRottenPro().getRating().getPerson();
                    break;
                case AppConstants.RATING_SORT_RATING_AUD:
                    score1 = o1.getRottenAud() == null ? 0:o1.getRottenAud().getRating().getScore();
                    score2 = o2.getRottenAud() == null ? 0:o2.getRottenAud().getRating().getScore();
                    break;
                case AppConstants.RATING_SORT_PERSON_AUD:
                    score1 = o1.getRottenAud() == null ? 0:o1.getRottenAud().getRating().getPerson();
                    score2 = o2.getRottenAud() == null ? 0:o2.getRottenAud().getRating().getPerson();
                    break;
                case AppConstants.RATING_SORT_RATING_PRO:
                default:
                    score1 = o1.getRottenPro() == null ? 0:o1.getRottenPro().getRating().getScore();
                    score2 = o2.getRottenPro() == null ? 0:o2.getRottenPro().getRating().getScore();
                    break;
            }
            double result = score2 - score1;
            if (result > 0) {
                return 1;
            }
            else if (result < 0) {
                return -1;
            }
            else {
                // 第二关键字
                double secondScore1;
                double secondScore2;
                switch (mSortType) {
                    case AppConstants.RATING_SORT_PERSON_PRO:
                        secondScore1 = o1.getRottenPro() == null ? 0:o1.getRottenPro().getRating().getScore();
                        secondScore2 = o2.getRottenPro() == null ? 0:o2.getRottenPro().getRating().getScore();
                        break;
                    case AppConstants.RATING_SORT_RATING_AUD:
                        secondScore1 = o1.getRottenAud() == null ? 0:o1.getRottenAud().getRating().getPerson();
                        secondScore2 = o2.getRottenAud() == null ? 0:o2.getRottenAud().getRating().getPerson();
                        break;
                    case AppConstants.RATING_SORT_PERSON_AUD:
                        secondScore1 = o1.getRottenAud() == null ? 0:o1.getRottenAud().getRating().getScore();
                        secondScore2 = o2.getRottenAud() == null ? 0:o2.getRottenAud().getRating().getScore();
                        break;
                    case AppConstants.RATING_SORT_RATING_PRO:
                    default:
                        secondScore1 = o1.getRottenPro() == null ? 0:o1.getRottenPro().getRating().getPerson();
                        secondScore2 = o2.getRottenPro() == null ? 0:o2.getRottenPro().getRating().getPerson();
                        break;
                }
                result = secondScore2 - secondScore1;
                if (result > 0) {
                    return 1;
                }
                else if (result < 0) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
    }

    private class ScoreComparator implements Comparator<RatingMovie> {

        private final int mSortType;

        public ScoreComparator(int mSortType) {
            this.mSortType = mSortType;
        }

        @Override
        public int compare(RatingMovie o1, RatingMovie o2) {
            double score1;
            double score2;
            switch (mSortType) {
                case AppConstants.RATING_SORT_PERSON:
                    score1 = o1.getRating().getPerson();
                    score2 = o2.getRating().getPerson();
                    break;
                case AppConstants.RATING_SORT_RATING:
                default:
                    score1 = o1.getRating().getScore();
                    score2 = o2.getRating().getScore();
                    break;
            }
            double result = score2 - score1;
            if (result > 0) {
                return 1;
            }
            else if (result < 0) {
                return -1;
            }
            else {
                // 第二关键字
                double secondScore1;
                double secondScore2;
                switch (mSortType) {
                    case AppConstants.RATING_SORT_PERSON:
                        secondScore1 = o1.getRating().getScore();
                        secondScore2 = o2.getRating().getScore();
                        break;
                    case AppConstants.RATING_SORT_RATING:
                    default:
                        secondScore1 = o1.getRating().getPerson();
                        secondScore2 = o2.getRating().getPerson();
                        break;
                }
                result = secondScore2 - secondScore1;
                if (result > 0) {
                    return 1;
                }
                else if (result < 0) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
    }

    private class DebutComparator implements Comparator<RatingMovie> {

        @Override
        public int compare(RatingMovie o1, RatingMovie o2) {
            String debut1 = o1.getMovie().getDebut();
            String debut2 = o2.getMovie().getDebut();
            return debut1.compareTo(debut2);
        }
    }

    private class NameComparator implements Comparator<RatingMovie> {

        @Override
        public int compare(RatingMovie o1, RatingMovie o2) {
            String name1 = o1.getMovie().getName();
            String name2 = o2.getMovie().getName();
            return name1.compareTo(name2);
        }
    }
}
