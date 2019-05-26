package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.view.View;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.http.mojo.MojoClient;
import com.king.app.gross.model.http.mojo.MojoParser;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MojoDefaultBean;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/14 16:33
 */
public class ParseMojoViewModel extends BaseViewModel {

    public ObservableInt isDailyAcquired = new ObservableInt(View.GONE);

    public ObservableInt isMarketAcquired = new ObservableInt(View.GONE);

    public ObservableField<String> defaultText = new ObservableField<>();

    public MutableLiveData<Boolean> notifyDailyDataChanged = new MutableLiveData<>();

    public MutableLiveData<Boolean> notifyTotalDataChanged = new MutableLiveData<>();

    public MutableLiveData<String> insertLeftPopup = new MutableLiveData<>();

    private Movie mMovie;

    private MojoParser mojoParser;

    private MojoDefaultBean mojoDefaultBean;

    private Gross leftGross;

    public ParseMojoViewModel(@NonNull Application application) {
        super(application);
        mojoParser = new MojoParser();
    }

    public void loadDefaultData(Movie movie) {

        mMovie = movie;
        long count = getDaoSession().getMarketGrossDao().queryBuilder()
                .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                .buildCount().count();
        if (count > 0) {
            isMarketAcquired.set(View.VISIBLE);
        }

        count = getDaoSession().getGrossDao().queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .buildCount().count();
        if (count > 10) {
            isDailyAcquired.set(View.VISIBLE);
        }
    }

    public void fetchDailyData(boolean clearAll) {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(mojoParser.getMojoDailyUrl(mMovie.getMojoId()))
                .flatMap(responseBody -> mojoParser.saveFile(responseBody, AppConfig.FILE_HTML_DAILY))
                .flatMap(file -> mojoParser.parseDaily(file, mMovie.getId(), clearAll))
//        mojoParser.parseDaily(new File(AppConfig.FILE_HTML_DAILY), mMovie.getId())
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
                        notifyDailyDataChanged.setValue(true);
                        isDailyAcquired.set(View.VISIBLE);
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

    public void fetchMarketData(boolean clearAll) {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(mojoParser.getMojoForeignUrl(mMovie.getMojoId()))
                .flatMap(responseBody -> mojoParser.saveFile(responseBody, AppConfig.FILE_HTML_FOREIGN))
                .flatMap(file -> mojoParser.parseForeign(file, mMovie.getId(), clearAll))
//        mojoParser.parseForeign(new File(AppConfig.FILE_HTML_FOREIGN), mMovie.getId())
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
                        messageObserver.setValue("解析并保存成功");
                        isMarketAcquired.set(View.VISIBLE);
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

    public void fetchDefaultData() {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(mojoParser.getMojoDefaultUrl(mMovie.getMojoId()))
                .flatMap(responseBody -> mojoParser.saveFile(responseBody, AppConfig.FILE_HTML_DEFAULT))
                .flatMap(file -> mojoParser.parseDefault(file))
//        mojoParser.parseDefault(new File(AppConfig.FILE_HTML_DEFAULT))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<MojoDefaultBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(MojoDefaultBean bean) {
                        loadingObserver.setValue(false);
                        mojoDefaultBean = bean;
                        setDefaultText(bean);
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

    private void setDefaultText(MojoDefaultBean bean) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Domestic:").append(FormatUtil.formatUsGross(bean.getDomestic()))
                .append(" Foreign:").append(FormatUtil.formatUsGross(bean.getForeign()))
                .append(" Worldwide:").append(FormatUtil.formatUsGross(bean.getWorldwide()));
        defaultText.set(buffer.toString());
    }

    public void insertTotal() {
        if (mojoDefaultBean == null) {
            messageObserver.setValue("未获取default页面");
            return;
        }
        Gross gross = getDaoSession().getGrossDao().queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.IsTotal.eq(1))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                .build().unique();
        if (gross == null) {
            gross = new Gross();
            gross.setMovieId(mMovie.getId());
            gross.setIsTotal(1);
            gross.setRegion(Region.OVERSEA.ordinal());
            gross.setSymbol(0);
        }
        gross.setGross(mojoDefaultBean.getForeign());
        getDaoSession().getGrossDao().insertOrReplace(gross);

        gross = getDaoSession().getGrossDao().queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.IsTotal.eq(1))
                .where(GrossDao.Properties.Region.eq(Region.WORLDWIDE.ordinal()))
                .build().unique();
        if (gross == null) {
            gross = new Gross();
            gross.setMovieId(mMovie.getId());
            gross.setIsTotal(1);
            gross.setRegion(Region.WORLDWIDE.ordinal());
            gross.setSymbol(0);
        }
        gross.setGross(mojoDefaultBean.getWorldwide());
        getDaoSession().getGrossDao().insertOrReplace(gross);
        
        messageObserver.setValue("Success");
        notifyTotalDataChanged.setValue(true);
    }

    public void insertLeft() {
        if (mojoDefaultBean == null) {
            messageObserver.setValue("未获取default页面");
            return;
        }
        String sql = "SELECT SUM(gross),COUNT(*) FROM gross WHERE movie_id=? AND region=0 AND is_left_after_day=0 AND is_total=0";
        Cursor cursor = MApplication.getInstance().getDatabase().rawQuery(sql, new String[]{String.valueOf(mMovie.getId())});

        StringBuffer buffer = new StringBuffer();
        if (cursor.moveToNext()) {
            long sum = cursor.getLong(0);
            int day = cursor.getInt(1);
            long left = mojoDefaultBean.getDomestic() - sum;
            buffer.append("当前").append(day).append("天累计").append(FormatUtil.formatUsGross(sum))
                    .append(". ").append("将插入余量为").append(FormatUtil.formatUsGross(left))
                    .append(". 是否继续？");

            leftGross = getDaoSession().getGrossDao().queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.IsLeftAfterDay.gt(0))
                    .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                    .build().unique();
            if (leftGross == null) {
                leftGross = new Gross();
                leftGross.setIsLeftAfterDay(day);
                leftGross.setRegion(Region.NA.ordinal());
                leftGross.setMovieId(mMovie.getId());
                leftGross.setSymbol(0);
            }
            // 排序是根据这个字段排的
            leftGross.setDay(day + 1);
            leftGross.setGross(left);

            insertLeftPopup.setValue(buffer.toString());
        }
    }

    public void confirmInsertLeft() {
        getDaoSession().getGrossDao().insertOrReplace(leftGross);

        messageObserver.setValue("Success");
        // 通知daily数据变化
        notifyDailyDataChanged.setValue(true);
    }
}
