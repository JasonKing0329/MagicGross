package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.RankItem;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MarketRankViewModel extends BaseViewModel {

    public MutableLiveData<List<Object>> marketsObserver = new MutableLiveData<>();
    public MutableLiveData<List<RankItem<MarketGross>>> rankObserver = new MutableLiveData<>();

    public MarketRankViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMarkets() {
        getMarkets()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Object>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<Object> list) {
                        marketsObserver.setValue(list);
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

    private Observable<List<Object>> getMarkets() {
        return Observable.create(e -> {
            List<Object> list = new ArrayList<>();
            List<Market> markets = getDaoSession().getMarketDao().queryBuilder()
                    .orderAsc(MarketDao.Properties.Continent, MarketDao.Properties.Name)
                    .build().list();
            List<Object> unknownList = new ArrayList<>();
            String continent = null;
            for (Market market:markets) {
                String cont = market.getContinent();
                if (cont == null) {
                    unknownList.add(market);
                }
                else {
                    if (!cont.equals(continent)) {
                        list.add(cont);
                        continent = cont;
                    }
                    list.add(market);
                }
            }
            if (unknownList.size() > 0) {
                unknownList.add(0, "Unknown");
                list.addAll(unknownList);
            }
            e.onNext(list);
        });
    }

    public void loadMarketRank(Market data) {
        loadRankItems(getMarketRankItems(data));
    }

    public void loadContinentRank(String continent) {
        loadRankItems(getContinentsRankItems(continent));
    }

    private void loadRankItems(Observable<List<RankItem<MarketGross>>> observable) {
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<RankItem<MarketGross>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<RankItem<MarketGross>> rankItems) {
                        rankObserver.setValue(rankItems);
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

    private Observable<List<RankItem<MarketGross>>> getMarketRankItems(Market data) {
        return Observable.create(e -> {
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MarketId.eq(data.getId()))
                    .orderDesc(MarketGrossDao.Properties.Gross)
                    .build().list();

            List<RankItem<MarketGross>> list = new ArrayList<>();
            for (int i = 0; i < marketGrosses.size(); i ++) {
                MarketGross gross = marketGrosses.get(i);
                RankItem item = new RankItem();
                item.setData(gross);
                item.setSortValue(gross.getGross());
                item.setValue(FormatUtil.formatUsGross(gross.getGross()));
                item.setMovie(gross.getMovie());
                item.setYear(String.valueOf(gross.getMovie().getYear()));
                if (i < 3) {
                    item.setImageUrl(ImageUrlProvider.getMovieImageRandom(item.getMovie()));
                }
                if (TextUtils.isEmpty(gross.getMovie().getSubName())) {
                    item.setName(gross.getMovie().getName());
                }
                else {
                    item.setName(gross.getMovie().getName() + ":" + gross.getMovie().getSubName());
                }
                item.setRank(String.valueOf(i + 1));
                list.add(item);
            }
            e.onNext(list);
        });
    }

    private Observable<List<RankItem<MarketGross>>> getContinentsRankItems(String continent) {
        return Observable.create(e -> {
            Database database = MApplication.getInstance().getDatabase();
            String sql = "SELECT mg.*, SUM(gross) AS total FROM market_gross mg\n" +
                    " JOIN market m ON mg.market_id = m._id\n" +
                    " WHERE m.continent=?\n" +
                    " GROUP BY mg.movie_id\n" +
                    " ORDER BY total DESC";
            Cursor cursor = database.rawQuery(sql,new String[]{continent});
            List<RankItem<MarketGross>> list = new ArrayList<>();
            int count = 0;
            while (cursor.moveToNext()) {
                MarketGross gross = new MarketGross(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getLong(3), cursor.getLong(4)
                    , cursor.getString(5), cursor.getString(6));
                RankItem item = new RankItem();
                item.setData(gross);
                item.setSortValue(gross.getGross());
                item.setValue(FormatUtil.formatUsGross(cursor.getLong(7)));

                Movie movie = getDaoSession().getMovieDao().load(gross.getMovieId());
                item.setMovie(movie);
                item.setYear(String.valueOf(movie.getYear()));
                item.setImageUrl(ImageUrlProvider.getMovieImageRandom(movie));
                if (TextUtils.isEmpty(movie.getSubName())) {
                    item.setName(movie.getName());
                }
                else {
                    item.setName(movie.getName() + ":" + movie.getSubName());
                }
                item.setRank(String.valueOf(count + 1));
                list.add(item);

                count ++;
            }
            e.onNext(list);
        });
    }
    public void updateMarketGross(int position) {
        MarketGross gross = rankObserver.getValue().get(position).getData();
        getDaoSession().getMarketGrossDao().update(gross);
        getDaoSession().getMarketGrossDao().detach(gross);
        RankItem item = rankObserver.getValue().get(position);
        item.setValue(FormatUtil.formatUsGross(gross.getGross()));
    }

    public void deleteMarketGross(int position) {
        getDaoSession().getMarketGrossDao().delete(rankObserver.getValue().get(position).getData());
        getDaoSession().getMarketGrossDao().detachAll();
        rankObserver.getValue().remove(position);
    }
}
