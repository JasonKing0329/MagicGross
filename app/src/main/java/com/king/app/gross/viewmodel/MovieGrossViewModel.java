package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.GrossDateType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.model.gross.WeekendModel;
import com.king.app.gross.model.gross.WeeklyModel;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.viewmodel.bean.GrossPage;
import com.king.app.gross.viewmodel.bean.SimpleGross;
import com.king.app.gross.viewmodel.bean.WeekGross;

import java.util.List;

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

    public MutableLiveData<Gross> editObserver = new MutableLiveData<>();

    private Movie mMovie;

    private GrossDateType mDateType;

    private DailyModel dailyModel;

    private WeeklyModel weeklyModel;

    private WeekendModel weekendModel;

    public MovieGrossViewModel(@NonNull Application application) {
        super(application);
        mDateType = GrossDateType.DAILY;
    }

    public void loadMovie(long movieId) {
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

    public void onGrossChanged(Gross gross) {
        DebugLog.e("region " + gross.getRegion());
        onGrossRegionChanged(gross.getRegion());
    }

    public void onGrossRegionChanged(int region) {
        if (region == Region.NA.ordinal()) {
            loadRegion(Region.NA);
            loadWorldWide();
        }
        else if (region == Region.CHN.ordinal()) {
            loadRegion(Region.CHN);
            loadOversea();
            loadWorldWide();
        }
        else if (region == Region.OVERSEA_NO_CHN.ordinal()) {
            loadRegion(Region.OVERSEA_NO_CHN);
            loadOversea();
            loadWorldWide();
        }
    }

    /**
     * 加载数据库中存在region的gross
     * @param region 只支持NA, CHN, OVERSEA_NO_CHN
     */
    public void loadRegion(Region region) {
        switch (mDateType) {
            case WEEKEND:
                loadWeekend(region);
                break;
            case WEEKLY:
                loadWeekly(region);
                break;
            default:
                loadDaily(region);
                break;
        }
    }

    private void loadOversea() {
        switch (mDateType) {
            case WEEKEND:
                loadWeekendOversea();
                break;
            case WEEKLY:
                loadWeeklyOversea();
                break;
            default:
                loadDailyOversea();
                break;
        }
    }

    private void loadWorldWide() {
        switch (mDateType) {
            case WEEKEND:
                loadWeekendWorldWide();
                break;
            case WEEKLY:
                loadWeeklyWorldWide();
                break;
            default:
                loadDailyWorldWide();
                break;
        }
    }

    private void loadDaily(Region region) {
        if (region == Region.OVERSEA) {
            loadDailyOversea();
        }
        else if (region == Region.WORLDWIDE) {
            loadDailyWorldWide();
        }
        else {
            loadDailyRegion(region);
        }
    }

    public void loadWeekly(Region region) {
        if (region == Region.OVERSEA) {
            loadWeeklyOversea();
        }
        else if (region == Region.WORLDWIDE) {
            loadWeeklyWorldWide();
        }
        else {
            loadWeeklyRegion(region);
        }
    }

    public void loadWeekend(Region region) {
        if (region == Region.OVERSEA) {
            loadWeekendOversea();
        }
        else if (region == Region.WORLDWIDE) {
            loadWeekendWorldWide();
        }
        else {
            loadWeekendRegion(region);
        }
    }

    private void loadDailyRegion(Region region) {
        dailyModel.queryGross(region.ordinal())
                .flatMap(list -> dailyModel.toSimpleGross(list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<SimpleGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<SimpleGross> simpleGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.DAILY;
                        page.region = region;
                        page.list = simpleGrosses;

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

    private void loadDailyWorldWide() {
        dailyModel.queryWorldWide()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<SimpleGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<SimpleGross> simpleGrosses) {
                        GrossPage page = new GrossPage();
                        page.region = Region.WORLDWIDE;
                        page.dateType = GrossDateType.DAILY;
                        page.list = simpleGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<SimpleGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<SimpleGross> simpleGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.DAILY;
                        page.region = Region.OVERSEA;
                        page.list = simpleGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKLY;
                        page.region = region;
                        page.weekList = weekGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKLY;
                        page.region = Region.OVERSEA;
                        page.weekList = weekGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKLY;
                        page.region = Region.WORLDWIDE;
                        page.weekList = weekGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKEND;
                        page.region = region;
                        page.weekList = weekGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKEND;
                        page.region = Region.WORLDWIDE;
                        page.weekList = weekGrosses;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<WeekGross>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<WeekGross> weekGrosses) {
                        GrossPage page = new GrossPage();
                        page.dateType = GrossDateType.WEEKEND;
                        page.region = Region.OVERSEA;
                        page.weekList = weekGrosses;
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

    public void editGross(SimpleGross data) {
        // na, chn, oversea_no_chn
        if (data.getBean() != null) {
            editObserver.setValue(data.getBean());
        }
    }

    public void deleteGross(Gross gross) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        dao.delete(gross);
        dao.detachAll();
        onGrossRegionChanged(gross.getRegion());
    }

    public void setDateType(GrossDateType type) {
        mDateType = type;
    }

    public GrossDateType getDateType() {
        return mDateType;
    }
}
