package com.king.app.gross.model.http.mojo;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.utils.DebugLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;

public class MojoParser extends AbsParser {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private DecimalFormat moneyFormat = new DecimalFormat(",###");

    public Observable<Boolean> parse(File file, long movieId) {
        return Observable.create((ObservableOnSubscribe<List<MarketGross>>) e -> {

            List<MarketGross> insertList = new ArrayList<>();

            // 文件不存在则从网络里重新，虽然这个可以满足文件不存在是从网络里重新下载并且还存到file里，
            // 但是这种方式不能自定义user agent
//                Document document = Jsoup.parse(file, "UTF-8", AtpWorldTourParams.URL_RANK);

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
                        parseTr(tr, gross);
                        DebugLog.e("i=" + i + ", market:" + gross.getMarketEn() + ", debut:" + gross.getDebut() + ", opening:" + gross.getOpening()
                                + ", total:" + gross.getGross() + ", endDate:" + gross.getEndDate());
                        insertList.add(gross);
                    }
                } catch (Exception exception) {
                    DebugLog.e("[error] i=" + i + ", " + exception.getMessage());
                }
            }

            e.onNext(insertList);
        }).flatMap(list -> insertAndRelate(list, movieId));
    }

    private void parseTr(Element tr, MarketGross gross){
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

    private ObservableSource<Boolean> insertAndRelate(List<MarketGross> insertList, long movieId) {
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
}
