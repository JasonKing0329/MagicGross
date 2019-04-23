package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.page.bean.EditMarketGrossBean;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.utils.ListUtil;

import java.util.ArrayList;
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
 * @date: 2019/4/23 17:16
 */
public class EditMarketGrossViewModel extends BaseViewModel {

    public ObservableInt selectRefVisibility = new ObservableInt();
    public ObservableField<String> ref1MovieName = new ObservableField<>();
    public ObservableField<String> ref2MovieName = new ObservableField<>();

    public MutableLiveData<Movie> movieObserver = new MutableLiveData<>();
    public MutableLiveData<List<EditMarketGrossBean>> marketsObserver = new MutableLiveData<>();

    public EditMarketGrossViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadData(long movieId) {
        try {
            Movie movie = getDaoSession().getMovieDao()
                    .queryBuilder()
                    .where(MovieDao.Properties.Id.eq(movieId))
                    .build().unique();
            movieObserver.setValue(movie);
        } catch (Exception e) {
            e.printStackTrace();
            messageObserver.setValue(e.getMessage());
            return;
        }

        loadData(null);
    }

    private void loadData(ArrayList<CharSequence> refMovieIds) {
        getData(refMovieIds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<EditMarketGrossBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<EditMarketGrossBean> list) {
                        marketsObserver.setValue(list);
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

    public void onReferenceChanged(ArrayList<CharSequence> list) {
        loadData(list);
    }

    private Observable<List<EditMarketGrossBean>> getData(ArrayList<CharSequence> refMovieIds) {
        return Observable.create(e -> {
            List<EditMarketGrossBean> list = new ArrayList<>();
            List<Market> markets = getDaoSession().getMarketDao().queryBuilder()
                    .orderAsc(MarketDao.Properties.Name)
                    .build().list();
            for (int i = 0; i < markets.size(); i ++) {
                Market market = markets.get(i);
                EditMarketGrossBean bean = new EditMarketGrossBean();
                bean.setBean(market);
                if (TextUtils.isEmpty(market.getNameChn())) {
                    bean.setMarket(market.getName());
                }
                else {
                    bean.setMarket(market.getName() + " " + market.getNameChn());
                }
                bean.setIndex(String.valueOf(i + 1));
                bean.setRef1("");
                bean.setRef2("");
                bean.setGrossText("");
                bean.setMarkColor(Color.WHITE);
                list.add(bean);
            }
            fillMovie(movieObserver.getValue().getId(), list, 2);
            if (ListUtil.isEmpty(refMovieIds)) {
                selectRefVisibility.set(View.VISIBLE);
            }
            else {
                selectRefVisibility.set(View.GONE);
                for (int i = 0; i < refMovieIds.size() && i < 2; i ++) {
                    long movieId = Long.parseLong(refMovieIds.get(i).toString());
                    fillMovie(movieId, list, i);
                    if (i == 0) {
                        ref1MovieName.set(getMovieName(movieId));
                    }
                    else if (i == 1) {
                        ref2MovieName.set(getMovieName(movieId));
                    }
                }
            }
            e.onNext(list);
        });
    }

    private String getMovieName(long movieId) {
        Movie movie = getDaoSession().getMovieDao()
                .queryBuilder()
                .where(MovieDao.Properties.Id.eq(movieId))
                .build().unique();
        if (TextUtils.isEmpty(movie.getSubName())) {
            return movie.getName();
        }
        else {
            return movie.getName() + ":" + movie.getSubName();
        }
    }

    private void fillMovie(long movieId, List<EditMarketGrossBean> list, int movieIndex) {
        MarketGrossDao dao = getDaoSession().getMarketGrossDao();
        for (EditMarketGrossBean bean:list) {
            MarketGross gross = dao.queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movieId))
                    .where(MarketGrossDao.Properties.MarketId.eq(bean.getBean().getId()))
                    .build().unique();
            if (gross != null) {
                if (movieIndex == 0) {
                    bean.setRef1(FormatUtil.formatUsGross(gross.getGross()));
                }
                else if (movieIndex == 1) {
                    bean.setRef2(FormatUtil.formatUsGross(gross.getGross()));
                }
                else {
                    bean.setMarketGross(gross);
                    bean.setGross(gross.getGross());
                    bean.setGrossText(FormatUtil.formatUsGross(gross.getGross()));
                    bean.setMarkColor(Color.parseColor("#ffff00"));
                }
            }
        }
    }

    public void executeUpdate() {
        update()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        messageObserver.setValue("Success");
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

    private Observable<Boolean> update() {
        return Observable.create(e -> {
            if (marketsObserver != null) {
                List<MarketGross> insertList = new ArrayList<>();
                List<MarketGross> updateList = new ArrayList<>();
                List<MarketGross> deleteList = new ArrayList<>();
                for (EditMarketGrossBean bean:marketsObserver.getValue()) {
                    if (bean.isEdited()) {
                        MarketGross gross = bean.getMarketGross();
                        if (gross == null) {
                            if (bean.getGross() > 0) {
                                gross = new MarketGross();
                                insertList.add(gross);
                            }
                        }
                        else {
                            if (bean.getGross() > 0) {
                                updateList.add(gross);
                            }
                            else {
                                deleteList.add(gross);
                            }
                        }
                        gross.setMovieId(movieObserver.getValue().getId());
                        gross.setGross(bean.getGross());
                        gross.setMarketId(bean.getBean().getId());
                    }
                }
                if (insertList.size() > 0) {
                    getDaoSession().getMarketGrossDao().insertInTx(insertList);
                }
                if (updateList.size() > 0) {
                    getDaoSession().getMarketGrossDao().updateInTx(updateList);
                }
                if (deleteList.size() > 0) {
                    getDaoSession().getMarketGrossDao().deleteInTx(deleteList);
                }
                getDaoSession().getMarketGrossDao().detachAll();
            }
            e.onNext(true);
        });
    }
}
