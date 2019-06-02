package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.compare.CompareChart;
import com.king.app.gross.model.compare.CompareInstance;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.gross.ChartModel;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.CompareItem;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/13 9:39
 */
public class CompareViewModel extends BaseViewModel {

    private Region mRegion;

    public ObservableField<String> regionText = new ObservableField<>();

    public MutableLiveData<List<CompareItem>> compareItemsObserver = new MutableLiveData<>();

    public MutableLiveData<CompareChart> chartObserver = new MutableLiveData<>();

    public MutableLiveData<Boolean> hideChart = new MutableLiveData<>();

    private ChartModel chartModel;

    public CompareViewModel(@NonNull Application application) {
        super(application);
        mRegion = Region.NA;
        regionText.set(getRegionText(mRegion));
        chartModel = new ChartModel();
    }

    public void loadCompareItems() {
        switch (mRegion) {
            case NA:
            case CHN:
                loadDaily();
                break;
            case MARKET:
            case MARKET_OPEN:
                loadMarket();
                break;
        }
    }

    private void loadMarket() {
        loadingObserver.setValue(true);
        queryCompares()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<CompareItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<CompareItem> list) {
                        loadingObserver.setValue(false);
                        hideChart.setValue(true);
                        compareItemsObserver.setValue(list);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void loadDaily() {
        loadingObserver.setValue(true);
        queryCompares()
                .flatMap(list -> {
                    int size = CompareInstance.getInstance().getMovieList().size();
                    compareItemsObserver.postValue(list);
                    if (SettingProperty.getCompareType() == AppConstants.COMPARE_TYPE_ACCU) {
                        return chartModel.createAccumulatedChart(list, size, mRegion);
                    }
                    else {
                        return chartModel.createDailyChart(list, size, mRegion);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<CompareChart>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(CompareChart chart) {
                        loadingObserver.setValue(false);
                        chartObserver.setValue(chart);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<List<CompareItem>> queryCompares() {
        return Observable.create(e -> {
            List<CompareItem> list = new ArrayList<>();
            list.addAll(queryBasic());
            switch (mRegion) {
                case NA:
                case CHN:
                    if (SettingProperty.getCompareType() == AppConstants.COMPARE_TYPE_ACCU) {
                        list.addAll(queryAccumulated());
                    }
                    else {
                        list.addAll(queryDayByDay());
                    }
                    break;
                case MARKET:
                case MARKET_OPEN:
                    list.addAll(queryMarket());
                    break;
            }
            e.onNext(list);
        });
    }

    private List<CompareItem> queryMarket() {
        List<CompareItem> list = new ArrayList<>();

        List<Market> markets = getDaoSession().getMarketDao().queryBuilder()
                .orderAsc(MarketDao.Properties.Continent, MarketDao.Properties.Name)
                .build().list();
        List<CompareItem> allMarkets = new ArrayList<>();
        Map<Long, CompareItem> map = new HashMap<>();
        int size = CompareInstance.getInstance().getMovieList().size();
        for (Market market:markets) {
            CompareItem item = new CompareItem();
            item.setBean(market);
            item.setKey(market.getName());
            allMarkets.add(item);
            map.put(market.getId(), item);
        }
        for (int i = 0; i < size; i ++) {
            Movie movie = CompareInstance.getInstance().getMovieList().get(i);
            List<MarketGross> marketGrosses = getDaoSession().getMarketGrossDao().queryBuilder()
                    .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                    .where(MarketGrossDao.Properties.MarketId.gt(AppConstants.MARKET_TOTAL_ID))
                    .build().list();
            for (MarketGross gross:marketGrosses) {
                CompareItem item = map.get(gross.getMarketId());
                if (item.getValues() == null) {
                    item.setValues(new ArrayList<>());
                    item.setGrossList(new ArrayList<>());
                    for (int j = 0; j < size; j ++) {
                        item.getValues().add("-");
                        item.getGrossList().add(null);
                    }
                }
                item.getValues().set(i, FormatUtil.formatUsGross(gross.getGross()));

                SimpleGross sg = new SimpleGross();
                sg.setGrossValue(gross.getGross());// 用于后面计算winIndex
                item.getGrossList().set(i, sg);
            }
        }

        String lastContinent = null;
        for (CompareItem item:allMarkets) {
            if (item.getValues() == null) {
                continue;
            }
            Market market = (Market) item.getBean();
            String continent = market.getContinent();
            if (!TextUtils.isEmpty(continent)) {
                if (!continent.equals(lastContinent)) {
                    list.add(countContinent(continent));
                    lastContinent = continent;
                }
            }
            // create win index
            item.setWinIndex(-1);
            int index = compareValues(item.getGrossList());
            item.setWinIndex(index);
            list.add(item);
        }
        return list;
    }

    private CompareItem countContinent(String continent) {
        int size = CompareInstance.getInstance().getMovieList().size();
        CompareItem item = new CompareItem();
        item.setKey(continent);
        item.setValues(new ArrayList<>());
        item.setGrossList(new ArrayList<>());
        String sql = "SELECT SUM(gross) AS total FROM market_gross mg\n" +
                " JOIN market m ON mg.market_id = m._id\n" +
                " WHERE m.continent=? AND mg.movie_id=?\n" +
                " ORDER BY total DESC";
        for (int i = 0; i < size; i ++) {
            Movie movie = CompareInstance.getInstance().getMovieList().get(i);
            Cursor cursor = MApplication.getInstance().getDatabase().rawQuery(sql,new String[]{continent, String.valueOf(movie.getId())});
            if (cursor.moveToNext()) {
                long gross = cursor.getLong(0);
                item.getValues().add(FormatUtil.formatUsGross(gross));

                SimpleGross sg = new SimpleGross();
                sg.setGrossValue(gross);
                item.getGrossList().add(sg);// 用于计算winIndex
            }
        }
        item.setWinIndex(-1);
        int index = compareValues(item.getGrossList());
        item.setWinIndex(index);
        return item;
    }

    private List<CompareItem> queryBasic() {
        List<CompareItem> list = new ArrayList<>();
        CompareItem itemDebut = new CompareItem();
        itemDebut.setKey("上映日期");
        itemDebut.setValues(new ArrayList<>());
        list.add(itemDebut);
        CompareItem itemExchange = new CompareItem();
        itemExchange.setKey("汇率");
        itemExchange.setValues(new ArrayList<>());
        list.add(itemExchange);
        CompareItem itemIsReal = new CompareItem();
        itemIsReal.setKey("Real");
        itemIsReal.setValues(new ArrayList<>());
        list.add(itemIsReal);
        CompareItem itemOpening = new CompareItem();
        itemOpening.setKey("开画");
        itemOpening.setValues(new ArrayList<>());
        list.add(itemOpening);
        CompareItem itemTotal = new CompareItem();
        itemTotal.setKey("累计");
        itemTotal.setValues(new ArrayList<>());
        list.add(itemTotal);
        CompareItem itemRate = new CompareItem();
        itemRate.setKey("后劲指数");
        itemRate.setValues(new ArrayList<>());
        list.add(itemRate);

        int size = CompareInstance.getInstance().getMovieList().size();
        for (int i = 0; i < size; i ++) {
            Movie movie = CompareInstance.getInstance().getMovieList().get(i);
            DailyModel dailyModel = new DailyModel(movie);
            itemDebut.getValues().add(movie.getDebut());
            itemExchange.getValues().add(FormatUtil.pointZZ(movie.getUsToYuan()));
            itemIsReal.getValues().add(AppConstants.MOVIE_REAL == movie.getIsReal() ? "true":"false");
            long openingNa = dailyModel.queryOpeningGross(Region.NA.ordinal());
            long openingChn = dailyModel.queryOpeningGross(Region.CHN.ordinal());
            long openingOverseaExceptChn = dailyModel.queryOpeningGross(Region.OVERSEA_NO_CHN.ordinal());
            long openingOversea = dailyModel.queryOpeningGross(Region.OVERSEA.ordinal());
            long openingWorld = dailyModel.queryOpeningGross(Region.WORLDWIDE.ordinal());
            long totalNa = dailyModel.queryTotalGross(Region.NA.ordinal());
            long totalChn = dailyModel.queryTotalGross(Region.CHN.ordinal());
            long totalOverseaExceptChn = dailyModel.queryTotalGross(Region.OVERSEA_NO_CHN.ordinal());
            long totalOversea = dailyModel.queryTotalGross(Region.OVERSEA.ordinal());
            long totalWorld = dailyModel.queryTotalGross(Region.WORLDWIDE.ordinal());
            StringBuffer opening = new StringBuffer();
            StringBuffer total = new StringBuffer();
            StringBuffer rate = new StringBuffer();
            if (openingNa > 0) {
                opening.append("\n").append("北美 ").append(FormatUtil.formatUsGross(openingNa));
            }
            if (openingChn > 0) {
                opening.append("\n").append("中国 ").append(FormatUtil.formatChnGross(openingChn));
            }
            if (openingOverseaExceptChn > 0) {
                opening.append("\n").append("海外(除中国) ").append(FormatUtil.formatUsGross(openingOverseaExceptChn));
            }
            if (openingOversea > 0) {
                opening.append("\n").append("海外 ").append(FormatUtil.formatUsGross(openingOversea));
            }
            if (openingWorld > 0) {
                opening.append("\n").append("全球 ").append(FormatUtil.formatUsGross(openingWorld));
            }
            if (opening.toString().length() > 0) {
                itemOpening.getValues().add(opening.toString().substring(1));
            }

            if (totalNa > 0) {
                total.append("\n").append("北美 ").append(FormatUtil.formatUsGross(totalNa));
            }
            if (totalChn > 0) {
                total.append("\n").append("中国 ").append(FormatUtil.formatChnGross(totalChn));
            }
            if (totalOverseaExceptChn > 0) {
                total.append("\n").append("海外(除中国) ").append(FormatUtil.formatUsGross(totalOverseaExceptChn));
            }
            if (totalOversea > 0) {
                total.append("\n").append("海外 ").append(FormatUtil.formatUsGross(totalOversea));
            }
            if (totalWorld > 0) {
                total.append("\n").append("全球 ").append(FormatUtil.formatUsGross(totalWorld));
            }
            if (total.toString().length() > 0) {
                itemTotal.getValues().add(total.toString().substring(1));
            }

            if (openingNa > 0 && totalNa > 0) {
                rate.append("\n").append("北美 ").append(FormatUtil.pointZZ((double) totalNa / (double) openingNa));
            }
            if (openingChn > 0 && totalChn > 0) {
                rate.append("\n").append("中国 ").append(FormatUtil.pointZZ((double) totalChn / (double) openingChn));
            }
            if (openingOverseaExceptChn > 0 && totalOverseaExceptChn > 0) {
                rate.append("\n").append("海外(除中国) ").append(FormatUtil.pointZZ((double) totalOverseaExceptChn / (double) openingOverseaExceptChn));
            }
            if (openingOversea > 0 && totalOversea > 0) {
                rate.append("\n").append("海外 ").append(FormatUtil.pointZZ((double) totalOversea / (double) openingOversea));
            }
            if (openingWorld > 0 && totalWorld > 0) {
                rate.append("\n").append("全球 ").append(FormatUtil.pointZZ((double) totalWorld / (double) openingWorld));
            }
            if (total.toString().length() > 0) {
                itemRate.getValues().add(rate.toString().substring(1));
            }
        }
        return list;
    }

    private List<CompareItem> queryDayByDay() {
        List<CompareItem> list = new ArrayList<>();
        int size = CompareInstance.getInstance().getMovieList().size();
        Map<String, CompareItem> map = new HashMap<>();
        for (int i = 0; i < size; i ++) {
            Movie movie = CompareInstance.getInstance().getMovieList().get(i);
            DailyModel dailyModel = new DailyModel(movie);
            List<SimpleGross> grosses;
            switch (mRegion) {
                case OVERSEA:
                    grosses = dailyModel.getOversea();
                    break;
                case WORLDWIDE:
                    grosses = dailyModel.getWorldWide();
                    break;
                default:
                    grosses = dailyModel.getGross(mRegion.ordinal());
                    break;
            }

            int week = 1;
            for (SimpleGross gross:grosses) {
                String key = getGrossKey(gross, week);
                if (key == null) {
                    continue;
                }
                if ("7".equals(gross.getDayOfWeek())) {
                    week ++;
                }
                CompareItem item = map.get(key);
                if (item == null) {
                    item = new CompareItem();
                    item.setDay(true);
                    item.setKey(key);
                    item.setValues(new ArrayList<>());
                    item.setGrossList(new ArrayList<>());
                    for (int n = 0; n < size; n ++) {
                        item.getValues().add("--");
                        item.getGrossList().add(null);
                    }
                    map.put(key, item);
                    if (key.equals(AppConstants.COMPARE_TITLE_ZERO_CHN)) {
                        list.add(0, item);
                    }
                    else {
                        list.add(item);
                    }
                }
                item.getValues().set(i, gross.getGrossDay());
                item.getGrossList().set(i, gross);
            }
            CompareItem item = map.get(AppConstants.COMPARE_TITLE_TOTAL_CHN);
            long total = dailyModel.queryTotalGross(mRegion.ordinal());
            if (item == null) {
                item = new CompareItem();
                item.setDay(true);
                item.setKey(AppConstants.COMPARE_TITLE_TOTAL_CHN);
                item.setTotal(true);
                item.setValues(new ArrayList<>());
                item.setGrossList(new ArrayList<>());
                for (int n = 0; n < size; n ++) {
                    item.getValues().add("--");
                    item.getGrossList().add(null);
                }
                map.put(AppConstants.COMPARE_TITLE_TOTAL_CHN, item);
                list.add(item);
            }
            item.getValues().set(i, mRegion == Region.CHN ? FormatUtil.formatChnGross(total):FormatUtil.formatUsGross(total));

            // 为了比较出winIndex
            SimpleGross sg = new SimpleGross();
            sg.setGrossValue(total);
            item.getGrossList().set(i, sg);
        }

        // create win index
        for (CompareItem item:list) {
            item.setWinIndex(-1);
            if (item.isDay()) {
                int index = compareValues(item.getGrossList());
                item.setWinIndex(index);
            }
        }
        return list;
    }

    private List<CompareItem> queryAccumulated() {
        List<CompareItem> list = new ArrayList<>();
        int size = CompareInstance.getInstance().getMovieList().size();
        Map<String, CompareItem> map = new HashMap<>();
        for (int i = 0; i < size; i ++) {
            Movie movie = CompareInstance.getInstance().getMovieList().get(i);
            DailyModel dailyModel = new DailyModel(movie);
            List<SimpleGross> grosses;
            switch (mRegion) {
                case OVERSEA:
                    grosses = dailyModel.getOversea();
                    break;
                case WORLDWIDE:
                    grosses = dailyModel.getWorldWide();
                    break;
                default:
                    grosses = dailyModel.getGross(mRegion.ordinal());
                    break;
            }

            for (SimpleGross gross:grosses) {
                String key = gross.getDay();
                if (key == null) {
                    continue;
                }
                else if (key.equals(AppConstants.COMPARE_TITLE_LEFT)) {
                    continue;
                }
                CompareItem item = map.get(key);
                if (item == null) {
                    item = new CompareItem();
                    item.setDay(true);
                    item.setKey(key);
                    item.setValues(new ArrayList<>());
                    item.setGrossList(new ArrayList<>());
                    for (int n = 0; n < size; n ++) {
                        item.getValues().add("--");
                        item.getGrossList().add(null);
                    }
                    map.put(key, item);
                    if (key.equals(AppConstants.COMPARE_TITLE_ZERO_CHN)) {
                        list.add(0, item);
                    }
                    else {
                        list.add(item);
                    }
                }
                item.getValues().set(i, gross.getGrossSum());
                item.getGrossList().set(i, gross);
            }
            CompareItem item = map.get(AppConstants.COMPARE_TITLE_TOTAL_CHN);
            long total = dailyModel.queryTotalGross(mRegion.ordinal());
            if (item == null) {
                item = new CompareItem();
                item.setDay(true);
                item.setKey(AppConstants.COMPARE_TITLE_TOTAL_CHN);
                item.setTotal(true);
                item.setValues(new ArrayList<>());
                item.setGrossList(new ArrayList<>());
                for (int n = 0; n < size; n ++) {
                    item.getValues().add("--");
                    item.getGrossList().add(null);
                }
                map.put(AppConstants.COMPARE_TITLE_TOTAL_CHN, item);
                list.add(item);
            }
            item.getValues().set(i, mRegion == Region.CHN ? FormatUtil.formatChnGross(total):FormatUtil.formatUsGross(total));

            // 为了比较出winIndex
            SimpleGross sg = new SimpleGross();
            sg.setGrossSumValue(total);
            item.getGrossList().set(i, sg);
        }

        // create win index
        for (CompareItem item:list) {
            item.setWinIndex(-1);
            if (item.isDay()) {
                int index = compareSumValues(item.getGrossList());
                item.setWinIndex(index);
            }
        }
        return list;
    }

    private int compareSumValues(List<SimpleGross> grossList) {
        int index = -1;
        long max = 0;
        for (int i = 0; i < grossList.size(); i ++) {
            if (grossList.get(i) != null) {
                if (grossList.get(i).getGrossSumValue() > max) {
                    max = grossList.get(i).getGrossSumValue();
                    index = i;
                }
            }
        }
        return index;
    }

    private int compareValues(List<SimpleGross> grossList) {
        int index = -1;
        long max = 0;
        for (int i = 0; i < grossList.size(); i ++) {
            if (grossList.get(i) != null) {
                if (grossList.get(i).getGrossValue() > max) {
                    max = grossList.get(i).getGrossValue();
                    index = i;
                }
            }
        }
        return index;
    }

    private String getGrossKey(SimpleGross gross, int week) {
        String key;
        // is total
        if (TextUtils.isEmpty(gross.getDay())) {
            key = null;
        }
        else if (gross.getDay().equals(AppConstants.COMPARE_TITLE_LEFT)) {
            key = AppConstants.COMPARE_TITLE_LEFT;
        }
        else {
            int dayOfWeek = Integer.parseInt(gross.getDayOfWeek());
            key = AppConstants.COMPARE_TITLE_WEEK + week + "-" + dayOfWeek;
        }
        return key;
    }

    private String getRegionText(Region region) {
        String text;
        switch (region) {
            case CHN:
                text = "中国";
                break;
            case MARKET:
                text = "海外";
                break;
            case MARKET_OPEN:
                text = "海外开画";
                break;
            default:
                text = "北美";
                break;
        }
        return text;
    }

    public Region getRegion() {
        return mRegion;
    }

    public void changeRegion(Region region) {
        mRegion = region;
        regionText.set(getRegionText(mRegion));
        loadCompareItems();
    }
}
