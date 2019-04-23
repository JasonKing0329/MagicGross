package com.king.app.gross.page.bean;

import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketGross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 17:12
 */
public class EditMarketGrossBean {

    private String index;

    private String market;

    private String ref1;

    private String ref2;

    private String grossText;

    private long gross;

    private Market bean;

    private MarketGross marketGross;

    private boolean isEdited;

    private int markColor;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getRef1() {
        return ref1;
    }

    public void setRef1(String ref1) {
        this.ref1 = ref1;
    }

    public String getRef2() {
        return ref2;
    }

    public void setRef2(String ref2) {
        this.ref2 = ref2;
    }

    public String getGrossText() {
        return grossText;
    }

    public void setGrossText(String gross) {
        this.grossText = gross;
    }

    public Market getBean() {
        return bean;
    }

    public void setBean(Market bean) {
        this.bean = bean;
    }

    public long getGross() {
        return gross;
    }

    public void setGross(long gross) {
        this.gross = gross;
    }

    public MarketGross getMarketGross() {
        return marketGross;
    }

    public void setMarketGross(MarketGross marketGross) {
        this.marketGross = marketGross;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public int getMarkColor() {
        return markColor;
    }

    public void setMarkColor(int markColor) {
        this.markColor = markColor;
    }
}
