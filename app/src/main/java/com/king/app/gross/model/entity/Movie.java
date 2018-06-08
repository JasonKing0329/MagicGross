package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

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

    @Generated(hash = 354994842)
    public Movie(Long id, String name, String nameChn, String subName,
            String subChnName, double usToYuan) {
        this.id = id;
        this.name = name;
        this.nameChn = nameChn;
        this.subName = subName;
        this.subChnName = subChnName;
        this.usToYuan = usToYuan;
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

}
