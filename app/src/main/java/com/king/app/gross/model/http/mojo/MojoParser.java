package com.king.app.gross.model.http.mojo;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.FileUtil;
import com.king.app.gross.viewmodel.bean.MojoDefaultBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import okhttp3.ResponseBody;

public class MojoParser extends AbsParser {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DecimalFormat moneyFormat = new DecimalFormat(",###");

    public String getMojoForeignUrl(String groupId) {
        return MojoConstants.FOREIGN_URL + groupId;
    }

    public String getMojoDailyUrl(String movieId) {
        return MojoConstants.DAILY_URL + movieId;
    }

    public String getMojoDefaultUrl(String movieId) {
        return MojoConstants.DAILY_URL + movieId;
    }

    public String getMojoTitleSummaryUrl(String titleId) {
        return MojoConstants.TITLE_SUMMARY_URL + titleId;
    }

    public ObservableSource<File> saveFile(ResponseBody responseBody, String path) {
        return Observable.create(e -> {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            e.onNext(FileUtil.saveFile(responseBody.byteStream(), path));
        });
    }

    public Observable<Boolean> parseForeign(File file, long movieId, boolean clearAll) {
        return Observable.create((ObservableOnSubscribe<List<MarketGross>>) e -> {

            List<MarketGross> insertList = new ArrayList<>();

            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Elements tables = document.select("table.releases-by-region");
            for (Element table:tables) {
                Elements trs = table.select("tr");
                for (int i = 2; i < trs.size(); i ++) {
                    Element tr = trs.get(i);
                    try {
                        MarketGross gross = new MarketGross();
                        parseForeignTr(tr, gross);
                        DebugLog.e("i=" + i + ", market:" + gross.getMarketEn() + ", debut:" + gross.getDebut() + ", opening:" + gross.getOpening()
                                + ", total:" + gross.getGross() + ", endDate:" + gross.getEndDate());
                        if (!"Domestic".equals(gross.getMarketEn())) {
                            insertList.add(gross);
                        }
                    } catch (Exception exception) {
                        DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                    }
                }
            }

            e.onNext(insertList);
        }).flatMap(list -> insertAndRelateForeign(list, movieId, clearAll));
    }

    private void parseForeignTr(Element tr, MarketGross gross){
        Elements tds = tr.select("td");
        String country = tds.get(0).text();
        String date = tds.get(1).text();
        String openingText = tds.get(2).text();
        String totalText = tds.get(3).text();
        if (!"-".equals(date)) {
            String debut = parseDate(date);
            gross.setDebut(debut);
        }
        if (!"-".equals(openingText)) {
            long opening = parseMoney(openingText);
            gross.setOpening(opening);
        }
        if (!"-".equals(totalText)) {
            long total = parseMoney(totalText);
            gross.setGross(total);
        }

        gross.setMarketEn(country);
    }

    private long parseMoney(String text) {
        try {
            return moneyFormat.parse(text.substring(1)).longValue();
        } catch (Exception e) {

        }
        return 0;
    }

    private String parseDate(String text) {
        try {
            Date debut = dateFormat.parse(text);
            String date = targetDateFormat.format(debut);
            return date;
        } catch (Exception e) {

        }
        return null;
    }

    private ObservableSource<Boolean> insertAndRelateForeign(List<MarketGross> insertList, long movieId, boolean clearAll) {
        return observer -> {
            if (insertList.size() > 0) {
                MarketGrossDao dao = MApplication.getInstance().getDaoSession().getMarketGrossDao();
                if (clearAll) {
                    // delete movie related first
                    dao.queryBuilder().where(MarketGrossDao.Properties.MovieId.eq(movieId))
                            .buildDelete()
                            .executeDeleteWithoutDetachingEntities();
                    dao.detachAll();
                }

                // relate to market and movie
                for (int i = 0; i < insertList.size(); i ++) {
                    MarketGross gross = insertList.get(i);
                    gross.setMovieId(movieId);
                    try {
                        if (!"FOREIGN TOTAL".equals(gross.getMarketEn())) {
                            Market market = MApplication.getInstance().getDaoSession().getMarketDao().queryBuilder()
                                    .where(MarketDao.Properties.Name.eq(gross.getMarketEn()))
                                    .build().unique();
                            if (market == null) {
                                market = new Market();
                                market.setName(gross.getMarketEn());
                                MApplication.getInstance().getDaoSession().getMarketDao().insert(market);
                                MApplication.getInstance().getDaoSession().getMarketDao().detachAll();
                                DebugLog.e("insert market " + gross.getMarketEn());
                            }
                            gross.setMarketId(market.getId());
                        }
                    } catch (Exception exception) {
                        DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                    }
                }

                if (clearAll) {
                    // insert list
                    dao.insertInTx(insertList);
                }
                else {
                    List<MarketGross> inserts = new ArrayList<>();
                    List<MarketGross> updates = new ArrayList<>();
                    // insert or replace
                    for (MarketGross marketGross:insertList) {
                        MarketGross local = dao.queryBuilder()
                                .where(MarketGrossDao.Properties.MovieId.eq(movieId))
                                .where(MarketGrossDao.Properties.MarketId.eq(marketGross.getMarketId()))
                                .build().unique();
                        if (local == null) {
                            inserts.add(marketGross);
                        }
                        else {
                            local.setDebut(marketGross.getDebut());
                            local.setGross(marketGross.getGross());
                            local.setOpening(marketGross.getOpening());
                            local.setEndDate(marketGross.getEndDate());
                            updates.add(local);
                        }
                    }
                    if (inserts.size() > 0) {
                        DebugLog.e("insert size " + inserts.size());
                        dao.insertInTx(inserts);
                    }
                    if (updates.size() > 0) {
                        DebugLog.e("update size " + updates.size());
                        dao.updateInTx(updates);
                    }
                }
            }
            observer.onNext(true);
        };
    }

    public Observable<Boolean> parseDaily(File file, long movieId, boolean clearAll) {
        return Observable.create((ObservableOnSubscribe<List<Gross>>) e -> {

            List<Gross> insertList = new ArrayList<>();

            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Elements tableDiv = document.select("div#table");
            Element table = tableDiv.get(0).selectFirst("table");
            Elements trs = table.select("tr");
            for (int i = 1; i < trs.size(); i ++) {
                Element tr = trs.get(i);
                try {
                    Gross gross = parseDailyTr(tr, movieId);
                    DebugLog.e("i=" + i + ", dayOfWeek=:" + gross.getDayOfWeek() + ", day:" + gross.getDay() + ", gross:" + gross.getGross());
                    insertList.add(gross);
                    // 只保存前35天数据
                    if (gross.getDay() > 34) {
                        break;
                    }
                } catch (Exception exception) {
                    DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                }
            }

            e.onNext(insertList);
        }).flatMap(list -> insertAndRelateDaily(list, movieId, clearAll));
    }

    private Gross parseDailyTr(Element tr, long movieId){
//        String wholeText = td.wholeText();
//        String text = td.text();
//        String data = td.data();
//        String ownText = td.ownText();
//        String toString = td.toString();
//        String val = td.val();
        Elements tds = tr.select("td");
        String dayText = tds.get(0).text();
        String weekday = tds.get(1).text();
        String dayGross = tds.get(3).text();
        String day = tds.get(tds.size() - 2).text();
        Gross gross = new Gross();
        gross.setDay(Integer.parseInt(day));
        gross.setRegion(Region.NA.ordinal());
        gross.setSymbol(0);
        gross.setMovieId(movieId);
        gross.setDayText(dayText);
        gross.setGross(parseMoney(dayGross));
        gross.setDayOfWeek(parseWeekday(weekday));
        return gross;
    }

    private int parseWeekday(String text) {
        int day = 0;
        switch (text) {
            case "Friday":
                day = 5;
                break;
            case "Saturday":
                day = 6;
                break;
            case "Sunday":
                day = 7;
                break;
            case "Monday":
                day = 1;
                break;
            case "Tuesday":
                day = 2;
                break;
            case "Wednesday":
                day = 3;
                break;
            case "Thursday":
                day = 4;
                break;
        }
        return day;
    }

    private ObservableSource<Boolean> insertAndRelateDaily(List<Gross> insertList, long movieId, boolean clearAll) {
        return observer -> {
            if (insertList.size() > 0) {
                GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
                if (clearAll) {
                    // delete movie related first
                    dao.queryBuilder()
                            .where(GrossDao.Properties.MovieId.eq(movieId))
                            .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                            .buildDelete()
                            .executeDeleteWithoutDetachingEntities();
                    dao.detachAll();
                    dao.insertInTx(insertList);
                }
                else {
                    List<Gross> inserts = new ArrayList<>();
                    List<Gross> updates = new ArrayList<>();
                    // insert or replace
                    for (Gross gross:insertList) {
                        Gross local = dao.queryBuilder()
                                .where(GrossDao.Properties.MovieId.eq(movieId))
                                .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                                .where(GrossDao.Properties.Day.eq(gross.getDay()))
                                .build().unique();
                        if (local == null) {
                            inserts.add(gross);
                        }
                        else {
                            local.setDayOfWeek(gross.getDayOfWeek());
                            local.setGross(gross.getGross());
                            updates.add(local);
                        }
                    }
                    if (inserts.size() > 0) {
                        DebugLog.e("insert size " + inserts.size());
                        dao.insertInTx(inserts);
                    }
                    if (updates.size() > 0) {
                        DebugLog.e("update size " + updates.size());
                        dao.updateInTx(updates);
                    }
                }
                dao.detachAll();
            }
            observer.onNext(true);
        };
    }

    /**
     * domestic & international & worldwide total gross data
     * from entry page(daily)
     * @param file
     * @return
     */
    public Observable<MojoDefaultBean> parseDefault(File file) {
        return Observable.create(e -> {
            MojoDefaultBean bean = new MojoDefaultBean();
            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Element summary = document.select("div.mojo-performance-summary-table").get(0);
            Elements divs = summary.select("div");
            // 从<div>select div，第一(0)个是它自身
            Element domestic = divs.get(1).select("span.money").get(0);
            bean.setDomestic(parseMoney(domestic.text()));
            Element foreign = divs.get(2).select("span.money").get(0);
            bean.setForeign(parseMoney(foreign.text()));
            Element wolrdwide = divs.get(3).select("span.money").get(0);
            bean.setWorldwide(parseMoney(wolrdwide.text()));

            e.onNext(bean);
        });
    }

    /**
     * movie name & debut & groupId & titleId
     * from entry page(daily)
     * @param file
     * @return
     */
    public Observable<Movie> parseDefaultMovie(File file) {
        return Observable.create(e -> {
            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Movie movie = new Movie();
            Document document = Jsoup.parse(file, "UTF-8");

            // title
            // <div>的children如果也是<div>，那么select("div").get(0)或者selectFirst("div") 是他自身。可以用children来实现
            Element headDiv = document.select("div.mojo-heading-summary").get(0);
            headDiv = headDiv.children().get(0).children().get(0);
            Element titleDiv = headDiv.children().get(1);
            Element title = titleDiv.selectFirst("h1");
            String name = title.text();
            DebugLog.e("name " + name);
            if (name.contains(":")) {
                String[] arr = name.split(":");
                movie.setName(arr[0]);
                movie.setSubName(arr[1]);
            }
            else {
                movie.setName(name);
            }

            // groupId & titleId
            Element select = document.select("select#releasegroup-picker-navSelector").get(0);
            Element optionTitle = select.children().first();
            String value = optionTitle.attributes().get("value");
            String[] arr = value.split("/");
            movie.setMojoTitleId(arr[arr.length - 1]);
            Element optionGroup = select.children().last();
            value = optionGroup.attributes().get("value");
            arr = value.split("/");
            movie.setMojoGrpId(arr[arr.length - 1]);

            // release information
            Elements releaseDivs = document.select("div.mojo-summary-values").get(0).children();
            for (Element element:releaseDivs) {
                String type = element.select("span").get(0).text();
                if ("Release Date".equals(type)) {
                    String date = element.select("span").get(1).text();
                    movie.setDebut(parseDate(date));
                    DebugLog.e("debut " + movie.getDebut());
                    break;
                }
            }
            // 这个是全面的正常顺序
//            Element distributor = releaseDivs.get(0);
//            Element runtime = releaseDivs.get(1);
//            Element opening = releaseDivs.get(2);
//            Element genres = releaseDivs.get(3);
//            Element releaseDate = releaseDivs.get(4);
//            Element wildRelease = releaseDivs.get(5);
//            Element inRelease = releaseDivs.get(6);
//            Element imdbPro = releaseDivs.get(7);
            e.onNext(movie);
        });
    }

    /**
     * movie budget
     * from TitleSummary page
     * @param file
     * @return
     */
    public Observable<Movie> parseTitleSummary(File file, Movie movie) {
        return Observable.create(e -> {
            Document document = Jsoup.parse(file, "UTF-8");

            // release information
            Elements releaseDivs = document.select("div.mojo-summary-values").get(0).children();
            for (Element element:releaseDivs) {
                String type = element.select("span").get(0).text();
                if ("Budget".equals(type)) {
                    String budgetText = element.select("span").get(1).text();
                    movie.setBudget(parseBudget(budgetText));
                    DebugLog.e("budget " + movie.getDebut());
                    break;
                }
            }
            // 这个是全面的正常顺序，有的会没有budget，所以用上面的遍历方法
//            Element distributor = releaseDivs.get(0);
//            Element opening = releaseDivs.get(1);
//            Element budget = releaseDivs.get(2);
//            Element debut = releaseDivs.get(3);
//            Element mpaa = releaseDivs.get(4);
//            Element runningTime = releaseDivs.get(5);
//            Element genres = releaseDivs.get(6);
//            Element imdbPro = releaseDivs.get(7);
            e.onNext(movie);
        });
    }

    private long parseBudget(String budgetStr) {
        if ("N/A".equals(budgetStr)) {
            return 0;
        }
        return parseMoney(budgetStr);
    }

}
