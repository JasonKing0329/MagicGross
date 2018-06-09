package com.king.app.gross.viewmodel.bean;

import com.king.app.gross.conf.GrossDateType;
import com.king.app.gross.conf.Region;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/8 11:53
 */
public class GrossPage {

    public Region region;

    public GrossDateType dateType;

    public List<SimpleGross> list;

    public List<WeekGross> weekList;
}
