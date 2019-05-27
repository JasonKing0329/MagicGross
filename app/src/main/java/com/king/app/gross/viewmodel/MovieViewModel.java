package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.GrossStatDao;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;

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

    public ObservableField<String> mojoTitle = new ObservableField<>();

    public ObservableField<String> movieImageUrl = new ObservableField<>();

    public ObservableField<String> movieName = new ObservableField<>();

    public ObservableField<String> movieChnName = new ObservableField<>();

    public ObservableField<String> movieDebut = new ObservableField<>();

    public ObservableField<String> movieBudget = new ObservableField<>();

    public ObservableField<String> movieExchangeRate = new ObservableField<>();

    public ObservableField<String> movieMojoId = new ObservableField<>();

    public ObservableField<String> grossNa = new ObservableField<>();

    public ObservableField<String> grossChn = new ObservableField<>();

    public ObservableField<String> grossMarket = new ObservableField<>();

    public ObservableField<String> grossOversea = new ObservableField<>();

    public ObservableField<String> grossWorldWide = new ObservableField<>();

    public MutableLiveData<Movie> movieObserver = new MutableLiveData<>();

    public MovieViewModel(@NonNull Application application) {
        super(application);
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
            movieExchangeRate.set(String.valueOf(movie.getUsToYuan()));
            movieMojoId.set(movie.getMojoId());
            if (movie.getBudget() == 0) {
                movieBudget.set("N/A");
            }
            else {
                movieBudget.set(FormatUtil.formatUsGross(movie.getBudget()));
            }

            if (movie.getIsReal() == AppConstants.MOVIE_REAL) {
                mojoTitle.set("Mojo Id");
            }
            else {
                mojoTitle.set("Virtual Movie");
            }
            observer.onNext(movie);
        };
    }

    private ObservableSource<Movie> loadGross(Movie movie) {
        return observer -> {
            GrossStat stat = getDaoSession().getGrossStatDao().queryBuilder()
                    .where(GrossStatDao.Properties.MovieId.eq(movie.getId()))
                    .build().unique();
            grossNa.set(FormatUtil.formatUsGross(stat.getUs()));
            grossChn.set(FormatUtil.formatChnGross(stat.getChn()));
            grossWorldWide.set(FormatUtil.formatUsGross(stat.getWorld()));

            Gross gross = getDaoSession().getGrossDao().queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                    .where(GrossDao.Properties.IsTotal.eq(1))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                    .build().unique();
            if (gross == null) {
                grossOversea.set("N/A");
            }
            else {
                grossOversea.set(FormatUtil.formatUsGross(gross.getGross()));
            }

            long marketCount = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                    .buildCount().count();
            grossMarket.set(marketCount + " Markets");
            observer.onNext(movie);
        };
    }
}
