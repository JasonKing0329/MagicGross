package com.king.app.gross.model.gross;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.RankType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.RankItem;

import java.util.List;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 21:49
 */

public class RankModel {

    public RankItem convertMovie(Movie movie, Region region, RankType mRankType) {
        RankItem item = new RankItem();
        item.setName(movie.getName());
        item.setSubName(movie.getSubName());
        item.setMovie(movie);
        switch (mRankType) {
            case TOTAL:
                loadTotalValue(movie, region, item);
                break;
            case OPENING:
                loadOpeningValue(movie, region, item);
                break;
            case RATE:
                loadRateValue(movie, region, item);
                break;
        }
        return item;
    }

    private void loadTotalValue(Movie movie, Region region, RankItem item) {
        long gross = queryTotalGross(movie, region.ordinal());
        item.setSortValue(gross);
        if (region == Region.CHN) {
            item.setValue(FormatUtil.formatChnGross(gross));
        }
        else {
            item.setValue(FormatUtil.formatUsGross(gross));
        }
    }

    private void loadOpeningValue(Movie movie, Region region, RankItem item) {
        long gross = queryOpeningGross(movie, region.ordinal());
        item.setSortValue(gross);
        if (region == Region.CHN) {
            item.setValue(FormatUtil.formatChnGross(gross));
        }
        else {
            item.setValue(FormatUtil.formatUsGross(gross));
        }
    }

    private void loadRateValue(Movie movie, Region region, RankItem item) {
        long total = queryTotalGross(movie, region.ordinal());
        long opening = queryOpeningGross(movie, region.ordinal());
        item.setSortValue((double) total / (double) opening);
        item.setValue(FormatUtil.pointZ(item.getSortValue()));
    }

    public long queryOpeningGross(Movie mMovie, int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        long sum = 0;
        if (region < Region.OVERSEA.ordinal()) {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .where(GrossDao.Properties.Day.lt(4))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
            }
        }
        else {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.Day.lt(4))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += (gross.getGross() / mMovie.getUsToYuan());
            }
            list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.Day.lt(4))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
            }

            if (region == Region.WORLDWIDE.ordinal()) {
                list = dao.queryBuilder()
                        .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .where(GrossDao.Properties.Day.lt(4))
                        .orderAsc(GrossDao.Properties.Day)
                        .build().list();
                for (Gross gross:list) {
                    sum += gross.getGross();
                }
            }
        }
        return sum;
    }

    private long queryTotalGross(Movie mMovie, int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        long sum = 0;
        if (region < Region.OVERSEA.ordinal()) {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
            }
        }
        else {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += (gross.getGross() / mMovie.getUsToYuan());
            }
            list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
            }

            if (region == Region.WORLDWIDE.ordinal()) {
                list = dao.queryBuilder()
                        .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .orderAsc(GrossDao.Properties.Day)
                        .build().list();
                for (Gross gross:list) {
                    sum += gross.getGross();
                }
            }
        }
        return sum;
    }
}
