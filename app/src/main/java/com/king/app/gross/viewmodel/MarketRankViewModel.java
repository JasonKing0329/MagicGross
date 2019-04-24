package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.RankItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MarketRankViewModel extends BaseViewModel {

    public MutableLiveData<List<Market>> marketsObserver = new MutableLiveData<>();
    public MutableLiveData<List<RankItem>> rankObserver = new MutableLiveData<>();

    public MarketRankViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMarkets() {
        marketsObserver.setValue(getDaoSession().getMarketDao().queryBuilder().orderAsc(MarketDao.Properties.Name).build().list());
    }

    public void loadMarketRank(Market data) {
        getMarketRankItems(data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<RankItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<RankItem> rankItems) {
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

    private Observable<List<RankItem>> getMarketRankItems(Market data) {
        return Observable.create(e -> {
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MarketId.eq(data.getId()))
                    .orderDesc(MarketGrossDao.Properties.Gross)
                    .build().list();

            List<RankItem> list = new ArrayList<>();
            for (int i = 0; i < marketGrosses.size(); i ++) {
                MarketGross gross = marketGrosses.get(i);
                RankItem item = new RankItem();
                item.setSortValue(gross.getGross());
                item.setValue(FormatUtil.formatUsGross(gross.getGross()));
                item.setMovie(gross.getMovie());
                item.setYear(String.valueOf(gross.getMovie().getYear()));
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
}
