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
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.page.bean.EditMarketGrossBean;
import com.king.app.gross.utils.DebugLog;
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

    public static final int TYPE_TOTAL = 0;
    public static final int TYPE_OPENING = 1;
    public static final int TYPE_DEBUT = 2;
    public static final int TYPE_END = 3;

    private int mEditType = TYPE_TOTAL;

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
            // total
            EditMarketGrossBean emgb = new EditMarketGrossBean();
            emgb.setMarket("Foreign Total");
            emgb.setIndex("0");
            emgb.setRef1("");
            emgb.setRef2("");
            emgb.setGrossText("");
            emgb.setMarkColor(Color.WHITE);
            list.add(emgb);

            List<Market> markets = getDaoSession().getMarketDao().queryBuilder()
                    .orderAsc(MarketDao.Properties.Continent, MarketDao.Properties.Name)
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

    private void setPresentText(EditMarketGrossBean bean) {
        switch (mEditType) {
            case TYPE_DEBUT:
                if (bean.getRef1MarketGross() != null) {
                    bean.setRef1(bean.getRef1MarketGross().getDebut());
                }
                if (bean.getRef2MarketGross() != null) {
                    bean.setRef2(bean.getRef2MarketGross().getDebut());
                }
                if (bean.getMarketGross() != null) {
                    bean.setGrossText(bean.getMarketGross().getDebut());
                }
                break;
            case TYPE_END:
                if (bean.getRef1MarketGross() != null) {
                    bean.setRef1(bean.getRef1MarketGross().getEndDate());
                }
                if (bean.getRef2MarketGross() != null) {
                    bean.setRef2(bean.getRef2MarketGross().getEndDate());
                }
                if (bean.getMarketGross() != null) {
                    bean.setGrossText(bean.getMarketGross().getEndDate());
                }
                break;
            case TYPE_OPENING:
                if (bean.getRef1MarketGross() != null) {
                    if (bean.getRef1MarketGross().getOpening() > 0) {
                        bean.setRef1(FormatUtil.formatUsGross(bean.getRef1MarketGross().getOpening()));
                    }
                    else {
                        bean.setRef1("");
                    }
                }
                if (bean.getRef2MarketGross() != null) {
                    if (bean.getRef2MarketGross().getOpening() > 0) {
                        bean.setRef2(FormatUtil.formatUsGross(bean.getRef2MarketGross().getOpening()));
                    }
                    else {
                        bean.setRef2("");
                    }
                }
                if (bean.getMarketGross() != null) {
                    if (bean.getMarketGross().getOpening() > 0) {
                        bean.setGrossText(FormatUtil.formatUsGross(bean.getMarketGross().getOpening()));
                    }
                    else {
                        bean.setGrossText("");
                    }
                }
                break;
            default:
                if (bean.getRef1MarketGross() != null) {
                    if (bean.getRef1MarketGross().getGross() > 0) {
                        bean.setRef1(FormatUtil.formatUsGross(bean.getRef1MarketGross().getGross()));
                    }
                    else {
                        bean.setRef1("");
                    }
                }
                if (bean.getRef2MarketGross() != null) {
                    if (bean.getRef2MarketGross().getGross() > 0) {
                        bean.setRef2(FormatUtil.formatUsGross(bean.getRef2MarketGross().getGross()));
                    }
                    else {
                        bean.setRef2("");
                    }
                }
                if (bean.getMarketGross() != null) {
                    if (bean.getMarketGross().getGross() > 0) {
                        bean.setGrossText(FormatUtil.formatUsGross(bean.getMarketGross().getGross()));
                    }
                    else {
                        bean.setGrossText("");
                    }
                }
                break;
        }
    }

    private void fillMovie(long movieId, List<EditMarketGrossBean> list, int movieIndex) {
        MarketGrossDao dao = getDaoSession().getMarketGrossDao();
        for (EditMarketGrossBean bean:list) {
            long marketId = 0;
            if (bean.getBean() != null) {
                marketId = bean.getBean().getId();
            }
            MarketGross gross = dao.queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movieId))
                    .where(MarketGrossDao.Properties.MarketId.eq(marketId))
                    .build().unique();
            if (gross == null) {
                gross = new MarketGross();
                bean.setMarketGross(gross);
                bean.setMarkColor(getUnMarkedColor());
            }
            else  {
                if (movieIndex == 0) {
                    bean.setRef1MarketGross(gross);
                }
                else if (movieIndex == 1) {
                    bean.setRef2MarketGross(gross);
                }
                else {
                    bean.setMarketGross(gross);
                    bean.setMarkColor(getMarkedColor());
                }
            }
            setPresentText(bean);
        }
    }

    public int getUnMarkedColor() {
        return Color.WHITE;
    }

    public int getMarkedColor() {
        return Color.parseColor("#ffff00");
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
                        if (bean.isEdited()) {
                            MarketGross gross = bean.getMarketGross();
                            if (gross.getId() == null) {
                                insertList.add(gross);
                                gross.setMovieId(movieObserver.getValue().getId());
                                if (bean.getBean() != null) {
                                    gross.setMarketId(bean.getBean().getId());
                                }
                                DebugLog.e("insert " + bean.getMarket());
                            }
                            else {
                                // 有修改且数值都有效
                                if (gross.getGross() > 0 || gross.getOpening() > 0 || !TextUtils.isEmpty(gross.getDebut()) || !TextUtils.isEmpty(gross.getEndDate())) {
                                    updateList.add(gross);
                                    DebugLog.e("update " + bean.getMarket());
                                }
                                // 有修改且为删除
                                else {
                                    deleteList.add(gross);
                                    DebugLog.e("delete " + bean.getMarket());
                                }
                            }
                        }
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

    public void onEditTypeChanged(int type) {
        mEditType = type;
        typeChanged()
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
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<List<EditMarketGrossBean>> typeChanged() {
        return Observable.create(e -> {
            if (marketsObserver.getValue() != null) {
                for (EditMarketGrossBean bean:marketsObserver.getValue()) {
                    setPresentText(bean);
                }
            }
            e.onNext(marketsObserver.getValue());
        });
    }

    public boolean updateTypeValue(EditMarketGrossBean data, String name) {
        try {
            switch (mEditType) {
                case TYPE_DEBUT:
                    if (TextUtils.isEmpty(name)) {
                        data.getMarketGross().setDebut("");
                    }
                    else {
                        data.getMarketGross().setDebut(name);
                    }
                    break;
                case TYPE_END:
                    if (TextUtils.isEmpty(name)) {
                        data.getMarketGross().setEndDate("");
                    }
                    else {
                        data.getMarketGross().setEndDate(name);
                    }
                    break;
                case TYPE_OPENING:
                    long opening = Long.parseLong(name);
                    data.getMarketGross().setOpening(opening);
                    break;
                case TYPE_TOTAL:
                    long gross = Long.parseLong(name);
                    data.getMarketGross().setGross(gross);
                    break;
            }
            // 除非修改了日期，否则自动设置debut
            if (TextUtils.isEmpty(data.getMarketGross().getDebut())) {
                data.getMarketGross().setDebut(movieObserver.getValue().getDebut());
            }
            data.setEdited(true);
            setPresentText(data);
            data.setMarkColor(getMarkedColor());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getEditInitText(EditMarketGrossBean data) {
        String text;
        switch (mEditType) {
            case TYPE_DEBUT:
                text = data.getMarketGross().getDebut();
                break;
            case TYPE_END:
                text = data.getMarketGross().getEndDate();
                break;
            case TYPE_OPENING:
                text = String.valueOf(data.getMarketGross().getOpening());
                break;
            default:
                text = String.valueOf(data.getMarketGross().getGross());
                break;
        }
        return text;
    }
}
