package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.http.mojo.MojoClient;
import com.king.app.gross.model.http.mojo.MojoParser;
import com.king.app.gross.page.bean.ContinentGross;
import com.king.app.gross.page.bean.MarketTotal;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.FormatUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MojoViewModel extends BaseViewModel {

    private final int TYPE_GROUP = 1;
    private final int TYPE_ALL = 0;

    private int groupType = TYPE_ALL;

    private MojoParser parser;

    public MutableLiveData<List<MarketGross>> grossObserver = new MutableLiveData<>();
    public MutableLiveData<Movie> movieObserver = new MutableLiveData<>();
    public MutableLiveData<List<Object>> groupObserver = new MutableLiveData<>();

    public MutableLiveData<String> nextGroupTypeTitle = new MutableLiveData<>();

    private MarketTotal marketTotal;

    private StatModel statModel;

    public MojoViewModel(@NonNull Application application) {
        super(application);
        parser = new MojoParser();
        marketTotal = new MarketTotal();
        statModel = new StatModel();
    }

    public boolean isGroupType() {
        return groupType == TYPE_GROUP;
    }

    public void loadMovie(long movieId) {
        try {
            Movie movie = MApplication.getInstance().getDaoSession().getMovieDao()
                    .queryBuilder()
                    .where(MovieDao.Properties.Id.eq(movieId))
                    .build().unique();
            movieObserver.setValue(movie);
        } catch (Exception e) {
            e.printStackTrace();
            messageObserver.setValue(e.getMessage());
            return;
        }

        if (groupType == TYPE_GROUP) {
            loadGroup();
        }
        else {
            loadGross();
        }
    }

    public void changeGroup() {
        if (groupType == TYPE_GROUP) {
            nextGroupTypeTitle.setValue("Group by continent");
            loadGross();
            groupType = TYPE_ALL;
        }
        else {
            nextGroupTypeTitle.setValue("No group");
            loadGroup();
            groupType = TYPE_GROUP;
        }
    }

    public MarketTotal getMarketTotal() {
        return marketTotal;
    }

    public void onTotalChanged() {
        createTotal()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<MarketTotal>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(MarketTotal marketTotal) {

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

    private Observable<MarketTotal> createTotal() {
        return Observable.create(e -> {
            if (movieObserver.getValue().getIsReal() == AppConstants.MOVIE_REAL) {
                getRealTotal();
            }
            else {
                getVirtualTotal();
            }
            e.onNext(marketTotal);
        });
    }

    private void getVirtualTotal() {
        long[] marketSumParams = statModel.sumMarket(movieObserver.getValue());
        long marketOpenSum = marketSumParams[0];
        long marketSum = marketSumParams[1];
        marketTotal.setMarketOpening(FormatUtil.formatUsGross(marketOpenSum));
        marketTotal.setMarketGross(FormatUtil.formatUsGross(marketSum));

        long undisOpenSum = 0;
        long undisSum = 0;
        MarketGross undisclosed = statModel.getUndisclosed(movieObserver.getValue());
        if (undisclosed != null) {
            undisOpenSum = undisclosed.getOpening();
            undisSum = undisclosed.getGross();
        }
        marketTotal.setUndisclosedOpening(FormatUtil.formatUsGross(undisOpenSum));
        marketTotal.setUndisclosedGross(FormatUtil.formatUsGross(undisSum));

        marketTotal.setGross(FormatUtil.formatUsGross(marketSum + undisSum));
        marketTotal.setOpening(FormatUtil.formatUsGross(marketOpenSum + undisOpenSum));
    }

    private void getRealTotal() {
        MarketGross total = MApplication.getInstance().getDaoSession().getMarketGrossDao().queryBuilder()
                .where(MarketGrossDao.Properties.MovieId.eq(movieObserver.getValue().getId()))
                .where(MarketGrossDao.Properties.MarketId.eq(AppConstants.MARKET_TOTAL_ID))
                .build().unique();
        if (total != null) {
            marketTotal.setGross(FormatUtil.formatUsGross(total.getGross()));
            marketTotal.setOpening(FormatUtil.formatUsGross(total.getOpening()));

            Cursor cursor = MApplication.getInstance().getDatabase().rawQuery(
                    "SELECT SUM(opening), SUM(gross) FROM market_gross WHERE movie_id=? AND market_id!=0", new String[]{String.valueOf(movieObserver.getValue().getId())});
            if (cursor.moveToNext()) {
                long opening = cursor.getLong(0);
                marketTotal.setMarketOpening(FormatUtil.formatUsGross(opening));
                marketTotal.setUndisclosedOpening(FormatUtil.formatUsGross(total.getOpening() - opening));
                long marketGross = cursor.getLong(1);
                marketTotal.setMarketGross(FormatUtil.formatUsGross(marketGross));
                marketTotal.setUndisclosedGross(FormatUtil.formatUsGross(total.getGross() - marketGross));
            }
        }
    }

    private void loadGross() {
        createTotal()
                .flatMap(total -> loadMarketGross())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<MarketGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<MarketGross> marketGrosses) {
                        grossObserver.setValue(marketGrosses);
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

    private Observable<List<MarketGross>> loadMarketGross() {
        return Observable.create(e -> {
            List<MarketGross> grosses = MApplication.getInstance().getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movieObserver.getValue().getId()))
                    .where(MarketGrossDao.Properties.MarketId.gt(AppConstants.MARKET_TOTAL_ID))// undisclosed -1; total 0
                    .orderAsc(MarketGrossDao.Properties.MarketId)
                    .build().list();
            marketTotal.setMarketTitle(grosses.size() + " Markets");
            e.onNext(grosses);
        });
    }

    public void fetchForeignData() {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(parser.getMojoForeignUrl(movieObserver.getValue().getMojoId()))
                .flatMap(responseBody -> parser.saveFile(responseBody, AppConfig.FILE_HTML_FOREIGN))
                .flatMap(file -> parser.parseForeign(file, movieObserver.getValue().getId(), false))
//        parser.parseForeign(new File(AppConfig.FILE_HTML_FOREIGN), movieObserver.getValue().getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Boolean result) {
                        loadingObserver.setValue(false);
                        loadGross();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue("下载失败" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void changeSortType(int type) {
        sortGross(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<MarketGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<MarketGross> marketGrosses) {
                        grossObserver.setValue(marketGrosses);
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

    private Observable<List<MarketGross>> sortGross(int type) {
        return Observable.create(e -> {
            QueryBuilder<MarketGross> builder = MApplication.getInstance().getDaoSession().getMarketGrossDao().queryBuilder();
            builder.where(MarketGrossDao.Properties.MovieId.eq(movieObserver.getValue().getId()));
            builder.where(MarketGrossDao.Properties.MarketId.notEq(0));
            if (type == AppConstants.MARKET_GROSS_SORT_TOTAL) {
                builder.orderDesc(MarketGrossDao.Properties.Gross);
            }
            else if (type == AppConstants.MARKET_GROSS_SORT_OPENING) {
                builder.orderDesc(MarketGrossDao.Properties.Opening);
            }
            else if (type == AppConstants.MARKET_GROSS_SORT_DEBUT) {
                builder.orderAsc(MarketGrossDao.Properties.Debut);
            }
            else {
                builder.orderAsc(MarketGrossDao.Properties.Id);
            }
            List<MarketGross> list = builder.build().list();
            e.onNext(list);
        });
    }

    private void loadGroup() {
        createTotal()
                .flatMap(total -> loadMarketGross())
                .flatMap(list -> toContinentGroups(list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Object>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<Object> list) {
                        groupObserver.setValue(list);
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

    private ObservableSource<List<Object>> toContinentGroups(List<MarketGross> markets) {
        return observer -> {
            List<Object> list = new ArrayList<>();
            Map<String, ContinentGross> map = new HashMap<>();
            for (MarketGross market:markets) {
                String continent;
                if (market.getMarket() == null) {
                    continent = "Unknown";
                }
                else {
                    continent = market.getMarket().getContinent();
                }
                ContinentGross cg = map.get(continent);
                if (cg == null) {
                    cg = new ContinentGross();
                    cg.setContinent(continent);
                    cg.setMarketList(new ArrayList<>());
                    map.put(continent, cg);
                }
                cg.getMarketList().add(market);
                cg.setGross(cg.getGross() + market.getGross());
            }

            List<ContinentGross> cList = new ArrayList<>();
            for (String key:map.keySet()) {
                ContinentGross cg = map.get(key);
                cList.add(cg);
            }
            Collections.sort(cList, new ContinentGrossComparator());

            for (ContinentGross cg:cList) {
                list.add(cg);
                Collections.sort(cg.getMarketList(), new MarketGrossComparator());
                for (MarketGross mg:cg.getMarketList()) {
                    list.add(mg);
                }
            }
            observer.onNext(list);
        };
    }

    public void removeItem(int position) {
        MarketGross gross = grossObserver.getValue().get(position);
        getDaoSession().getMarketGrossDao().delete(gross);
        getDaoSession().getMarketGrossDao().detachAll();
        grossObserver.getValue().remove(position);
        onMarketGrossChanged();
    }

    public void updateMarketGross(MarketGross gross) {
        getDaoSession().getMarketGrossDao().update(gross);
        getDaoSession().getMarketGrossDao().detach(gross);
        onMarketGrossChanged();
    }

    public boolean enableEditMarket(MarketGross data) {
        if (movieObserver.getValue().getIsReal() == AppConstants.MOVIE_VIRTUAL &&
                data.getMarket().getName().equals("China")) {
            messageObserver.setValue("You must modify China data in daily page!");
            return false;
        }
        return true;
    }

    private class MarketGrossComparator implements Comparator<MarketGross> {

        @Override
        public int compare(MarketGross left, MarketGross right) {
            long result = right.getGross() - left.getGross();
            if (result < 0) {
                return -1;
            }
            else if (result > 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    private class ContinentGrossComparator implements Comparator<ContinentGross> {

        @Override
        public int compare(ContinentGross left, ContinentGross right) {
            long result = right.getGross() - left.getGross();
            if (result < 0) {
                return -1;
            }
            else if (result > 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    /**
     * 仅针对virtual movie
     * market gross发生变化
     * 重新统计总计以及gross_stat
     */
    public void onMarketGrossChanged() {
        if (movieObserver.getValue().getIsReal() == AppConstants.MOVIE_VIRTUAL) {
            marketChange()
                    .flatMap(result -> statModel.statVirtualMarket(movieObserver.getValue()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<GrossStat>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            addDisposable(d);
                        }

                        @Override
                        public void onNext(GrossStat aBoolean) {

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
    }

    private Observable<Boolean> marketChange() {
        DebugLog.e("");
        return Observable.create(e -> {
            long[] marketSumParams = statModel.sumMarket(movieObserver.getValue());
            long marketOpenSum = marketSumParams[0];
            long marketSum = marketSumParams[1];
            marketTotal.setMarketOpening(FormatUtil.formatUsGross(marketOpenSum));
            marketTotal.setMarketGross(FormatUtil.formatUsGross(marketSum));

            long undisOpenSum = 0;
            long undisSum = 0;
            MarketGross undisclosed = statModel.getUndisclosed(movieObserver.getValue());
            if (undisclosed != null) {
                undisOpenSum = undisclosed.getOpening();
                undisSum = undisclosed.getGross();
            }

            marketTotal.setGross(FormatUtil.formatUsGross(marketSum + undisSum));
            marketTotal.setOpening(FormatUtil.formatUsGross(marketOpenSum + undisOpenSum));
            e.onNext(true);
        });
    }

}
