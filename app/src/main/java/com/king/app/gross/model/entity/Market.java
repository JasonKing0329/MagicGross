package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/21 13:50
 */
@Entity(nameInDb = "market")
public class Market {

    @Id(autoincrement = true)
    private Long id;

    private String name;

    private String nameChn;

    private String continent;

    @Transient
    private String imageUrl;

    @Generated(hash = 1504232295)
    public Market(Long id, String name, String nameChn, String continent) {
        this.id = id;
        this.name = name;
        this.nameChn = nameChn;
        this.continent = continent;
    }

    @Generated(hash = 1454995179)
    public Market() {
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

    public String getContinent() {
        return this.continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
