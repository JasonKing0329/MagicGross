package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.GrossStatDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.entity.MovieRating;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.page.bean.MovieBasicData;
import com.king.app.gross.page.bean.MovieMarketItem;
import com.king.app.gross.page.bean.RatingData;
import com.king.app.gross.page.bean.RatingMovie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.utils.ListUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/27 9:20
 */
public class MovieViewModel extends BaseViewModel {

    public ObservableField<String> movieImageUrl = new ObservableField<>();

    public ObservableField<String> movieName = new ObservableField<>();

    public ObservableField<String> movieChnName = new ObservableField<>();

    public ObservableField<String> movieDebut = new ObservableField<>();

    public MutableLiveData<Movie> movieObserver = new MutableLiveData<>();

    public MutableLiveData<List<Object>> pageDataObserver = new MutableLiveData<>();

    private MovieBasicData basicData;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        basicData = new MovieBasicData();
    }

    public void loadMovie(long movieId) {
        loadingObserver.setValue(true);
        getMovie(movieId)
                .flatMap(movie -> loadBaseInfo(movie))
                .flatMap(movie -> loadGross(movie))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Movie>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Movie movie) {
                        loadingObserver.setValue(false);
                        movieObserver.setValue(movie);

                        List<Object> list = new ArrayList<>();
                        list.add(basicData);
                        pageDataObserver.setValue(list);

                        loadRankInfo();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Movie> getMovie(long movieId) {
        return Observable.create(e -> e.onNext(getDaoSession().getMovieDao().load(movieId)));
    }

    private ObservableSource<Movie> loadBaseInfo(Movie movie) {
        return observer -> {
            if (TextUtils.isEmpty(movie.getSubName())) {
                movieName.set(movie.getName());
            }
            else {
                movieName.set(movie.getName() + ":" + movie.getSubName());
            }
            if (!TextUtils.isEmpty(movie.getNameChn())) {
                if (TextUtils.isEmpty(movie.getSubChnName())) {
                    movieChnName.set(movie.getNameChn());
                }
                else {
                    movieChnName.set(movie.getNameChn() + ":" + movie.getSubChnName());
                }
            }
            movieDebut.set(movie.getDebut());
            movieImageUrl.set(ImageUrlProvider.getMovieImageRandom(movie));
            basicData.setExchangeRate(String.valueOf(movie.getUsToYuan()));
            basicData.setMojoId(movie.getMojoId());
            basicData.setMojoGrpId(movie.getMojoGrpId());
            basicData.setMojoTitleId(movie.getMojoTitleId());
            if (movie.getBudget() == 0) {
                basicData.setBudget("N/A");
            }
            else {
                basicData.setBudget(FormatUtil.formatUsGross(movie.getBudget()));
            }

            if (movie.getIsReal() == AppConstants.MOVIE_REAL) {
                basicData.setMojoTitle("Mojo Id");
                basicData.setMojoGrpVisibility(View.VISIBLE);
                basicData.setMojoTitleVisibility(View.VISIBLE);
            }
            else {
                basicData.setMojoTitle("Virtual Movie");
                basicData.setMojoGrpVisibility(View.GONE);
                basicData.setMojoTitleVisibility(View.GONE);
            }

            // ratings
            if (!ListUtil.isEmpty(movie.getRatingList())) {
                for (MovieRating rating:movie.getRatingList()) {
                    if (rating.getScore() > 0) {
                        RatingData data = new RatingData();
                        if (rating.getPerson() > 0) {
                            data.setPerson(FormatUtil.formatDivideNumber(rating.getPerson()));
                        }
                        data.setRating(rating);
                        data.setScore(FormatUtil.formatNumber(rating.getScore()));

                        if (rating.getSystemId() == RatingSystem.IMDB) {
                            basicData.setImdb(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.META) {
                            basicData.setMetaScore(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.ROTTEN_AUD) {
                            data.setScore(data.getScore() + "%");
                            basicData.setRottenAud(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.ROTTEN_PRO) {
                            data.setScore(data.getScore() + "%");
                            basicData.setRottenPro(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.DOUBAN) {
                            basicData.setDouBan(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.MAOYAN) {
                            basicData.setMaoYan(data);
                        }
                        else if (rating.getSystemId() == RatingSystem.TAOPP) {
                            basicData.setTaoPiaoPiao(data);
                        }
                    }
                }
            }

            observer.onNext(movie);
        };
    }

    private ObservableSource<Movie> loadGross(Movie movie) {
        return observer -> {
            GrossStat stat = getDaoSession().getGrossStatDao().queryBuilder()
                    .where(GrossStatDao.Properties.MovieId.eq(movie.getId()))
                    .build().unique();
            basicData.setGrossNa(FormatUtil.formatUsGross(stat.getUs()));
            basicData.setGrossChn(FormatUtil.formatChnGross(stat.getChn()));
            basicData.setGrossWorldWide(FormatUtil.formatUsGross(stat.getWorld()));

            Gross gross = getDaoSession().getGrossDao().queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                    .where(GrossDao.Properties.IsTotal.eq(1))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                    .build().unique();
            if (gross == null) {
                basicData.setGrossOversea("N/A");
            }
            else {
                basicData.setGrossOversea(FormatUtil.formatUsGross(gross.getGross()));
            }

            long marketCount = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                    .buildCount().count();
            basicData.setGrossMarket(marketCount + " Markets");
            observer.onNext(movie);
        };
    }

    public void statVirtualChn() {
        new StatModel().statVirtualChn(movieObserver.getValue())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossStat>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossStat stat) {
                        loadMovie(movieObserver.getValue().getId());
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

    private void loadRankInfo() {
        getMarketRankInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Object>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<Object> list) {
                        pageDataObserver.setValue(list);
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

    private Observable<List<Object>> getMarketRankInfo() {
        return Observable.create(e -> {
            long movieId = movieObserver.getValue().getId();
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movieId))
                    .build().list();
            List<MovieMarketItem> list = new ArrayList<>();
            for (MarketGross mg:marketGrosses) {
                QueryBuilder<MarketGross> builder = getDaoSession().getMarketGrossDao().queryBuilder();
                if (!SettingProperty.isEnableVirtualMovie()) {
                    builder.join(MarketGrossDao.Properties.MovieId, Movie.class)
                            .where(MovieDao.Properties.IsReal.eq(AppConstants.MOVIE_REAL));
                }
                List<MarketGross> movies = builder
                        .where(MarketGrossDao.Properties.MarketId.eq(mg.getMarketId()))
                        .where(MarketGrossDao.Properties.MarketId.gt(AppConstants.MARKET_TOTAL_ID))
                        .orderDesc(MarketGrossDao.Properties.Gross)
                        .build().list();

                // 只取大市场的排名（market_gross记录超过100）
                if (movies.size() >= 100) {
                    MovieMarketItem item = new MovieMarketItem();
                    item.setMarket(mg.getMarket());
                    item.setTotal(movies.size());
                    item.setRank(findRankInMarket(movieId, movies));
                    item.setImageUrl(ImageUrlProvider.getMarketFlag(mg.getMarket()));
                    list.add(item);
                }
            }
            // 按rank升序排名
            Collections.sort(list, new RankComparator());

            List<Object> items = formatRankInfo(list);
            e.onNext(items);
        });
    }

    private int findRankInMarket(long movieId, List<MarketGross> movies) {
        for (int i = 0; i < movies.size(); i ++) {
            if (movieId == movies.get(i).getMovieId()) {
                return i + 1;
            }
        }
        return 0;
    }

    private List<Object> formatRankInfo(List<MovieMarketItem> items) {
        String lastGroup = null;
        for (MovieMarketItem item:items) {
            String group = getGroupRank(item.getRank());
            if (!group.equals(lastGroup)) {
                lastGroup = group;
                pageDataObserver.getValue().add(group);
            }
            pageDataObserver.getValue().add(item);
        }
        return pageDataObserver.getValue();
    }

    private String getGroupRank(int rank) {
        if (rank == 1) {
            return "Champion";
        }
        else if (rank < 4) {
            return "Top 3";
        }
        else if (rank <= 10) {
            return "Top 10";
        }
        else if (rank <= 20) {
            return "Top 11-20";
        }
        else if (rank <= 30) {
            return "Top 21-30";
        }
        else if (rank <= 50) {
            return "Top 31-50";
        }
        else if (rank <= 100) {
            return "Top 51-100";
        }
        else {
            return "Out of 100";
        }
    }

    public RatingMovie getRatingMovie(long systemId) {
        RatingMovie rm = new RatingMovie();
        rm.setMovie(movieObserver.getValue());
        List<MovieRating> list = movieObserver.getValue().getRatingList();
        if (list != null) {
            for (MovieRating rating:list) {
                if (rating.getSystemId() == systemId) {
                    rm.setRating(rating);
                }
            }
        }
        return rm;
    }

    public RatingMovie getRottenRating() {
        RatingMovie rm = new RatingMovie();
        rm.setMovie(movieObserver.getValue());
        List<MovieRating> list = movieObserver.getValue().getRatingList();
        if (list != null) {
            for (MovieRating rating:list) {
                if (rating.getSystemId() == RatingSystem.ROTTEN_AUD) {
                    RatingData data = new RatingData();
                    data.setRating(rating);
                    rm.setRottenAud(data);
                }
                else if (rating.getSystemId() == RatingSystem.ROTTEN_PRO) {
                    RatingData data = new RatingData();
                    data.setRating(rating);
                    rm.setRottenPro(data);
                }
            }
        }
        return rm;
    }

    public void updateScore(RatingMovie ratingMovie, double score, int person) {
        MovieRating rating = ratingMovie.getRating();
        if (rating == null) {
            rating = new MovieRating();
            rating.setSystemId(ratingMovie.getRating().getSystemId());
            rating.setMovieId(ratingMovie.getMovie().getId());
        }
        rating.setPerson(person);
        rating.setScore(score);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rating.setUpdateDate(sdf.format(new Date()));
        getDaoSession().getMovieRatingDao().insertOrReplace(rating);
        getDaoSession().getMovieRatingDao().detachAll();
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
    }

    private class RankComparator implements Comparator<MovieMarketItem> {

        @Override
        public int compare(MovieMarketItem left, MovieMarketItem right) {
            return left.getRank() - right.getRank();
        }
    }
}
