package com.king.app.gross.page.bean;

import com.king.app.gross.model.entity.Market;

public class MovieMarketItem {
    private Market market;
    private int rank;
    private int total;

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
