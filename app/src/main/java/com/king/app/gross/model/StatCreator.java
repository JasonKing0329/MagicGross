package com.king.app.gross.model;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.GrossStatDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.utils.DebugLog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StatCreator {

    public Observable<GrossStat> statisticMovie(Movie movie) {
        return Observable.create(e -> {
            e.onNext(statisticMovieInstant(movie));
        });
    }

    public GrossStat statisticMovieInstant(Movie movie) {
        GrossStatDao dao = MApplication.getInstance().getDaoSession().getGrossStatDao();
        GrossStat stat = convertMovie(movie);
        DebugLog.e(movie.getName() + " us[" + stat.getUsOpening() + "," + stat.getUs() + "] chn["
            + stat.getChnOpening() + "," + stat.getChn() + "] ww[" + stat.getWorldOpening() + "," + stat.getWorld() + "]");
        GrossStat statInDb = dao.queryBuilder()
                .where(GrossStatDao.Properties.MovieId.eq(movie.getId()))
                .build().unique();
        if (statInDb == null) {
            dao.insert(stat);
            statInDb = stat;
        }
        else {
            statInDb.setUs(stat.getUs());
            statInDb.setUsOpening(stat.getUsOpening());
            statInDb.setChnOpening(stat.getChnOpening());
            statInDb.setChn(stat.getChn());
            statInDb.setWorldOpening(stat.getWorldOpening());
            statInDb.setWorld(stat.getWorld());
            dao.update(statInDb);
        }
        dao.detachAll();
        MApplication.getInstance().getDaoSession().getMovieDao().detach(movie);
        return statInDb;
    }

    /**
     * 慎用，用于统计全部movie
     */
    public void statisticAll() {
        queryMovies()
                .flatMap(list -> insertStatItems(list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<GrossStat>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<GrossStat> grossStats) {

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
            QueryBuilder<Movie> builder = MApplication.getInstance().getDaoSession().getMovieDao()
                    .queryBuilder();
            List<Movie> list = builder.build().list();
            e.onNext(list);
        });
    }

    private GrossStat convertMovie(Movie movie) {
        GrossStat item = new GrossStat();
        DailyModel model = new DailyModel(movie);
        item.setChn(model.queryTotalGross(Region.CHN.ordinal()));
        item.setChnOpening(model.queryOpeningGross(Region.CHN.ordinal()));
        item.setUs(model.queryTotalGross(Region.NA.ordinal()));
        item.setUsOpening(model.queryOpeningGross(Region.NA.ordinal()));
        item.setWorld(model.queryTotalGross(Region.WORLDWIDE.ordinal()));
        item.setWorldOpening(model.queryOpeningGross(Region.WORLDWIDE.ordinal()));
        item.setMovieId(movie.getId());
        return item;
    }

    private Observable<List<GrossStat>> insertStatItems(List<Movie> list) {
        return Observable.create(e -> {
            List<GrossStat> results = new ArrayList<>();
            for (Movie movie:list) {
                GrossStat item = convertMovie(movie);
                results.add(item);
            }
            MApplication.getInstance().getDaoSession().getGrossStatDao().deleteAll();
            MApplication.getInstance().getDaoSession().getGrossStatDao().insertInTx(results);
            MApplication.getInstance().getDaoSession().getGrossStatDao().detachAll();
            e.onNext(results);
        });
    }

}
