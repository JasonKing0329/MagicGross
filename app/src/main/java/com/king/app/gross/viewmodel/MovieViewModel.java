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
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.FormatUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public ObservableField<String> marketRankInfo = new ObservableField<>();

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

    private class RankItem {
        Market market;
        int rank;
        int total;
    }

    private void loadRankInfo() {
        getMarketRankInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(String s) {
                        marketRankInfo.set(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<String> getMarketRankInfo() {
        return Observable.create(e -> {
            long movieId = movieObserver.getValue().getId();
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movieId))
                    .build().list();
            List<RankItem> items = new ArrayList<>();
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
                    RankItem item = new RankItem();
                    item.market = mg.getMarket();
                    item.total = movies.size();
                    item.rank = findRankInMarket(movieId, movies);
                    items.add(item);
                }
            }
            // 按rank升序排名
            Collections.sort(items, new RankComparator());

            String info = formatRankInfo(items);
            e.onNext(info);
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

    private String formatRankInfo(List<RankItem> items) {
        StringBuffer buffer = new StringBuffer();
        String lastGroup = null;
        for (RankItem item:items) {
            String group = getGroupRank(item.rank);
            if (!group.equals(lastGroup)) {
                lastGroup = group;
                buffer.append("\n").append(group).append("\n");
            }
            buffer.append(item.market.getName()).append("(").append(item.rank).append("/").append(item.total).append(")  ");
        }
        return buffer.toString();
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

    private class RankComparator implements Comparator<RankItem> {

        @Override
        public int compare(RankItem left, RankItem right) {
            return left.rank - right.rank;
        }
    }
}
