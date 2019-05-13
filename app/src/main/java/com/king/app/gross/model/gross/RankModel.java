package com.king.app.gross.model.gross;

import android.text.TextUtils;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.RankType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.RankItem;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 21:49
 */

public class RankModel {

    public RankItem convertMovie(Movie movie, Region region, RankType mRankType) {
        RankItem item = new RankItem();
        item.setMovie(movie);
        item.setImageUrl(ImageUrlProvider.getMovieImageRandom(movie));
        if (region == Region.CHN) {
            item.setName(movie.getNameChn());
            if (!TextUtils.isEmpty(movie.getSubChnName())) {
                item.setName(movie.getNameChn() + "：" + movie.getSubChnName());
            }
        }
        else {
            item.setName(movie.getName());
            if (!TextUtils.isEmpty(movie.getSubName())) {
                item.setName(movie.getName() + ": " + movie.getSubName());
            }
        }
        try {
            item.setYear(movie.getDebut().substring(0, 4));
        } catch (Exception e) {
            item.setYear("--");
        }
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
                // 首周即开画
            case FIRST_WEEK:
                loadOpeningValue(movie, region, item);
                break;
            case SECOND_WEEK:
                loadSecondWeekValue(movie, region, item);
                break;
            case THIRD_WEEK:
                loadThirdWeekValue(movie, region, item);
                break;
            case FOURTH_WEEK:
                loadFourthWeekValue(movie, region, item);
                break;
            case FIFTH_WEEK:
                loadFifthWeekValue(movie, region, item);
                break;
            case LEFT_AFTER_35:
                loadLeftValue(movie, region, item);
                break;
            case DAYS_10:
                load10DaysValue(movie, region, item);
                break;
            case DAYS_20:
                load20DaysValue(movie, region, item);
                break;
            case DAYS_30:
                load30DaysValue(movie, region, item);
                break;
            case BUDGET:
                loadBudgetValue(movie, item);
                break;
            case TOP_DAY:
                loadTopDay(movie, region, item);
                break;
        }
        return item;
    }

    private void formatGross(Region region, RankItem item, long gross) {
        if (region == Region.CHN) {
            item.setValue(FormatUtil.formatChnGross(gross));
        }
        else {
            item.setValue(FormatUtil.formatUsGross(gross));
        }
    }

    private long getWorldWideGross(Movie movie) {
        // 优先加载is total的数据，没有才按day by day加载
        long gross = queryGrossByIsTotal(movie, Region.WORLDWIDE.ordinal());
        if (gross == 0) {
            gross = queryGrossByDay(movie, Region.WORLDWIDE.ordinal(), null);
        }
        return gross;
    }

    private long getWorldWideOpening(Movie movie) {
        // 优先加载is total的数据，没有才按day by day加载
        long gross = queryOpeningByIsTotal(movie, Region.WORLDWIDE.ordinal());
        if (gross == 0) {
            gross = queryGrossByDay(movie, Region.WORLDWIDE.ordinal(), true, GrossDao.Properties.Day.le(7));
        }
        return gross;
    }

    private void loadTotalValue(Movie movie, Region region, RankItem item) {
        // 优先加载is total的数据，没有才按day by day加载
        long gross = queryGrossByIsTotal(movie, region.ordinal());
        if (gross == 0) {
            gross = queryGrossByDay(movie, region.ordinal(), null);
        }
        item.setSortValue(gross);
        formatGross(region, item, gross);

        if (region != Region.WORLDWIDE) {
            // 计算相对全球占比
            long total = getWorldWideGross(movie);
            if (region == Region.CHN) {
                item.setRate(FormatUtil.pointZ((double) gross / (double) total / movie.getUsToYuan() * 100d) + "%");
            }
            else {
                item.setRate(FormatUtil.pointZ((double) gross / (double) total * 100d) + "%");
            }
        }
    }

    private void loadOpeningValue(Movie movie, Region region, RankItem item) {
        // 优先加载is total的数据，没有才按day by day加载
        long gross = queryOpeningByIsTotal(movie, region.ordinal());
        if (gross == 0) {
            gross = queryGrossByDay(movie, region.ordinal(), true, GrossDao.Properties.Day.le(7));
        }
        item.setSortValue(gross);
        formatGross(region, item, gross);

        if (region != Region.WORLDWIDE) {
            // 计算相对全球占比
            long total = getWorldWideOpening(movie);
            if (total > 0 && gross > 0) {
                if (region == Region.CHN) {
                    item.setRate(FormatUtil.pointZ((double) gross / (double) total / movie.getUsToYuan() * 100d) + "%");
                }
                else {
                    item.setRate(FormatUtil.pointZ((double) gross / (double) total * 100d) + "%");
                }
            }
        }
    }

    private void loadRateValue(Movie movie, Region region, RankItem item) {
        // 优先加载is total的数据，没有才按day by day加载
        long total = queryGrossByIsTotal(movie, region.ordinal());
        if (total == 0) {
            total = queryGrossByDay(movie, region.ordinal(), null);
        }
        long opening = queryOpeningByIsTotal(movie, region.ordinal());
        if (opening == 0) {
            opening = queryGrossByDay(movie, region.ordinal(), true, GrossDao.Properties.Day.le(7));
        }
        if (opening > 0) {
            item.setSortValue((double) total / (double) opening);
        }
        item.setValue(FormatUtil.pointZ(item.getSortValue()));
    }

    private int getFirstWeekEndDay(Movie movie, Region region) {
        // first day
        Gross gb = MApplication.getInstance().getDaoSession().getGrossDao().queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(region.ordinal()))
                .where(GrossDao.Properties.Day.eq(1))
                .build().unique();
        if (gb == null) {
            return 3;
        }
        int firstWeekEndDay = (7 - gb.getDayOfWeek()) + 1;
        return firstWeekEndDay;
    }

    private void loadSecondWeekValue(Movie movie, Region region, RankItem item) {
        int firstWeekEndDay = getFirstWeekEndDay(movie, region);
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.gt(firstWeekEndDay), GrossDao.Properties.Day.le(firstWeekEndDay + 7)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void loadThirdWeekValue(Movie movie, Region region, RankItem item) {
        int firstWeekEndDay = getFirstWeekEndDay(movie, region);
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.gt(firstWeekEndDay + 7), GrossDao.Properties.Day.le(firstWeekEndDay + 14)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void loadFourthWeekValue(Movie movie, Region region, RankItem item) {
        int firstWeekEndDay = getFirstWeekEndDay(movie, region);
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.gt(firstWeekEndDay + 14), GrossDao.Properties.Day.le(firstWeekEndDay + 21)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void loadFifthWeekValue(Movie movie, Region region, RankItem item) {
        int firstWeekEndDay = getFirstWeekEndDay(movie, region);
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.gt(firstWeekEndDay + 21), GrossDao.Properties.Day.le(firstWeekEndDay + 28)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void load10DaysValue(Movie movie, Region region, RankItem item) {
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.le(10)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void load20DaysValue(Movie movie, Region region, RankItem item) {
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.le(20)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void load30DaysValue(Movie movie, Region region, RankItem item) {
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.Day.le(30)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private void loadBudgetValue(Movie movie, RankItem item) {
        item.setSortValue(movie.getBudget());
        item.setValue(FormatUtil.formatUsGross(movie.getBudget()));
    }

    private void loadTopDay(Movie movie, Region region, RankItem item) {
        Gross gross = queryTopGross(movie, region.ordinal());
        if (gross != null) {
            item.setSortValue(gross.getGross());
            formatGross(region, item, gross.getGross());
            item.setRate(gross.getDay() + "-" + gross.getDayOfWeek());
        }
    }

    private void loadLeftValue(Movie movie, Region region, RankItem item) {
        long gross = queryGrossByDay(movie, region.ordinal(), new WhereCondition[]{GrossDao.Properties.IsLeftAfterDay.gt(0)});
        item.setSortValue(gross);
        formatGross(region, item, gross);
    }

    private long queryGrossByDay(Movie mMovie, int region, WhereCondition[] dayConditions) {
        return queryGrossByDay(mMovie, region, false, dayConditions);
    }

    private Gross queryTopGross(Movie mMovie, int region) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" WHERE ").append(GrossDao.Properties.Gross.columnName).append("=(SELECT MAX(")
                .append(GrossDao.Properties.Gross.columnName).append(") FROM ")
                .append(GrossDao.TABLENAME).append(" WHERE ")
                .append(GrossDao.Properties.MovieId.columnName).append("=? AND ")
                .append(GrossDao.Properties.Region.columnName).append("=? AND ")
                .append(GrossDao.Properties.IsTotal.columnName).append("=0 AND ")
                .append(GrossDao.Properties.IsLeftAfterDay.columnName).append("=0)");
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        String sql = buffer.toString();
        List<Gross> list = dao.queryRaw(sql, new String[]{String.valueOf(mMovie.getId()), String.valueOf(region)});
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private long queryGrossByDay(Movie mMovie, int region, boolean isOpening, WhereCondition... dayConditions) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        long sum = 0;
        if (region < Region.OVERSEA.ordinal()) {
            QueryBuilder<Gross> builder = dao.queryBuilder();
            builder.where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(region))
                    .where(GrossDao.Properties.IsTotal.eq(0));
            if (dayConditions != null) {
                for (WhereCondition condition:dayConditions) {
                    builder.where(condition);
                }
            }
            builder.orderAsc(GrossDao.Properties.Day);
            List<Gross> list = builder.build().list();
            for (Gross gross:list) {
                sum += gross.getGross();
                if (gross.getDayOfWeek() == 7 && isOpening) {
                    break;
                }
            }
        }
        else {
            QueryBuilder<Gross> builder = dao.queryBuilder();
            builder.where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0));
            if (dayConditions != null) {
                for (WhereCondition condition:dayConditions) {
                    builder.where(condition);
                }
            }
            builder.orderAsc(GrossDao.Properties.Day);
            List<Gross> list = builder.build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += (gross.getGross() / mMovie.getUsToYuan());
                if (gross.getDayOfWeek() == 7 && isOpening) {
                    break;
                }
            }

            builder = dao.queryBuilder();
            builder.where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                    .where(GrossDao.Properties.Region.eq(Region.OVERSEA_NO_CHN.ordinal()))
                    .where(GrossDao.Properties.IsTotal.eq(0));
            if (dayConditions != null) {
                for (WhereCondition condition:dayConditions) {
                    builder.where(condition);
                }
            }
            builder.orderAsc(GrossDao.Properties.Day);
            list = builder.build().list();
            if (list.size() == 0) {
                return 0;
            }
            for (Gross gross:list) {
                sum += gross.getGross();
                if (gross.getDayOfWeek() == 7 && isOpening) {
                    break;
                }
            }

            if (region == Region.WORLDWIDE.ordinal()) {
                builder = dao.queryBuilder();
                builder.where(GrossDao.Properties.MovieId.eq(mMovie.getId()))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .where(GrossDao.Properties.IsTotal.eq(0));
                if (dayConditions != null) {
                    for (WhereCondition condition:dayConditions) {
                        builder.where(condition);
                    }
                }
                builder.orderAsc(GrossDao.Properties.Day);
                list = builder.build().list();
                if (list.size() == 0) {
                    return 0;
                }
                for (Gross gross:list) {
                    sum += gross.getGross();
                    if (gross.getDayOfWeek() == 7 && isOpening) {
                        break;
                    }
                }
            }
        }
        return sum;
    }

    private long queryGrossByIsTotal(Movie movie, int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> list = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().list();
        // 加入过isTotal的数据，只取这一条，不进行day by day的计算
        if (list.size() > 0) {
            return list.get(0).getGross();
        }
        return 0;
    }

    private long queryOpeningByIsTotal(Movie movie, int region) {
        GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
        List<Gross> list = dao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(region))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().list();
        // 加入过isTotal的数据，只取这一条，不进行day by day的计算
        if (list.size() > 0) {
            return list.get(0).getGross();
        }
        return 0;
    }

}
