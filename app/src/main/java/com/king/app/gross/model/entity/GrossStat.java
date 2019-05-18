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
@Entity(nameInDb = "gross_stat")
public class GrossStat {

    @Id
    private Long movieId;

    private long us;

    private long chn;

    private long world;

    private long usOpening;

    private long chnOpening;

    private long worldOpening;

    @Generated(hash = 421825181)
    public GrossStat(Long movieId, long us, long chn, long world, long usOpening,
            long chnOpening, long worldOpening) {
        this.movieId = movieId;
        this.us = us;
        this.chn = chn;
        this.world = world;
        this.usOpening = usOpening;
        this.chnOpening = chnOpening;
        this.worldOpening = worldOpening;
    }

    @Generated(hash = 1373936138)
    public GrossStat() {
    }

    public Long getMovieId() {
        return this.movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public long getUs() {
        return this.us;
    }

    public void setUs(long us) {
        this.us = us;
    }

    public long getChn() {
        return this.chn;
    }

    public void setChn(long chn) {
        this.chn = chn;
    }

    public long getWorld() {
        return this.world;
    }

    public void setWorld(long world) {
        this.world = world;
    }

    public long getUsOpening() {
        return this.usOpening;
    }

    public void setUsOpening(long usOpening) {
        this.usOpening = usOpening;
    }

    public long getChnOpening() {
        return this.chnOpening;
    }

    public void setChnOpening(long chnOpening) {
        this.chnOpening = chnOpening;
    }

    public long getWorldOpening() {
        return this.worldOpening;
    }

    public void setWorldOpening(long worldOpening) {
        this.worldOpening = worldOpening;
    }

}
