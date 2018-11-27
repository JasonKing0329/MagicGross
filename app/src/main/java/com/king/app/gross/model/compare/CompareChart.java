package com.king.app.gross.model.compare;

import com.king.app.gross.view.widget.chart.adapter.LineData;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/27 11:36
 */
public class CompareChart {

    private int xCount;

    private int yCount;

    private List<String> xTextList;

    private List<LineData> lineDataList;

    public int getxCount() {
        return xCount;
    }

    public void setxCount(int xCount) {
        this.xCount = xCount;
    }

    public int getyCount() {
        return yCount;
    }

    public void setyCount(int yCount) {
        this.yCount = yCount;
    }

    public List<String> getxTextList() {
        return xTextList;
    }

    public void setxTextList(List<String> xTextList) {
        this.xTextList = xTextList;
    }

    public List<LineData> getLineDataList() {
        return lineDataList;
    }

    public void setLineDataList(List<LineData> lineDataList) {
        this.lineDataList = lineDataList;
    }
}
