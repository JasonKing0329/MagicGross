package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/21 13:50
 */
@Entity(nameInDb = "market_gross")
public class MarketGross {

    @Id(autoincrement = true)
    private Long id;

    private long marketId;

    private long movieId;

    private long gross;

    private long opening;

    private String debut;

    private String endDate;

    @ToOne(joinProperty = "marketId")
    private Market market;

    @ToOne(joinProperty = "movieId")
    private Movie movie;

    /**
     * 不存储该字段
     */
    @Transient
    private String marketEn;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 143382774)
    private transient MarketGrossDao myDao;

    @Generated(hash = 941605798)
    private transient Long market__resolvedKey;

    @Generated(hash = 708760245)
    private transient Long movie__resolvedKey;

    @Generated(hash = 142683167)
    public MarketGross(Long id, long marketId, long movieId, long gross,
            long opening, String debut, String endDate) {
        this.id = id;
        this.marketId = marketId;
        this.movieId = movieId;
        this.gross = gross;
        this.opening = opening;
        this.debut = debut;
        this.endDate = endDate;
    }

    @Generated(hash = 506735134)
    public MarketGross() {
    }

    public String getMarketEn() {
        return marketEn;
    }

    public void setMarketEn(String marketEn) {
        this.marketEn = marketEn;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMarketId() {
        return this.marketId;
    }

    public void setMarketId(long marketId) {
        this.marketId = marketId;
    }

    public long getMovieId() {
        return this.movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public long getGross() {
        return this.gross;
    }

    public void setGross(long gross) {
        this.gross = gross;
    }

    public long getOpening() {
        return this.opening;
    }

    public void setOpening(long opening) {
        this.opening = opening;
    }

    public String getDebut() {
        return this.debut;
    }

    public void setDebut(String debut) {
        this.debut = debut;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1091121916)
    public Market getMarket() {
        long __key = this.marketId;
        if (market__resolvedKey == null || !market__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MarketDao targetDao = daoSession.getMarketDao();
            Market marketNew = targetDao.load(__key);
            synchronized (this) {
                market = marketNew;
                market__resolvedKey = __key;
            }
        }
        return market;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 675228263)
    public void setMarket(@NotNull Market market) {
        if (market == null) {
            throw new DaoException(
                    "To-one property 'marketId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.market = market;
            marketId = market.getId();
            market__resolvedKey = marketId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1088418243)
    public Movie getMovie() {
        long __key = this.movieId;
        if (movie__resolvedKey == null || !movie__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MovieDao targetDao = daoSession.getMovieDao();
            Movie movieNew = targetDao.load(__key);
            synchronized (this) {
                movie = movieNew;
                movie__resolvedKey = __key;
            }
        }
        return movie;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2024058190)
    public void setMovie(@NotNull Movie movie) {
        if (movie == null) {
            throw new DaoException(
                    "To-one property 'movieId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.movie = movie;
            movieId = movie.getId();
            movie__resolvedKey = movieId;
        }
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 836586790)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMarketGrossDao() : null;
    }
}
