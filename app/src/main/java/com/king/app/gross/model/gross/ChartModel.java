package com.king.app.gross.model.gross;

import com.king.app.gross.conf.Region;
import com.king.app.gross.page.gross.AxisData;
import com.king.app.gross.view.widget.chart.adapter.LineData;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/26 13:49
 */
public class ChartModel {

    public AxisData createAxisData(Region region, List<SimpleGross> list) {
        AxisData data = new AxisData();
        long max = 0;
        for (SimpleGross gross:list) {
            if (gross.getGrossValue() > max) {
                // 除去left
                // 汇总类（oversea/world wide）
                if (gross.getBean() == null) {
                    if (gross.isLeft()) {
                        continue;
                    }
                }
                else {
                    if (gross.getBean().getIsLeftAfterDay() > 0) {
                        continue;
                    }
                }
                max = gross.getGrossValue();
            }
        }
        if (region == Region.CHN) {
            // 人民币单位以百万计
            int top = (int) (max / 1000000 + 1);
            data.total = top;
            data.totalWeight = top;
        }
        else {
            // 美元单位以十万计
            int top = (int) (max / 100000 + 1);
            data.total = top;
            data.totalWeight = top;
        }
        return data;
    }

    public LineData createLineData(Region region, List<SimpleGross> list) {
        LineData data = new LineData();
        data.setStartX(0);
        data.setEndX(list.size() - 1);
        data.setValues(new ArrayList<>());
        data.setValuesText(new ArrayList<>());
        for (int i = 0; i < list.size(); i ++) {
            boolean isLeft = false;
            // 汇总类（oversea/world wide）
            if (list.get(i).getBean() == null) {
                if (list.get(i).isLeft()) {
                    isLeft = true;
                }
            }
            else {
                if (list.get(i).getBean().getIsLeftAfterDay() > 0) {
                    isLeft = true;
                }
            }
            // 除去left
            if (isLeft) {
                // 显示余量文字
                data.getValuesText().add(list.get(i).getGrossDay());
                // 曲线显示为持平
                if (i > 0) {
                    data.getValues().add(data.getValues().get(i - 1));
                }
                else {
                    data.getValues().add(0);
                }
                continue;
            }

            if (region == Region.CHN) {
                // 人民币单位以百万计
                data.getValues().add((int) (list.get(i).getGrossValue() / 1000000));
            }
            else {
                // 美元单位以十万计
                data.getValues().add((int) (list.get(i).getGrossValue() / 100000));
            }

            // 只显示提前场、首日和每周6的value text
            if (Integer.parseInt(list.get(i).getDayOfWeek()) == 6 || Integer.parseInt(list.get(i).getDay()) <= 1) {
                data.getValuesText().add(list.get(i).getGrossDay());
            }
            else {
                data.getValuesText().add("");
            }
        }
        return data;
    }
}
