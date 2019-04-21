package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.http.mojo.MojoClient;
import com.king.app.gross.model.http.mojo.MojoConstants;
import com.king.app.gross.model.http.mojo.MojoParser;
import com.king.app.gross.utils.FileUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MojoViewModel extends BaseViewModel {

    private MojoParser parser;

    private Movie mMovie;

    public MutableLiveData<List<MarketGross>> grossObserver = new MutableLiveData<>();
    public MutableLiveData<Movie> movieObserver = new MutableLiveData<>();

    public MojoViewModel(@NonNull Application application) {
        super(application);
        parser = new MojoParser();
    }

    public void loadMovie(long movieId) {
        try {
            mMovie = MApplication.getInstance().getDaoSession().getMovieDao()
                    .queryBuilder()
                    .where(MovieDao.Properties.Id.eq(movieId))
                    .build().unique();
            movieObserver.setValue(mMovie);
        } catch (Exception e) {
            e.printStackTrace();
            messageObserver.setValue(e.getMessage());
            return;
        }

        loadGross();
    }

    private void loadGross() {
        loadMarketGross()
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
            List<MarketGross> grosses = MApplication.getInstance().getDaoSession().getMarketGrossDao()
                    .queryBuilder().where(MarketGrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .orderAsc(MarketGrossDao.Properties.MarketId)
                    .build().list();
            e.onNext(grosses);
        });
    }

    public void fetchForeignData() {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getMovieForeignPage(getMojoForeignUrl(mMovie.getMojoId()))
                .flatMap(responseBody -> saveFile(responseBody, AppConfig.FILE_HTML_FOREIGN))
                .flatMap(file -> parser.parse(file, mMovie.getId()))
//        parser.parse(new File(AppConfig.FILE_HTML_FOREIGN), mMovie.getId())
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

    private String getMojoForeignUrl(String movieId) {
        return MojoConstants.FOREIGN_URL + movieId + MojoConstants.URL_END;
    }

    private Observable<File> saveFile(ResponseBody responseBody, String path) {
        return Observable.create(e -> {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            e.onNext(FileUtil.saveFile(responseBody.byteStream(), path));
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
            builder.where(MarketGrossDao.Properties.MovieId.eq(mMovie.getId()));
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

}
