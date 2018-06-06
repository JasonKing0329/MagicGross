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

    @Generated(hash = 647948982)
    public Movie(Long id, String name, String nameChn) {
        this.id = id;
        this.name = name;
        this.nameChn = nameChn;
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
}
