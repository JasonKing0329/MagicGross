package com.king.app.gross.model.gross;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/9 0009 12:21
 */

public class DailyModel {

    private Movie mMovie;

    public DailyModel(Movie mMovie) {
        this.mMovie = mMovie;
    }

    public Observable<List<Gross>> queryGross(int region) {
        return Observable.create(e -> {
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            e.onNext(list);
        });
    }

    public Observable<List<SimpleGross>> queryOversea() {
        return Observable.create(e -> {
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> chnList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> overseaList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            int size = Math.max(overseaList.size(), chnList.size());

            List<SimpleGross> list = new ArrayList<>();
            long sum = 0;
            for (int i = 0; i < size; i ++) {
                Gross gross = null;
                if (i < chnList.size()) {
                    gross = chnList.get(i);
                }
                if (i < overseaList.size()) {
                    gross = overseaList.get(i);
                }
                long nGross = getOverseaGross(i, chnList, overseaList);
                sum += nGross;

                SimpleGross sg = new SimpleGross();
                sg.setDay(gross.getDay());
                sg.setDayOfWeek(gross.getDayOfWeek());
                if (i > 0) {
                    long last = getOverseaGross(i - 1, chnList, overseaList);
                    double drop = (double) (nGross - last) / (double) last;
                    sg.setDropDay(FormatUtil.formatDrop(drop));
                }
                if (i > 6) {
                    long last = getOverseaGross(i - 7, chnList, overseaList);
                    double drop = (double) (nGross - last) / (double) last;
                    sg.setDropWeek(FormatUtil.formatDrop(drop));
                }
                // 当日都以单位为万显示
                sg.setGrossDay(FormatUtil.pointZZ((double) nGross / (double) 10000));
                // 累计以单位为个位显示
                sg.setGrossSum(FormatUtil.formatUsGross(sum));
                list.add(sg);
            }
            e.onNext(list);
        });
    }

    private long getOverseaGross(int i, List<Gross> chnList, List<Gross> overseaList) {
        long grossChn = 0;
        long grossOversea = 0;
        if (i < chnList.size()) {
            grossChn = (long) (chnList.get(i).getGross() / mMovie.getUsToYuan());
        }
        if (i < overseaList.size()) {
            grossOversea = overseaList.get(i).getGross();
        }
        return grossChn + grossOversea;
    }

    public Observable<List<SimpleGross>> queryWorldWide() {
        return Observable.create(e -> {
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> naList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> chnList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> overseaList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            int size = Math.max(naList.size(), chnList.size());
            size = Math.max(size, overseaList.size());

            List<SimpleGross> list = new ArrayList<>();
            long sum = 0;
            for (int i = 0; i < size; i ++) {
                Gross gross = null;
                if (i < naList.size()) {
                    gross = naList.get(i);
                }
                if (i < chnList.size()) {
                    gross = chnList.get(i);
                }
                if (i < overseaList.size()) {
                    gross = overseaList.get(i);
                }
                long nGross = getWorldGross(i, naList, chnList, overseaList);
                sum += nGross;

                SimpleGross sg = new SimpleGross();
                sg.setDay(gross.getDay());
                sg.setDayOfWeek(gross.getDayOfWeek());
                if (i > 0) {
                    long last = getWorldGross(i - 1, naList, chnList, overseaList);
                    double drop = (double) (nGross - last) / (double) last;
                    sg.setDropDay(FormatUtil.formatDrop(drop));
                }
                if (i > 6) {
                    long last = getWorldGross(i - 7, naList, chnList, overseaList);
                    double drop = (double) (nGross - last) / (double) last;
                    sg.setDropWeek(FormatUtil.formatDrop(drop));
                }
                // 当日都以单位为万显示
                sg.setGrossDay(FormatUtil.pointZZ((double) nGross / (double) 10000));
                // 累计以单位为个位显示
                sg.setGrossSum(FormatUtil.formatUsGross(sum));
                list.add(sg);
            }
            e.onNext(list);
        });
    }

    private long getWorldGross(int i, List<Gross> naList, List<Gross> chnList, List<Gross> overseaList) {
        long grossChn = 0;
        long grossOversea = 0;
        long grossNa = 0;
        if (i < naList.size()) {
            grossNa = naList.get(i).getGross();
        }
        if (i < chnList.size()) {
            grossChn = (long) (chnList.get(i).getGross() / mMovie.getUsToYuan());
        }
        if (i < overseaList.size()) {
            grossOversea = overseaList.get(i).getGross();
        }
        return grossNa + grossChn + grossOversea;
    }

    public Observable<List<SimpleGross>> toSimpleGross(List<Gross> grosses) {
        return Observable.create(e -> {
            List<SimpleGross> list = new ArrayList<>();
            long sum = 0;
            for (int i = 0; i < grosses.size(); i ++) {
                Gross gross = grosses.get(i);
                sum += gross.getGross();

                SimpleGross sg = new SimpleGross();
                sg.setBean(gross);
                sg.setDay(gross.getDay());
                sg.setDayOfWeek(gross.getDayOfWeek());
                if (i > 0) {
                    long last = grosses.get(i - 1).getGross();
                    double drop = (double) (gross.getGross() - last) / (double) last;
                    sg.setDropDay(FormatUtil.formatDrop(drop));
                }
                if (i > 6) {
                    long last = grosses.get(i - 7).getGross();
                    double drop = (double) (gross.getGross() - last) / (double) last;
                    sg.setDropWeek(FormatUtil.formatDrop(drop));
                }
                // 当日都以单位为万显示
                sg.setGrossDay(FormatUtil.pointZZ((double) gross.getGross() / (double) 10000));
                // 累计以单位为个位显示
                if (gross.getRegion() == Region.CHN.ordinal()) {
                    sg.setGrossSum(FormatUtil.formatChnGross(sum));
                }
                else {
                    sg.setGrossSum(FormatUtil.formatUsGross(sum));
                }
                list.add(sg);
            }
            e.onNext(list);
        });
    }

}
