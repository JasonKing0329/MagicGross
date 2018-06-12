package com.king.app.gross.model.gross;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.SimpleGross;
import com.king.app.gross.viewmodel.bean.WeekGross;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/9 0009 12:40
 */

public class WeeklyModel {
    private Movie mMovie;

    public WeeklyModel(Movie mMovie) {
        this.mMovie = mMovie;
    }

    private List<WeekGross> getIsTotalList(int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> grossList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.notEq(0))
                .build().list();
        // 加入过isTotal的数据，只取这一条，不进行day by day的计算
        if (grossList.size() > 0) {
            List<WeekGross> list = new ArrayList<>();
            for (Gross item:grossList) {
                WeekGross gross = new WeekGross();
                if (item.getIsTotal() == AppConstants.GROSS_IS_TOTAL) {
                    gross.setDayRange("Total");
                }
                else if (item.getIsTotal() == AppConstants.GROSS_IS_OPENING) {
                    gross.setDayRange("Opening");
                }
                gross.setGrossSum(FormatUtil.formatUsGross(item.getGross()));
                gross.setWeek("");
                gross.setDrop("");
                gross.setGross("");
                list.add(gross);
            }
            return list;
        }
        return null;
    }

    public Observable<List<WeekGross>> queryWeeklyGross(int region) {
        return Observable.create(e -> {
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> grossList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();

            // 优先加载day by day，没有再检查是否有total
            if (grossList.size() == 0) {
                List<WeekGross> totals = getIsTotalList(region);
                if (totals != null) {
                    e.onNext(totals);
                    return;
                }
            }

            List<WeekGross> list = getListFrom(grossList);

            e.onNext(list);
        });
    }

    public Observable<List<WeekGross>> queryWeeklyOversea() {
        return Observable.create(e -> {
            // 优先检查是否有is total
            List<WeekGross> totals = getIsTotalList(Region.OVERSEA.ordinal());
            if (totals != null) {
                e.onNext(totals);
                return;
            }
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> chnList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> overseaList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> combineList = getOverseaGross(chnList, overseaList);

            List<WeekGross> list = getListFrom(combineList);
            e.onNext(list);
        });
    }

    public Observable<List<WeekGross>> queryWeeklyWorldWide() {
        return Observable.create(e -> {
            // 优先检查是否有is total
            List<WeekGross> totals = getIsTotalList(Region.WORLDWIDE.ordinal());
            if (totals != null) {
                e.onNext(totals);
                return;
            }
            GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
            List<Gross> naList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> chnList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> overseaList = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            List<Gross> combineList = getWorldWideGross(naList, chnList, overseaList);

            List<WeekGross> list = getListFrom(combineList);
            e.onNext(list);
        });
    }

    private List<WeekGross> getListFrom(List<Gross> combineList) {
        List<WeekGross> list = new ArrayList<>();
        WeekGross wg = new WeekGross();
        long sum = 0;
        int dayStart = 0;
        long lastSum = 0;
        long totalSum = 0;
        for (int i = 0; i < combineList.size(); i ++) {
            Gross gross = combineList.get(i);
            sum += gross.getGross();
            totalSum += gross.getGross();
            if (gross.getIsLeftAfterDay() > 0) {
                wg.setWeek("Left");
                wg.setGross(FormatUtil.pointZZ((double) gross.getGross() / (double) 10000));
                if (gross.getRegion() == Region.CHN.ordinal()) {
                    wg.setGrossSum(FormatUtil.formatChnGross(totalSum));
                }
                else {
                    wg.setGrossSum(FormatUtil.formatUsGross(totalSum));
                }
                wg.setDrop("");
                wg.setDayRange("After " + gross.getIsLeftAfterDay());
                list.add(wg);
            }
            else {
                if (gross.getDay() % 7 == 0 && gross.getDay() > 0) {
                    wg.setWeek(String.valueOf(gross.getDay() / 7));
                    wg.setDayRange(dayStart + "-" + gross.getDay());
                    wg.setGross(FormatUtil.pointZZ((double) sum / (double) 10000));
                    if (gross.getRegion() == Region.CHN.ordinal()) {
                        wg.setGrossSum(FormatUtil.formatChnGross(totalSum));
                    }
                    else {
                        wg.setGrossSum(FormatUtil.formatUsGross(totalSum));
                    }
                    if (!wg.getWeek().equals("1")) {
                        double drop = (double) (sum - lastSum) / (double) lastSum;
                        wg.setDrop(FormatUtil.formatDrop(drop));
                    }
                    list.add(wg);

                    lastSum = sum;
                    sum = 0;
                    dayStart = gross.getDay() + 1;
                    wg = new WeekGross();
                }
            }
        }
        return list;
    }

    /**
     * 票房按day相加
     * @param chnList
     * @param overseaList
     * @return
     */
    private List<Gross> getOverseaGross(List<Gross> chnList, List<Gross> overseaList) {
        List<Gross> result = new ArrayList<>();
        int size = Math.max(chnList.size(), overseaList.size());
        for (int i = 0; i < size; i ++) {
            Gross gross = new Gross();
            gross.setRegion(Region.OVERSEA.ordinal());
            if (i < chnList.size()) {
                gross.setDay(chnList.get(i).getDay());
                gross.setDayOfWeek(chnList.get(i).getDayOfWeek());
                gross.setIsLeftAfterDay(chnList.get(i).getIsLeftAfterDay());
                gross.setGross((long) (chnList.get(i).getGross() / mMovie.getUsToYuan()));
            }
            if (i < overseaList.size()) {
                gross.setDay(overseaList.get(i).getDay());
                gross.setDayOfWeek(overseaList.get(i).getDayOfWeek());
                gross.setIsLeftAfterDay(overseaList.get(i).getIsLeftAfterDay());
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
    private List<Gross> getWorldWideGross(List<Gross> naList, List<Gross> chnList, List<Gross> overseaList) {
        List<Gross> result = new ArrayList<>();
        int size = Math.max(chnList.size(), overseaList.size());
        size = Math.max(naList.size(), size);
        for (int i = 0; i < size; i ++) {
            Gross gross = new Gross();
            gross.setRegion(Region.WORLDWIDE.ordinal());
            if (i < chnList.size()) {
                gross.setDay(chnList.get(i).getDay());
                gross.setDayOfWeek(chnList.get(i).getDayOfWeek());
                gross.setIsLeftAfterDay(chnList.get(i).getIsLeftAfterDay());
                gross.setGross((long) (chnList.get(i).getGross() / mMovie.getUsToYuan()));
            }
            if (i < overseaList.size()) {
                gross.setDay(overseaList.get(i).getDay());
                gross.setDayOfWeek(overseaList.get(i).getDayOfWeek());
                gross.setIsLeftAfterDay(overseaList.get(i).getIsLeftAfterDay());
                gross.setGross(gross.getGross() + overseaList.get(i).getGross());
            }
            if (i < naList.size()) {
                gross.setDay(naList.get(i).getDay());
                gross.setDayOfWeek(naList.get(i).getDayOfWeek());
                gross.setIsLeftAfterDay(naList.get(i).getIsLeftAfterDay());
                gross.setGross(gross.getGross() + naList.get(i).getGross());
            }
            result.add(gross);
        }
        return result;
    }
}
