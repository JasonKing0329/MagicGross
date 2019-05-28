package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.GrossDateType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.ChartModel;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.model.gross.WeekendModel;
import com.king.app.gross.model.gross.WeeklyModel;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.utils.ListUtil;
import com.king.app.gross.viewmodel.bean.GrossPage;
import com.king.app.gross.viewmodel.bean.SimpleGross;
import com.king.app.gross.viewmodel.bean.WeekGross;

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
 * @date: 2018/6/7 15:00
 */
public class MovieGrossViewModel extends BaseViewModel {

    public MutableLiveData<String> titleText = new MutableLiveData<>();

    public MutableLiveData<GrossPage> grossObserver = new MutableLiveData<>();

    private Movie mMovie;

    private int mRegion;

    private GrossDateType mDateType;

    private DailyModel dailyModel;

    private WeeklyModel weeklyModel;

    private WeekendModel weekendModel;

    private ChartModel chartModel;

    private StatModel statModel;

    private int scrollPosition;
    private boolean enableScroll;
    public MutableLiveData<Integer> forceScrollTo = new MutableLiveData<>();

    public MovieGrossViewModel(@NonNull Application application) {
        super(application);
        mDateType = GrossDateType.DAILY;
        chartModel = new ChartModel();
        statModel = new StatModel();
    }

    public void loadMovie(long movieId, int region) {
        mRegion = region;
        try {
            mMovie = MApplication.getInstance().getDaoSession().getMovieDao()
                    .queryBuilder()
                    .where(MovieDao.Properties.Id.eq(movieId))
                    .build().unique();
            if (TextUtils.isEmpty(mMovie.getSubName())) {
                titleText.setValue(mMovie.getName());
            }
            else {
                titleText.setValue(mMovie.getName() + "\n" + mMovie.getSubName());
            }

            dailyModel = new DailyModel(mMovie);
            weeklyModel = new WeeklyModel(mMovie);
            weekendModel = new WeekendModel(mMovie);
        } catch (Exception e) {
            e.printStackTrace();
            messageObserver.setValue(e.getMessage());
        }
    }

    public Movie getMovie() {
        return mMovie;
    }

    public int getGrossSize() {
        return grossObserver.getValue() == null ? 0:ListUtil.getSize(grossObserver.getValue().list);
    }

    public void onGrossChanged(Gross gross, int scrollPosition) {
        DebugLog.e("region " + gross.getRegion());
        // 重新统计movie_stat表里的数据
        statistic();

        enableScroll = true;
        this.scrollPosition = scrollPosition;
        onGrossRegionChanged(gross.getRegion());
    }

    /**
     * 统计movie_stat表里的数据
     */
    public void statistic() {
        Observable<GrossStat> observable;
        if (AppConstants.MOVIE_REAL == mMovie.getIsReal()) {
            observable = statModel.statisticMovie(mMovie);
        }
        else {
            if (mRegion == Region.CHN.ordinal()) {
                observable = statModel.statVirtualChn(mMovie);
            }
            else {
                observable = statModel.statVirtualNa(mMovie);
            }
        }
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossStat>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossStat grossStat) {
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

    public void onGrossRegionChanged(int region) {
        if (region == Region.NA.ordinal()) {
            loadRegion(Region.NA);
        }
        else if (region == Region.CHN.ordinal()) {
            loadRegion(Region.CHN);
        }
    }

    /**
     * 加载数据库中存在region的gross
     * @param region 只支持NA, CHN, OVERSEA_NO_CHN
     */
    public void loadRegion(Region region) {
        switch (mDateType) {
            case WEEKEND:
                loadWeekendRegion(region);
                break;
            case WEEKLY:
                loadWeeklyRegion(region);
                break;
            default:
                loadDailyRegion(region);
                break;
        }
    }

    private Observable<GrossPage> toDailyGrossPage(Region region, GrossDateType dateType, List<SimpleGross> list) {
        return Observable.create(e -> {
            GrossPage page = new GrossPage();
            page.dateType = dateType;
            page.region = region;
            page.list = list;
            long opening = dailyModel.queryOpeningGross(region.ordinal());
            if (region == Region.CHN) {
                page.opening = FormatUtil.formatChnGross(opening);
            }
            else {
                page.opening = FormatUtil.formatUsGross(opening);
            }
            long total = dailyModel.queryTotalGross(region.ordinal());
            if (region == Region.CHN) {
                page.total = FormatUtil.formatChnGross(total);
            }
            else {
                page.total = FormatUtil.formatUsGross(total);
            }
            if (opening > 0) {
                page.rate = FormatUtil.pointZ((double) total / (double) opening);
            }

            page.axisYData = chartModel.createAxisData(region, list);
            page.lineData = chartModel.createLineData(region, list);
            e.onNext(page);
        });
    }

    private Observable<GrossPage> toWeekGrossPage(Region region, GrossDateType dateType, List<WeekGross> list) {
        return Observable.create(e -> {
            GrossPage page = new GrossPage();
            page.dateType = dateType;
            page.region = region;
            page.weekList = list;
            long opening = dailyModel.queryOpeningGross(region.ordinal());
            if (region == Region.CHN) {
                page.opening = FormatUtil.formatChnGross(opening);
            }
            else {
                page.opening = FormatUtil.formatUsGross(opening);
            }
            long total = dailyModel.queryTotalGross(region.ordinal());
            if (region == Region.CHN) {
                page.total = FormatUtil.formatChnGross(total);
            }
            else {
                page.total = FormatUtil.formatUsGross(total);
            }
            if (opening > 0) {
                page.rate = FormatUtil.pointZ((double) total / (double) opening);
            }
            e.onNext(page);
        });
    }

    private void loadDailyRegion(Region region) {
        dailyModel.queryGross(region.ordinal())
                .flatMap(list -> toDailyGrossPage(region, GrossDateType.DAILY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);

                        if (enableScroll) {
                            // 滚动后马上重置enableScroll
                            enableScroll = false;

                            forceScrollTo.setValue(scrollPosition);
                        }
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

    private void loadDailyWorldWide() {
        dailyModel.queryWorldWide()
                .flatMap(list -> toDailyGrossPage(Region.WORLDWIDE, GrossDateType.DAILY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadDailyOversea() {
        dailyModel.queryOversea()
                .flatMap(list -> toDailyGrossPage(Region.OVERSEA, GrossDateType.DAILY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeeklyRegion(Region region) {
        weeklyModel.queryWeeklyGross(region.ordinal())
                .flatMap(list -> toWeekGrossPage(region, GrossDateType.WEEKLY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeeklyOversea() {
        weeklyModel.queryWeeklyOversea()
                .flatMap(list -> toWeekGrossPage(Region.OVERSEA, GrossDateType.WEEKLY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeeklyWorldWide() {
        weeklyModel.queryWeeklyWorldWide()
                .flatMap(list -> toWeekGrossPage(Region.WORLDWIDE, GrossDateType.WEEKLY, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeekendRegion(Region region) {
        weekendModel.queryWeekendGross(region.ordinal())
                .flatMap(list -> toWeekGrossPage(region, GrossDateType.WEEKEND, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeekendWorldWide() {
        weekendModel.queryWeeklyWorldWide()
                .flatMap(list -> toWeekGrossPage(Region.WORLDWIDE, GrossDateType.WEEKEND, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    private void loadWeekendOversea() {
        weekendModel.queryWeekendOversea()
                .flatMap(list -> toWeekGrossPage(Region.OVERSEA, GrossDateType.WEEKEND, list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<GrossPage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(GrossPage page) {
                        grossObserver.setValue(page);
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

    public void deleteGross(Gross gross, int scrollToPosition) {
        enableScroll = true;
        this.scrollPosition = scrollToPosition;

        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        dao.delete(gross);
        dao.detachAll();
        statistic();
        onGrossRegionChanged(gross.getRegion());
    }

    public void setDateType(GrossDateType type) {
        mDateType = type;
    }

    public GrossDateType getDateType() {
        return mDateType;
    }

}
