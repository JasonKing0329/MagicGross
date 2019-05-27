package com.king.app.gross.model.gross;

import android.database.Cursor;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.GrossStatDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.utils.DebugLog;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StatModel {

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

    public void statOversea(Movie movie) {
        long[] markets = sumMarket(movie);
        long undisOpenSum = 0;
        long undisSum = 0;
        MarketGross undisclosed = getUndisclosed(movie);
        if (undisclosed != null) {
            undisOpenSum = undisclosed.getOpening();
            undisSum = undisclosed.getGross();
        }
        insertOversea(movie, markets[0], markets[1], undisOpenSum, undisSum);
    }

    public void statWorldWide(Movie movie) {
        long[] markets = sumMarket(movie);
        long undisOpenSum = 0;
        long undisSum = 0;
        MarketGross undisclosed = getUndisclosed(movie);
        if (undisclosed != null) {
            undisOpenSum = undisclosed.getOpening();
            undisSum = undisclosed.getGross();
        }
        long naOpen = 0;
        long na = 0;
        GrossStat stat = getGrossStat(movie);
        if (stat != null) {
            naOpen = stat.getUsOpening();
            na = stat.getUs();
        }
        insertWorld(movie, markets[0], markets[1], undisOpenSum, undisSum, naOpen, na);
    }

    public long[] sumMarket(Movie movie) {
        long marketSum = 0;
        long marketOpenSum = 0;
        Database database = MApplication.getInstance().getDatabase();
        String sql = "SELECT SUM(opening), SUM(gross) FROM %s WHERE %s=? AND %s>?";
        sql = String.format(sql, MarketGrossDao.TABLENAME, MarketGrossDao.Properties.MovieId.columnName, MarketGrossDao.Properties.MarketId.columnName);
        Cursor cursor = database.rawQuery(sql, new String[]{String.valueOf(movie.getId()), String.valueOf(AppConstants.MARKET_TOTAL_ID)});
        if (cursor.moveToNext()) {
            marketOpenSum = cursor.getLong(0);
            marketSum = cursor.getLong(1);
        }
        return new long[]{marketOpenSum, marketSum};
    }

    public MarketGross getUndisclosed(Movie movie) {
        MarketGross marketGross = MApplication.getInstance().getDaoSession().getMarketGrossDao().queryBuilder()
                .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                .where(MarketGrossDao.Properties.MarketId.eq(AppConstants.MARKET_UNDISCLOSED_ID))
                .build().unique();
        return marketGross;
    }

    private GrossStat getGrossStat(Movie movie) {
        GrossStat stat = MApplication.getInstance().getDaoSession().getGrossStatDao().queryBuilder()
                .where(GrossStatDao.Properties.MovieId.eq(movie.getId()))
                .build().unique();
        return stat;
    }

    private void insertWorld(Movie movie, long marketOpen, long market, long undisOpen, long undis, long naOpen, long na) {
        GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
        Gross world = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.WORLDWIDE.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().unique();
        if (world == null) {
            world = new Gross();
            world.setMovieId(movie.getId());
            world.setIsTotal(AppConstants.GROSS_IS_TOTAL);
            world.setRegion(Region.WORLDWIDE.ordinal());
            DebugLog.e("insert world");
        }
        else {
            DebugLog.e("update world");
        }
        world.setGross(market + undis + na);
        grossDao.insertOrReplace(world);

        world = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.WORLDWIDE.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().unique();
        if (world == null) {
            world = new Gross();
            world.setMovieId(movie.getId());
            world.setIsTotal(AppConstants.GROSS_IS_OPENING);
            world.setRegion(Region.WORLDWIDE.ordinal());
            DebugLog.e("insert world opening");
        }
        else {
            DebugLog.e("update world opening");
        }
        world.setGross(marketOpen + undisOpen + naOpen);
        grossDao.insertOrReplace(world);
        grossDao.detachAll();
    }

    private void insertOversea(Movie movie, long marketOpen, long market, long undisOpen, long undis) {
        GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
        Gross oversea = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().unique();
        if (oversea == null) {
            oversea = new Gross();
            oversea.setMovieId(movie.getId());
            oversea.setIsTotal(AppConstants.GROSS_IS_TOTAL);
            oversea.setRegion(Region.OVERSEA.ordinal());
            DebugLog.e("insert oversea");
        }
        else {
            DebugLog.e("update oversea");
        }
        oversea.setGross(market + undis);
        grossDao.insertOrReplace(oversea);

        oversea = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().unique();
        if (oversea == null) {
            oversea = new Gross();
            oversea.setMovieId(movie.getId());
            oversea.setIsTotal(AppConstants.GROSS_IS_OPENING);
            oversea.setRegion(Region.OVERSEA.ordinal());
            DebugLog.e("insert oversea opening");
        }
        else {
            DebugLog.e("update oversea opening");
        }
        oversea.setGross(marketOpen + undisOpen);
        grossDao.insertOrReplace(oversea);
        grossDao.detachAll();
    }

}
