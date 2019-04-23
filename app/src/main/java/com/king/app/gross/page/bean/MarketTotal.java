package com.king.app.gross.page.bean;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.king.app.gross.BR;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 11:47
 */
public class MarketTotal extends BaseObservable {

    private String opening;
    private String gross;
    private String marketTitle;
    private String marketGross;
    private String undisclosedGross;

    @Bindable
    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
        notifyPropertyChanged(BR.opening);
    }

    @Bindable
    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
        notifyPropertyChanged(BR.gross);
    }

    @Bindable
    public String getMarketTitle() {
        return marketTitle;
    }

    public void setMarketTitle(String marketTitle) {
        this.marketTitle = marketTitle;
        notifyPropertyChanged(BR.marketTitle);
    }

    @Bindable
    public String getMarketGross() {
        return marketGross;
    }

    public void setMarketGross(String marketGross) {
        this.marketGross = marketGross;
        notifyPropertyChanged(BR.marketGross);
    }

    @Bindable
    public String getUndisclosedGross() {
        return undisclosedGross;
    }

    public void setUndisclosedGross(String undisclosedGross) {
        this.undisclosedGross = undisclosedGross;
        notifyPropertyChanged(BR.undisclosedGross);
    }
}
