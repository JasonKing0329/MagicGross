package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/16 16:41
 */
@Entity(nameInDb = "label_movie")
public class LabelMovie {

    @Id(autoincrement = true)
    private Long id;

    private long movieId;

    private long typeId;

    private int order;

    @Generated(hash = 240506771)
    public LabelMovie(Long id, long movieId, long typeId, int order) {
        this.id = id;
        this.movieId = movieId;
        this.typeId = typeId;
        this.order = order;
    }

    @Generated(hash = 280148552)
    public LabelMovie() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMovieId() {
        return this.movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public long getTypeId() {
        return this.typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
