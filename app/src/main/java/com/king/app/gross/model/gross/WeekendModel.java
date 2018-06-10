package com.king.app.gross.model.gross;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.WeekGross;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/9 0009 12:40
 */

public class WeekendModel {
    private Movie mMovie;

    public WeekendModel(Movie mMovie) {
        this.mMovie = mMovie;
    }

    public Observable<List<WeekGross>> queryWeekendGross(int region) {
        return Observable.create(e -> {
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> grossList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<WeekendGross> weekendList = toWeekendGross(grossList);

            List<WeekGross> list = getListFrom(weekendList);
            e.onNext(list);
        });
    }

    /**
     * list is already sorted by day, asc
     * @param list
     * @return
     */
    private List<WeekendGross> toWeekendGross(List<Gross> list) {
        List<WeekendGross> weekendList = new ArrayList<>();
        WeekendGross weekend = null;
        int week = 1;
        long sum = 0;
        for (int i = 0; i < list.size(); i ++) {
            Gross gross = list.get(i);
            sum += gross.getGross();
            // 首周末开始，零点场
            if (gross.getDay() == 0) {
                weekend = new WeekendGross();
                weekendList.add(weekend);
                weekend.setDayStart(0);
                weekend.setGross(gross.getGross());
            }
            // 周末开始，星期五
            else if (gross.getDayOfWeek() == 5 && gross.getIsLeftAfterDay() == 0) {
                if (weekend == null) {
                    weekend = new WeekendGross();
                    weekend.setDayStart(gross.getDay());
                    weekendList.add(weekend);
                }
                weekend.setLeftAfterDay(0);
                weekend.setRegion(gross.getRegion());
                weekend.setSymbol(gross.getSymbol());
                weekend.setWeek(week ++);
                weekend.setGross(weekend.getGross() + gross.getGross());
            }
            else if (gross.getDayOfWeek() == 6) {
                weekend.setGross(weekend.getGross() + gross.getGross());
            }
            // 周末结束
            else if (gross.getDayOfWeek() == 7) {
                weekend.setGross(weekend.getGross() + gross.getGross());
                weekend.setDayEnd(gross.getDay());
                weekend.setTotalGross(sum);

                weekend = null;
            }
        }

        if (weekendList.size() > 0) {
            weekend = new WeekendGross();
            weekend.setTotalGross(sum);
            weekend.setRegion(weekendList.get(0).getRegion());
            weekendList.add(weekend);
        }
        return weekendList;
    }

    public Observable<List<WeekGross>> queryWeekendOversea() {
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
            List<WeekendGross> combineList = getOverseaGross(toWeekendGross(chnList), toWeekendGross(overseaList));

            List<WeekGross> list = getListFrom(combineList);
            e.onNext(list);
        });
    }

    public Observable<List<WeekGross>> queryWeeklyWorldWide() {
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
            List<WeekendGross> combineList = getWorldWideGross(toWeekendGross(naList), toWeekendGross(chnList), toWeekendGross(overseaList));

            List<WeekGross> list = getListFrom(combineList);
            e.onNext(list);
        });
    }

    private List<WeekGross> getListFrom(List<WeekendGross> weekends) {
        List<WeekGross> list = new ArrayList<>();
        for (int i = 0; i < weekends.size(); i ++) {
            WeekGross wg = new WeekGross();
            if (i == 0) {
                wg.setWeek("1");
                wg.setDrop("");
                wg.setDayRange("Opening");
            }
            else if (i == weekends.size() - 1) {
                wg.setWeek("Total");
                wg.setGross("");
                wg.setDrop("");
                wg.setDayRange("");
                if (weekends.get(i).getRegion() == Region.CHN.ordinal()) {
                    wg.setGrossSum(FormatUtil.formatChnGross(weekends.get(i).getTotalGross()));
                }
                else {
                    wg.setGrossSum(FormatUtil.formatUsGross(weekends.get(i).getTotalGross()));
                }
                list.add(wg);
                break;
            }
            else  {
                wg.setWeek(String.valueOf(i + 1));
                double drop = (double) (weekends.get(i).getGross() - weekends.get(i - 1).getGross()) / (double) weekends.get(i - 1).getGross();
                wg.setDrop(FormatUtil.formatDrop(drop));
                wg.setDayRange(weekends.get(i).getDayStart() + "-" + weekends.get(i).getDayEnd());
            }
            wg.setGross(FormatUtil.pointZZ((double) weekends.get(i).getGross() / (double) 10000));
            if (weekends.get(i).getRegion() == Region.CHN.ordinal()) {
                wg.setGrossSum(FormatUtil.formatChnGross(weekends.get(i).getTotalGross()));
            }
            else {
                wg.setGrossSum(FormatUtil.formatUsGross(weekends.get(i).getTotalGross()));
            }
            list.add(wg);
        }
        return list;
    }

    /**
     * 票房按day相加
     * @param chnList
     * @param overseaList
     * @return
     */
    private List<WeekendGross> getOverseaGross(List<WeekendGross> chnList, List<WeekendGross> overseaList) {
        List<WeekendGross> result = new ArrayList<>();
        int size = Math.max(chnList.size(), overseaList.size());
        for (int i = 0; i < size; i ++) {
            WeekendGross gross = new WeekendGross();
            gross.setRegion(Region.OVERSEA.ordinal());
            if (i < chnList.size()) {
                gross.setWeek(chnList.get(i).getWeek());
                gross.setSymbol(chnList.get(i).getSymbol());
                gross.setLeftAfterDay(chnList.get(i).getLeftAfterDay());
                gross.setDayEnd(chnList.get(i).getDayEnd());
                gross.setDayStart(chnList.get(i).getDayStart());
                gross.setTotalGross((long) (chnList.get(i).getTotalGross() / mMovie.getUsToYuan()));
                gross.setGross((long) (chnList.get(i).getGross() / mMovie.getUsToYuan()));
            }
            if (i < overseaList.size()) {
                gross.setWeek(overseaList.get(i).getWeek());
                gross.setSymbol(overseaList.get(i).getSymbol());
                gross.setLeftAfterDay(overseaList.get(i).getLeftAfterDay());
                gross.setDayEnd(overseaList.get(i).getDayEnd());
                gross.setDayStart(overseaList.get(i).getDayStart());
                gross.setTotalGross(gross.getTotalGross() + overseaList.get(i).getTotalGross());
                gross.setGross(gross.getGross() + overseaList.get(i).getGross());
            }
            result.add(gross);
        }
        return result;
    }

    /**
     * 票房按day相加
     * @param naList
     * @param chnList
     * @param overseaList
     * @return
     */
    private List<WeekendGross> getWorldWideGross(List<WeekendGross> naList, List<WeekendGross> chnList, List<WeekendGross> overseaList) {
        List<WeekendGross> result = new ArrayList<>();
        int size = Math.max(chnList.size(), overseaList.size());
        size = Math.max(naList.size(), size);
        for (int i = 0; i < size; i ++) {
            WeekendGross gross = new WeekendGross();
            gross.setRegion(Region.WORLDWIDE.ordinal());
            if (i < chnList.size()) {
                gross.setWeek(chnList.get(i).getWeek());
                gross.setSymbol(chnList.get(i).getSymbol());
                gross.setLeftAfterDay(chnList.get(i).getLeftAfterDay());
                gross.setDayEnd(chnList.get(i).getDayEnd());
                gross.setDayStart(chnList.get(i).getDayStart());
                gross.setTotalGross((long) (chnList.get(i).getTotalGross() / mMovie.getUsToYuan()));
                gross.setGross((long) (chnList.get(i).getGross() / mMovie.getUsToYuan()));
            }
            if (i < overseaList.size()) {
                gross.setWeek(overseaList.get(i).getWeek());
                gross.setSymbol(overseaList.get(i).getSymbol());
                gross.setLeftAfterDay(overseaList.get(i).getLeftAfterDay());
                gross.setDayEnd(overseaList.get(i).getDayEnd());
                gross.setDayStart(overseaList.get(i).getDayStart());
                gross.setTotalGross(gross.getTotalGross() + overseaList.get(i).getTotalGross());
                gross.setGross(gross.getGross() + overseaList.get(i).getGross());
            }
            if (i < naList.size()) {
                gross.setWeek(naList.get(i).getWeek());
                gross.setSymbol(naList.get(i).getSymbol());
                gross.setLeftAfterDay(naList.get(i).getLeftAfterDay());
                gross.setDayEnd(naList.get(i).getDayEnd());
                gross.setDayStart(naList.get(i).getDayStart());
                gross.setTotalGross(gross.getTotalGross() + naList.get(i).getTotalGross());
                gross.setGross(gross.getGross() + naList.get(i).getGross());
            }
            result.add(gross);
        }
        return result;
    }
}
