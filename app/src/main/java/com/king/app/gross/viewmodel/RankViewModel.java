package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.view.View;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.RankType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.RankModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.model.single.DateRangeInstance;
import com.king.app.gross.viewmodel.bean.RankItem;
import com.king.app.gross.viewmodel.bean.RankTag;

import org.greenrobot.greendao.query.QueryBuilder;

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
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 21:24
 */

public class RankViewModel extends BaseViewModel {

    public MutableLiveData<List<RankTag>> regionTagsObserver = new MutableLiveData<>();
    public MutableLiveData<List<RankTag>> typeTagsObserver = new MutableLiveData<>();
    public MutableLiveData<List<RankItem>> itemsObserver = new MutableLiveData<>();

    public ObservableField<String> titleValueText = new ObservableField<>();
    public ObservableField<String> titleRateText = new ObservableField<>();
    public ObservableInt titleRateVisibility = new ObservableInt();

    private Region mRegion;

    private RankType mRankType;

    private RankModel rankModel;

    public RankViewModel(@NonNull Application application) {
        super(application);
        mRegion = Region.NA;
        mRankType = RankType.TOTAL;
        rankModel = new RankModel();
    }

    public void load() {
        updateRankTitle();
        loadRegionTags();
        loadTypeTags();
        loadMovies();
    }

    private void loadRegionTags() {
        List<RankTag> tags = new ArrayList<>();
        for (int i = 0; i < AppConstants.REGION_TITLES.length; i ++) {
            RankTag tag = new RankTag();
            tag.setBean(Region.values()[i]);
            tag.setTag(AppConstants.REGION_TITLES[i]);
            tags.add(tag);
        }
        regionTagsObserver.setValue(tags);
    }

    private void loadTypeTags() {
        List<RankTag> tags = new ArrayList<>();
        for (int i = 0; i < AppConstants.RANK_TYPE_TITLES.length; i ++) {
            RankTag tag = new RankTag();
            tag.setBean(RankType.values()[i]);
            tag.setTag(AppConstants.RANK_TYPE_TITLES[i]);
            tags.add(tag);
        }
        typeTagsObserver.setValue(tags);
    }

    private void loadMovies() {
        queryMovies()
                .flatMap(movies -> toRankItems(movies))
                .flatMap(list -> sortItems(list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<RankItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<RankItem> rankItems) {
                        itemsObserver.setValue(rankItems);
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

    private Observable<List<Movie>> queryMovies() {
        return Observable.create(e -> {
            QueryBuilder<Movie> builder = getDaoSession().getMovieDao().queryBuilder();
            if (!SettingProperty.isEnableVirtualMovie()) {
                builder.where(MovieDao.Properties.IsReal.eq(AppConstants.MOVIE_REAL));
            }
            if (DateRangeInstance.getInstance().getStartDate() != null) {
                builder.where(MovieDao.Properties.Debut.ge(DateRangeInstance.getInstance().getStartDate()));
            }
            if (DateRangeInstance.getInstance().getEndDate() != null) {
                builder.where(MovieDao.Properties.Debut.le(DateRangeInstance.getInstance().getEndDate()));
            }
            List<Movie> list = builder.build().list();
            e.onNext(list);
        });
    }

    private Observable<List<RankItem>> toRankItems(List<Movie> movies) {
        return Observable.create(e -> {
            List<RankItem> items = new ArrayList<>();
            for (Movie movie:movies) {
                RankItem item = rankModel.convertMovie(movie, mRegion, mRankType);
                // real的影片基本上只有na或者只有chn，所以数据为0的movie不出现在rank列表中
                if (item.getSortValue() > 0) {
                    items.add(item);
                }
            }
            e.onNext(items);
        });
    }

    private Observable<List<RankItem>> sortItems(List<RankItem> list) {
        return Observable.create(e -> {
            Collections.sort(list, new ValueComparator());
            for (int i = 0; i < list.size(); i ++) {
                list.get(i).setRank(String.valueOf(i + 1));
            }
            e.onNext(list);
        });
    }

    public void changeRegion(Region region) {
        if (mRegion != region) {
            mRegion = region;
            loadMovies();
            updateRankTitle();
        }
    }

    public void changeRankType(RankType type) {
        if (mRankType != type) {
            mRankType = type;
            loadMovies();
            updateRankTitle();
        }
    }

    private void updateRankTitle() {
        // 仅total与opening计算相对全球占比
        if ((mRankType == RankType.TOTAL || mRankType == RankType.OPENING)
                && mRegion != Region.WORLDWIDE) {
            titleRateText.set("占比");
            titleRateVisibility.set(View.VISIBLE);
        }
        else {
            titleRateVisibility.set(View.GONE);
        }

        // 后劲指数
        if (mRankType == RankType.RATE) {
            titleValueText.set("指数");
        }
        else if (mRankType == RankType.BUDGET || mRankType == RankType.BUDGET_RATE) {
            titleRateText.set("回报率");
            titleValueText.set("Budget");
            titleRateVisibility.set(View.VISIBLE);
        }
        else {
            titleValueText.set("票房");
        }
    }

    public boolean isShowRate() {
        return mRankType == RankType.TOTAL || mRankType == RankType.OPENING || mRankType == RankType.BUDGET || mRankType == RankType.BUDGET_RATE;
    }

    private class ValueComparator implements Comparator<RankItem> {

        @Override
        public int compare(RankItem left, RankItem right) {
            if (left.getSortValue() - right.getSortValue() > 0) {
                return -1;
            }
            else if (left.getSortValue() - right.getSortValue() < 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
