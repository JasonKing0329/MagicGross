package com.king.app.gross.page.bean;

import com.king.app.gross.model.entity.MarketGross;

import java.util.List;

public class ContinentGross {

    private String continent;

    private long gross;

    private List<MarketGross> marketList;

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public long getGross() {
        return gross;
    }

    public void setGross(long gross) {
        this.gross = gross;
    }

    public List<MarketGross> getMarketList() {
        return marketList;
    }

    public void setMarketList(List<MarketGross> marketList) {
        this.marketList = marketList;
    }
}
