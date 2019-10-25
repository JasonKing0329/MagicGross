package com.king.app.gross.page.bean;

public class MovieBasicData {
    private String grossNa;

    private String grossChn;

    private String grossMarket;

    private String grossOversea;

    private String grossWorldWide;

    private String budget;

    private String exchangeRate;

    private String mojoTitle;

    private String mojoId;

    private int mojoGrpVisibility;

    private String mojoGrpId;

    private int mojoTitleVisibility;

    private String mojoTitleId;

    private RatingData imdb;

    private RatingData rottenPro;

    private RatingData rottenAud;

    private RatingData metaScore;

    private RatingData douBan;

    private RatingData maoYan;

    private RatingData taoPiaoPiao;

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getMojoId() {
        return mojoId;
    }

    public void setMojoId(String mojoId) {
        this.mojoId = mojoId;
    }

    public String getGrossNa() {
        return grossNa;
    }

    public void setGrossNa(String grossNa) {
        this.grossNa = grossNa;
    }

    public String getGrossChn() {
        return grossChn;
    }

    public void setGrossChn(String grossChn) {
        this.grossChn = grossChn;
    }

    public String getGrossMarket() {
        return grossMarket;
    }

    public void setGrossMarket(String grossMarket) {
        this.grossMarket = grossMarket;
    }

    public String getGrossOversea() {
        return grossOversea;
    }

    public void setGrossOversea(String grossOversea) {
        this.grossOversea = grossOversea;
    }

    public String getGrossWorldWide() {
        return grossWorldWide;
    }

    public void setGrossWorldWide(String grossWorldWide) {
        this.grossWorldWide = grossWorldWide;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getMojoTitle() {
        return mojoTitle;
    }

    public void setMojoTitle(String mojoTitle) {
        this.mojoTitle = mojoTitle;
    }

    public String getMojoGrpId() {
        return mojoGrpId;
    }

    public void setMojoGrpId(String mojoGrpId) {
        this.mojoGrpId = mojoGrpId;
    }

    public RatingData getImdb() {
        return imdb;
    }

    public void setImdb(RatingData imdb) {
        this.imdb = imdb;
    }

    public RatingData getRottenPro() {
        return rottenPro;
    }

    public void setRottenPro(RatingData rottenPro) {
        this.rottenPro = rottenPro;
    }

    public RatingData getRottenAud() {
        return rottenAud;
    }

    public void setRottenAud(RatingData rottenAud) {
        this.rottenAud = rottenAud;
    }

    public RatingData getMetaScore() {
        return metaScore;
    }

    public void setMetaScore(RatingData metaScore) {
        this.metaScore = metaScore;
    }

    public RatingData getDouBan() {
        return douBan;
    }

    public void setDouBan(RatingData douBan) {
        this.douBan = douBan;
    }

    public RatingData getMaoYan() {
        return maoYan;
    }

    public void setMaoYan(RatingData maoYan) {
        this.maoYan = maoYan;
    }

    public RatingData getTaoPiaoPiao() {
        return taoPiaoPiao;
    }

    public void setTaoPiaoPiao(RatingData taoPiaoPiao) {
        this.taoPiaoPiao = taoPiaoPiao;
    }

    public boolean isRottenEmpty() {
        return rottenAud == null && rottenPro == null;
    }

    public int getMojoGrpVisibility() {
        return mojoGrpVisibility;
    }

    public void setMojoGrpVisibility(int mojoGrpVisibility) {
        this.mojoGrpVisibility = mojoGrpVisibility;
    }

    public int getMojoTitleVisibility() {
        return mojoTitleVisibility;
    }

    public void setMojoTitleVisibility(int mojoTitleVisibility) {
        this.mojoTitleVisibility = mojoTitleVisibility;
    }

    public String getMojoTitleId() {
        return mojoTitleId;
    }

    public void setMojoTitleId(String mojoTitleId) {
        this.mojoTitleId = mojoTitleId;
    }
}
