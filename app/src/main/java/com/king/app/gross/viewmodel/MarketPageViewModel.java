package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.model.single.DateRangeInstance;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.RankItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/30 10:26
 */
public class MarketPageViewModel extends BaseViewModel {

    public ObservableField<String> marketImageUrl = new ObservableField<>();

    public ObservableField<String> marketName = new ObservableField<>();

    public ObservableField<String> marketChnName = new ObservableField<>();

    public ObservableField<String> marketContinent = new ObservableField<>();

    public ObservableField<String> marketCount = new ObservableField<>();

    public MutableLiveData<List<RankItem<MarketGross>>> moviesObserver = new MutableLiveData<>();

    private int mSortType;

    public MarketPageViewModel(@NonNull Application application) {
        super(application);
        mSortType = AppConstants.MOVIE_SORT_TOTAL;
    }

    public void loadMarket(long marketId) {
        loadingObserver.setValue(true);
        getMarket(marketId)
                .flatMap(market -> {
                    marketImageUrl.set(market.getImageUrl());
                    marketName.set(market.getName());
                    if (!TextUtils.isEmpty(market.getNameChn())) {
                        marketChnName.set(market.getNameChn());
                    }
                    if (!TextUtils.isEmpty(market.getContinent())) {
                        marketContinent.set(market.getContinent());
                    }
                    return getMarketRankItems(market);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<RankItem<MarketGross>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<RankItem<MarketGross>> rankItems) {
                        loadingObserver.setValue(false);
                        moviesObserver.setValue(rankItems);
                        if (rankItems.size() <= 1) {
                            marketCount.set(rankItems.size() + " Movie");
                        }
                        else {
                            marketCount.set(rankItems.size() + " Movies");
                        }
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

    private Observable<Market> getMarket(long marketId) {
        return Observable.create(e -> {
            Market market = getDaoSession().getMarketDao().load(marketId);
            market.setImageUrl(ImageUrlProvider.getMarketImage(market));
            e.onNext(market);
        });
    }

    private Observable<List<RankItem<MarketGross>>> getMarketRankItems(Market data) {
        return Observable.create(e -> {
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MarketId.eq(data.getId()))
                    .orderDesc(MarketGrossDao.Properties.Gross)
                    .build().list();

            List<RankItem<MarketGross>> list = new ArrayList<>();
            int rank = 1;
            for (int i = 0; i < marketGrosses.size(); i ++) {
                MarketGross gross = marketGrosses.get(i);
                RankItem item = new RankItem();
                if (isMovieDisable(gross.getMovie())) {
                    continue;
                }
                item.setMovie(gross.getMovie());
                item.setData(gross);
                item.setSortValue(gross.getGross());
                item.setValue(FormatUtil.formatUsGross(gross.getGross()));
                item.setYear(String.valueOf(gross.getMovie().getYear()));
                item.setImageUrl(ImageUrlProvider.getMovieImageRandom(item.getMovie()));
                if (TextUtils.isEmpty(gross.getMovie().getSubName())) {
                    item.setName(gross.getMovie().getName());
                }
                else {
                    item.setName(gross.getMovie().getName() + ":" + gross.getMovie().getSubName());
                }
                item.setRank(String.valueOf(rank ++));
                list.add(item);
            }
            e.onNext(list);
        });
    }

    private boolean isMovieDisable(Movie movie) {
        if (movie == null) {
            return true;
        }
        if (!SettingProperty.isEnableVirtualMovie() && movie.getIsReal() == AppConstants.MOVIE_VIRTUAL) {
            return true;
        }
        if (DateRangeInstance.getInstance().getStartDate() != null && movie.getDebut().compareTo(DateRangeInstance.getInstance().getStartDate()) < 0) {
            return true;
        }
        if (DateRangeInstance.getInstance().getEndDate() != null && movie.getDebut().compareTo(DateRangeInstance.getInstance().getEndDate()) > 0) {
            return true;
        }
        return false;
    }

    public void sort(int sortType) {
        if (sortType != mSortType) {
            mSortType = sortType;
            onSortTypeChanged();
        }
    }

    private void onSortTypeChanged() {
        loadingObserver.setValue(true);
        sortItems()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<RankItem<MarketGross>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<RankItem<MarketGross>> rankItems) {
                        loadingObserver.setValue(false);
                        moviesObserver.setValue(rankItems);
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

    private Observable<List<RankItem<MarketGross>>> sortItems() {
        return Observable.create(e -> {
            List<RankItem<MarketGross>> items = moviesObserver.getValue();
            switch (mSortType) {
                case AppConstants.MOVIE_SORT_DATE:
                    Collections.sort(items, new DebutComparator());
                    break;
                case AppConstants.MOVIE_SORT_NAME:
                    Collections.sort(items, new NameComparator());
                    break;
                case AppConstants.MOVIE_SORT_TOTAL:
                    Collections.sort(items, new TotalComparator());
                    break;
                case AppConstants.MOVIE_SORT_OPENING:
                    Collections.sort(items, new OpeningComparator());
                    break;
            }
            for (int i = 0; i < items.size(); i ++) {
                items.get(i).setRank(String.valueOf(i + 1));
            }
            e.onNext(items);
        });
    }

    /**
     * 倒序
     */
    private class DebutComparator implements Comparator<RankItem<MarketGross>> {

        @Override
        public int compare(RankItem<MarketGross> o1, RankItem<MarketGross> o2) {
            String date1 = o1.getMovie().getDebut();
            String date2 = o2.getMovie().getDebut();
            return date2.compareTo(date1);
        }
    }

    /**
     * 顺序
     */
    private class NameComparator implements Comparator<RankItem<MarketGross>> {

        @Override
        public int compare(RankItem<MarketGross> o1, RankItem<MarketGross> o2) {
            String name1 = o1.getMovie().getName().toLowerCase();
            String name2 = o2.getMovie().getName().toLowerCase();
            return name1.compareTo(name2);
        }
    }

    /**
     * 倒序
     */
    private class TotalComparator implements Comparator<RankItem<MarketGross>> {

        @Override
        public int compare(RankItem<MarketGross> o1, RankItem<MarketGross> o2) {
            long total1 = o1.getData().getGross();
            long total2 = o2.getData().getGross();
            long result = total2 - total1;
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

    /**
     * 倒序
     */
    private class OpeningComparator implements Comparator<RankItem<MarketGross>> {

        @Override
        public int compare(RankItem<MarketGross> o1, RankItem<MarketGross> o2) {
            long total1 = o1.getData().getOpening();
            long total2 = o2.getData().getOpening();
            long result = total2 - total1;
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
