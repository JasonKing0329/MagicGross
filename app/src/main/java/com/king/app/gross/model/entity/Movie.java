package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 13:50
 */
@Entity(nameInDb = "movie")
public class Movie {

    @Id(autoincrement = true)
    private Long id;

    private String name;

    private String nameChn;

    private String subName;

    private String subChnName;

    private double usToYuan;

    private int year;

    private String debut;

    private int isReal;

    private long budget;

    private String mojoId;

    @ToOne(joinProperty = "id")
    private GrossStat grossStat;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1042217376)
    private transient MovieDao myDao;

    @Generated(hash = 859527616)
    private transient Long grossStat__resolvedKey;

    @Generated(hash = 667280219)
    public Movie(Long id, String name, String nameChn, String subName,
            String subChnName, double usToYuan, int year, String debut, int isReal,
            long budget, String mojoId) {
        this.id = id;
        this.name = name;
        this.nameChn = nameChn;
        this.subName = subName;
        this.subChnName = subChnName;
        this.usToYuan = usToYuan;
        this.year = year;
        this.debut = debut;
        this.isReal = isReal;
        this.budget = budget;
        this.mojoId = mojoId;
    }

    @Generated(hash = 1263461133)
    public Movie() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameChn() {
        return this.nameChn;
    }

    public void setNameChn(String nameChn) {
        this.nameChn = nameChn;
    }

    public String getSubName() {
        return this.subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getSubChnName() {
        return this.subChnName;
    }

    public void setSubChnName(String subChnName) {
        this.subChnName = subChnName;
    }

    public double getUsToYuan() {
        return this.usToYuan;
    }

    public void setUsToYuan(double usToYuan) {
        this.usToYuan = usToYuan;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDebut() {
        return this.debut;
    }

    public void setDebut(String debut) {
        this.debut = debut;
    }

    public int getIsReal() {
        return this.isReal;
    }

    public void setIsReal(int isReal) {
        this.isReal = isReal;
    }

    public long getBudget() {
        return this.budget;
    }

    public void setBudget(long budget) {
        this.budget = budget;
    }

    public String getMojoId() {
        return this.mojoId;
    }

    public void setMojoId(String mojoId) {
        this.mojoId = mojoId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1042867517)
    public GrossStat getGrossStat() {
        Long __key = this.id;
        if (grossStat__resolvedKey == null
                || !grossStat__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GrossStatDao targetDao = daoSession.getGrossStatDao();
            GrossStat grossStatNew = targetDao.load(__key);
            synchronized (this) {
                grossStat = grossStatNew;
                grossStat__resolvedKey = __key;
            }
        }
        return grossStat;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2146775533)
    public void setGrossStat(GrossStat grossStat) {
        synchronized (this) {
            this.grossStat = grossStat;
            id = grossStat == null ? null : grossStat.getMovieId();
            grossStat__resolvedKey = id;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 215161401)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMovieDao() : null;
    }

}
