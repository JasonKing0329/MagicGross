package com.king.app.gross.model.http.mojo;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
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
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import okhttp3.ResponseBody;

public class MojoParser extends AbsParser {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private DecimalFormat moneyFormat = new DecimalFormat(",###");

    public String getMojoForeignUrl(String movieId) {
        return MojoConstants.FOREIGN_URL + movieId + MojoConstants.URL_END;
    }

    public String getMojoDailyUrl(String movieId) {
        return MojoConstants.DAILY_URL + movieId + MojoConstants.URL_END;
    }

    public String getMojoDefaultUrl(String movieId) {
        return MojoConstants.DEFAULT_URL + movieId + MojoConstants.URL_END;
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

    public Observable<Boolean> parseForeign(File file, long movieId) {
        return Observable.create((ObservableOnSubscribe<List<MarketGross>>) e -> {

            List<MarketGross> insertList = new ArrayList<>();

            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Elements tables = document.select("table");
            Element table = tables.get(tables.size() - 1);
            Elements trs = table.select("tr");
            for (int i = 0; i < trs.size(); i ++) {
                Element tr = trs.get(i);
                try {
                    String toString = tr.child(0).toString();
                    if (toString.contains("country=")) {
                        MarketGross gross = new MarketGross();
                        parseForeignTr(tr, gross);
                        DebugLog.e("i=" + i + ", market:" + gross.getMarketEn() + ", debut:" + gross.getDebut() + ", opening:" + gross.getOpening()
                                + ", total:" + gross.getGross() + ", endDate:" + gross.getEndDate());
                        insertList.add(gross);
                    }
                } catch (Exception exception) {
                    DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                }
            }

            e.onNext(insertList);
        }).flatMap(list -> insertAndRelateForeign(list, movieId));
    }

    private void parseForeignTr(Element tr, MarketGross gross){
        Elements tds = tr.select("td");
        TextNode marketNode = (TextNode) tds.get(0).child(0).child(0).child(0).childNode(0);
        TextNode dateNode = (TextNode) tds.get(2).child(0).childNode(0);
        TextNode openingNode = (TextNode) tds.get(3).child(0).childNode(0);
        TextNode rateNode = (TextNode) tds.get(4).child(0).childNode(0);
        TextNode grossNode = (TextNode) tds.get(5).child(0).child(0).childNode(0);
        TextNode endDateNode = (TextNode) tds.get(6).child(0).childNode(0);
        if (!"-".equals(dateNode.text())) {
            String debut = parseDate(dateNode.text());
            gross.setDebut(debut);
        }
        if (!"-".equals(endDateNode.text())) {
            String endDate = parseDate(endDateNode.text());
            gross.setEndDate(endDate);
        }
        if (!"-".equals(openingNode.text())) {
            long opening = parseMoney(openingNode.text());
            gross.setOpening(opening);
        }
        if (!"-".equals(grossNode.text())) {
            long total = parseMoney(grossNode.text());
            gross.setGross(total);
        }

        gross.setMarketEn(marketNode.text());
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
            String[] arrays = text.split("/");
            if (arrays[2].length() == 2) {
                text = arrays[0] + "/" + arrays[1] + "/20" + arrays[2];
            }
            Date debut = dateFormat.parse(text);
            String date = targetDateFormat.format(debut);
            return date;
        } catch (Exception e) {

        }
        return null;
    }

    private ObservableSource<Boolean> insertAndRelateForeign(List<MarketGross> insertList, long movieId) {
        return observer -> {
            if (insertList.size() > 0) {
                // delete movie related first
                MarketGrossDao dao = MApplication.getInstance().getDaoSession().getMarketGrossDao();
                dao.queryBuilder().where(MarketGrossDao.Properties.MovieId.eq(movieId))
                        .buildDelete()
                        .executeDeleteWithoutDetachingEntities();
                dao.detachAll();

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

                // insert list
                dao.insertInTx(insertList);
            }
            observer.onNext(true);
        };
    }

    public Observable<Boolean> parseDaily(File file, long movieId) {
        return Observable.create((ObservableOnSubscribe<List<Gross>>) e -> {

            List<Gross> insertList = new ArrayList<>();

            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Elements tables = document.select("table");
            Element table = tables.get(tables.size() - 1);
            Elements trs = table.select("tr");
            for (int i = 0; i < trs.size(); i ++) {
                Element tr = trs.get(i);
                try {
                    String toString = tr.toString();
                    if (toString.contains("/daily/chart/?sortdate=")) {
                        Gross gross = parseDailyTr(i, tr, movieId);
                        DebugLog.e("i=" + i + ", dayOfWeek=:" + gross.getDayOfWeek() + ", day:" + gross.getDay() + ", gross:" + gross.getGross());
                        insertList.add(gross);
                        // 只保存前35天数据
                        if (gross.getDay() > 34) {
                            break;
                        }
                    }
                } catch (Exception exception) {
                    DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                }
            }

            e.onNext(insertList);
        }).flatMap(list -> insertAndRelateDaily(list, movieId));
    }

    private Gross parseDailyTr(int index, Element tr, long movieId){
//        String wholeText = td.wholeText();
//        String text = td.text();
//        String data = td.data();
//        String ownText = td.ownText();
//        String toString = td.toString();
//        String val = td.val();
        Elements tds = tr.select("td");
        String weekday = tds.get(0).text();
        String dayGross = tds.get(3).text();
        String day = tds.get(tds.size() - 1).text();
        Gross gross = new Gross();
        gross.setDay(Integer.parseInt(day));
        gross.setRegion(Region.NA.ordinal());
        gross.setSymbol(0);
        gross.setMovieId(movieId);
        gross.setGross(parseMoney(dayGross));
        gross.setDayOfWeek(parseWeekday(weekday));
        return gross;
    }

    private int parseWeekday(String text) {
        int day = 0;
        switch (text) {
            case "Fri":
                day = 5;
                break;
            case "Sat":
                day = 6;
                break;
            case "Sun":
                day = 7;
                break;
            case "Mon":
                day = 1;
                break;
            case "Tue":
                day = 2;
                break;
            case "Wed":
                day = 3;
                break;
            case "Thu":
                day = 4;
                break;
        }
        return day;
    }

    private ObservableSource<Boolean> insertAndRelateDaily(List<Gross> insertList, long movieId) {
        return observer -> {
            if (insertList.size() > 0) {
                // delete movie related first
                GrossDao dao = MApplication.getInstance().getDaoSession().getGrossDao();
                dao.queryBuilder()
                        .where(GrossDao.Properties.MovieId.eq(movieId))
                        .where(GrossDao.Properties.Region.eq(Region.NA.ordinal()))
                        .buildDelete()
                        .executeDeleteWithoutDetachingEntities();
                dao.detachAll();

                dao.insertInTx(insertList);
            }
            observer.onNext(true);
        };
    }

    public Observable<MojoDefaultBean> parseDefault(File file) {
        return Observable.create(e -> {
            MojoDefaultBean bean = new MojoDefaultBean();
            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Document document = Jsoup.parse(file, "UTF-8");
            Elements divs = document.select("div.mp_box_content");
            Element div = divs.get(0);
            Element table = div.selectFirst("table");
            Elements trs = table.select("tr");
            Element domestic = trs.get(0);
            Element td = domestic.select("td").get(1);
            bean.setDomestic(parseMoney(td.text()));
            Element foreign = trs.get(1);
            td = foreign.select("td").get(1);
            bean.setForeign(parseMoney(td.text()));
            Element wolrdwide = trs.get(3);
            td = wolrdwide.select("td").get(1);
            bean.setWorldwide(parseMoney(td.text()));

            e.onNext(bean);
        });
    }


    public Observable<Movie> parseDefaultMovie(File file) {
        return Observable.create(e -> {
            MojoDefaultBean bean = new MojoDefaultBean();
            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parseForeign(file, "UTF-8", AtpWorldTourParams.URL_RANK);

            Movie movie = new Movie();
            Document document = Jsoup.parse(file, "UTF-8");
            Elements divs = document.select("table");
            try {
                Element table = divs.get(3);
                Element tr = table.selectFirst("tr");
                Element td = tr.select("td").get(1);
                Element br = td.selectFirst("b");
                String name = br.text();
                DebugLog.e("name " + name);
                if (name.contains(":")) {
                    String[] arr = name.split(":");
                    movie.setName(arr[0]);
                    movie.setSubName(arr[1]);
                }
                else {
                    movie.setName(name);
                }
            } catch (Exception exp) {}
            Element table = divs.get(5);
            Elements trs = table.select("tr");
            for (int i = 0; i < trs.size(); i ++) {
                Element tr = trs.get(i);
                Elements tds = tr.select("td");
                for (int j = 0; j < tds.size(); j ++) {
                    Element td = tds.get(j);
                    String text = td.text();
                    DebugLog.e("td " + text);
                    try {
                        if (text.startsWith("Domestic Total Gross")) {
                            String gross = text.substring(text.indexOf(":") + 1).trim();
                            DebugLog.e("gross " + gross);
                        }
//                        else if (text.startsWith("Distributor")) {
//                            String distributor = text.substring(text.indexOf(":") + 1).trim();
//                        }
                        else if (text.startsWith("Release Date")) {
                            String date = text.substring(text.indexOf(":") + 1).trim();
                            parseMovieDate(date, movie);
                            DebugLog.e("debut " + movie.getDebut());
                        }
//                        else if (text.startsWith("Genre")) {
//                            String genre = text.substring(text.indexOf(":") + 1).trim();
//                        }
//                        else if (text.startsWith("Runtime")) {
//                            String runtime = text.substring(text.indexOf(":") + 1).trim();
//                        }
//                        else if (text.startsWith("MPAA Rating")) {
//                            String rating = text.substring(text.indexOf(":") + 1).trim();
//                        }
                        else if (text.startsWith("Production Budget")) {
                            String budgetStr = text.substring(text.indexOf(":") + 1).trim();
                            long budget = parseBudget(budgetStr);
                            movie.setBudget(budget);
                            DebugLog.e("budget " + budget);
                        }
                    } catch (Exception exp) {
                        DebugLog.e("error: td " + text);
                    }
                }
            }
            e.onNext(movie);
        });
    }

    private long parseBudget(String budgetStr) {
        if ("N/A".equals(budgetStr)) {
            return 0;
        }
        String[] arr = budgetStr.split(" ");
        int num = Integer.parseInt(arr[0].substring(1));
        return num * 1000000;
    }

    private void parseMovieDate(String dateStr, Movie movie) {
        String[] arr = dateStr.split(",");
        int year = Integer.parseInt(arr[1].trim());
        movie.setYear(year);
        arr = arr[0].split(" ");
        int day = Integer.parseInt(arr[1]);
        String dayStr = day < 10 ? "0" + day : String.valueOf(day);
        String month;
        if (arr[0].equals("January")) {
            month = "01";
        }
        else if (arr[0].equals("February")) {
            month = "02";
        }
        else if (arr[0].equals("March")) {
            month = "03";
        }
        else if (arr[0].equals("April")) {
            month = "04";
        }
        else if (arr[0].equals("May")) {
            month = "05";
        }
        else if (arr[0].equals("June")) {
            month = "06";
        }
        else if (arr[0].equals("July")) {
            month = "07";
        }
        else if (arr[0].equals("August")) {
            month = "08";
        }
        else if (arr[0].equals("September")) {
            month = "09";
        }
        else if (arr[0].equals("October")) {
            month = "10";
        }
        else if (arr[0].equals("November")) {
            month = "11";
        }
        else {
            month = "12";
        }
        movie.setDebut(year + "-" + month + "-" + dayStr);
    }

}
