package com.king.app.gross.model.gross;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
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

    private List<SimpleGross> getIsTotalList(int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> grossList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.notEq(0))
                .build().list();
        // 加入过isTotal的数据，只取total数据，不进行day by day的计算
        if (grossList.size() > 0) {
            List<SimpleGross> list = new ArrayList<>();
            for (Gross item:grossList) {
                SimpleGross gross = new SimpleGross();
                gross.setBean(item);
                if (item.getIsTotal() == AppConstants.GROSS_IS_TOTAL) {
                    gross.setGrossDay("Total");
                }
                else if (item.getIsTotal() == AppConstants.GROSS_IS_OPENING) {
                    gross.setGrossDay("Opening");
                }
                gross.setGrossValue(item.getGross());
                gross.setGrossSum(FormatUtil.formatUsGross(item.getGross()));
                gross.setDay("");
                gross.setDropWeek("");
                gross.setDropDay("");
                gross.setDayOfWeek("");
                list.add(gross);
            }
            return list;
        }
        return null;
    }

    public Observable<List<SimpleGross>> queryGross(int region) {
        return Observable.create(e -> e.onNext(getGross(region)));
    }

    public List<SimpleGross> getGross(int region) {
        // 优先加载total，没有再检查是否有day by day
        List<SimpleGross> totals = getIsTotalList(region);
        if (totals != null) {
            return totals;
        }
        List<SimpleGross> list = new ArrayList<>();
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> grosses = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        long sum = 0;
        for (int i = 0; i < grosses.size(); i ++) {
            Gross gross = grosses.get(i);
            sum += gross.getGross();

            SimpleGross sg = new SimpleGross();
            sg.setBean(gross);
            if (gross.getIsLeftAfterDay() > 0) {
                sg.setDay("Left");
                sg.setDayOfWeek("");
            }
            else {
                sg.setDay(String.valueOf(gross.getDay()));
                sg.setDayOfWeek(String.valueOf(gross.getDayOfWeek()));
            }
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
            sg.setGrossValue(gross.getGross());
            // 累计以单位为个位显示
            if (gross.getRegion() == Region.CHN.ordinal()) {
                sg.setGrossSum(FormatUtil.formatChnGross(sum));
            }
            else {
                sg.setGrossSum(FormatUtil.formatUsGross(sum));
            }
            list.add(sg);
        }
        return list;
    }

    public Observable<List<SimpleGross>> queryOversea() {
        return Observable.create(e -> e.onNext(getOversea()));
    }

    public List<SimpleGross> getOversea() {
        // 优先检查是否有is total
        List<SimpleGross> totals = getIsTotalList(Region.OVERSEA.ordinal());
        if (totals != null) {
            return totals;
        }
        List<SimpleGross> list = new ArrayList<>();

        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> chnList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        if (chnList.size() == 0) {
            return list;
        }
        List<Gross> overseaList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        if (overseaList.size() == 0) {
            return list;
        }

        int size = Math.max(overseaList.size(), chnList.size());

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
            if (gross.getIsLeftAfterDay() > 0) {
                sg.setDay("Left");
                sg.setDayOfWeek("");
            }
            else {
                sg.setDay(String.valueOf(gross.getDay()));
                sg.setDayOfWeek(String.valueOf(gross.getDayOfWeek()));
            }
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
            sg.setGrossValue(gross.getGross());
            list.add(sg);
        }
        return list;
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
        return Observable.create(e -> e.onNext(getWorldWide()));
    }

    public List<SimpleGross> getWorldWide() {

        // 优先检查是否有is total
        List<SimpleGross> totals = getIsTotalList(Region.WORLDWIDE.ordinal());
        if (totals != null) {
            return totals;
        }
        List<SimpleGross> list = new ArrayList<>();
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> naList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        if (naList.size() == 0) {
            return list;
        }
        List<Gross> chnList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        if (chnList.size() == 0) {
            return list;
        }
        List<Gross> overseaList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(0))
                .orderAsc(GrossDao.Properties.Day)
                .build().list();
        if (overseaList.size() == 0) {
            return list;
        }
        int size = Math.max(naList.size(), chnList.size());
        size = Math.max(size, overseaList.size());

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
            if (gross.getIsLeftAfterDay() > 0) {
                sg.setDay("Left");
                sg.setDayOfWeek("");
            }
            else {
                sg.setDay(String.valueOf(gross.getDay()));
                sg.setDayOfWeek(String.valueOf(gross.getDayOfWeek()));
            }
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
            sg.setGrossValue(gross.getGross());
            list.add(sg);
        }
        return list;
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

    public long queryOpeningGross(int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        // 先检查total
        List<Gross> openingList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().list();
        if (openingList.size() > 0) {
            return openingList.get(0).getGross();
        }
        // oversea与worldwide一般情况下是根据na, chn, oversea_no_chn之间进行运算的，任何一方没有数据则不做运算
        // 以编辑的total数据为准
        // 并非所有movie都是在周五上映，所以统计开画要以第一个星期天为准（查询头7天的数据，累计到第一个星期天出现为准）
        long sum = 0;
        if (region < Region.OVERSEA.ordinal()) {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .where(GrossDao.Properties.Day.le(7))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
                if (gross.getDayOfWeek() == 7) {
                    break;
                }
            }
        }
        else {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .where(GrossDao.Properties.Day.le(7))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += (gross.getGross() / mMovie.getUsToYuan());
                if (gross.getDayOfWeek() == 7) {
                    break;
                }
            }
            list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .where(GrossDao.Properties.Day.le(7))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += gross.getGross();
                if (gross.getDayOfWeek() == 7) {
                    break;
                }
            }

            if (region == Region.WORLDWIDE.ordinal()) {
                list = dao.queryBuilder()
                        .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .where(GrossDao.Properties.IsTotal.eq(0))
                        .where(GrossDao.Properties.Day.le(7))
                        .orderAsc(GrossDao.Properties.Day)
                        .build().list();
                if (list.size() == 0) {
                    return 0;
                }
                for (Gross gross:list) {
                    sum += gross.getGross();
                    if (gross.getDayOfWeek() == 7) {
                        break;
                    }
                }
            }
        }
        return sum;
    }

    public long queryTotalGross(int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> totalList = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().list();
        if (totalList.size() > 0) {
            return totalList.get(0).getGross();
        }
        long sum = 0;
        if (region < Region.OVERSEA.ordinal()) {
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
            }
        }
        else {
            // oversea与worldwide一般情况下是根据na, chn, oversea_no_chn之间进行运算的，任何一方没有数据则不做运算
            // 以编辑的total数据为准
            List<Gross> list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += (gross.getGross() / mMovie.getUsToYuan());
            }
            list = dao.queryBuilder()
                    .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0))
                    .orderAsc(GrossDao.Properties.Day)
                    .build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += gross.getGross();
            }

            if (region == Region.WORLDWIDE.ordinal()) {
                list = dao.queryBuilder()
                        .where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .where(GrossDao.Properties.IsTotal.eq(0))
                        .orderAsc(GrossDao.Properties.Day)
                        .build().list();
                if (list.size() == 0) {
                    return 0;
                }
                for (Gross gross:list) {
                    sum += gross.getGross();
                }
            }
        }
        return sum;
    }
}
