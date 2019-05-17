package com.king.app.gross.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/16 16:41
 */
@Entity(nameInDb = "label")
public class Label {

    @Id(autoincrement = true)
    private Long id;

    /**
     * AppConstants.LABEL_XX
     */
    private int type;

    private String name;

    private String nameChn;

    private String description;

    @Generated(hash = 1520089167)
    public Label(Long id, int type, String name, String nameChn,
            String description) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.nameChn = nameChn;
        this.description = description;
    }

    @Generated(hash = 2137109701)
    public Label() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
