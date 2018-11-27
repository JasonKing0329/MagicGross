package com.king.app.gross.model.gross;

import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.compare.CompareChart;
import com.king.app.gross.page.gross.AxisData;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.view.widget.chart.adapter.LineData;
import com.king.app.gross.viewmodel.CompareViewModel;
import com.king.app.gross.viewmodel.bean.CompareItem;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.ObservableSource;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/26 13:49
 */
public class ChartModel {

    public static String formatAxisString(Region region, int position) {
        if (region == Region.CHN) {
            if (position < 100) {
                return position / 10 + "千万";
            }
            else {
                double ft = (double) position / (double) 100;
                return FormatUtil.pointZ(ft) + "亿";
            }
        }
        else {
            if (position < 100) {
                return position / 10 + "百万";
            }
            else if (position < 1000) {
                double ft = (double) position / (double) 100;
                return FormatUtil.pointZ(ft) + "千万";
            }
            else {
                double ft = (double) position / (double) 1000;
                return FormatUtil.pointZ(ft) + "亿";
            }
        }
    }

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
            // 出去非daily数据
            if (list.get(i).getBean() != null && list.get(i).getBean().getIsTotal() != 0) {
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

    public ObservableSource<CompareChart> createChart(List<CompareItem> list, int lineCount, Region region) {
        return observer -> {
            CompareChart chart = new CompareChart();
            List<CompareItem> dayList = new ArrayList<>();
            for (int i = 0; i < list.size(); i ++) {
                // 只加daily类型的
                if (list.get(i).isDay() && !list.get(i).isTotal()) {
                    dayList.add(list.get(i));
                }
            }

            if (dayList.size() == 0) {
                observer.onNext(chart);
                return;
            }

            // 按照week-day排序，left放最后
            Collections.sort(dayList, new CompareKeyComparator());

            chart.setxCount(dayList.size());
            chart.setxTextList(new ArrayList<>());
            chart.setLineDataList(new ArrayList<>());
            for (int i = 0; i < lineCount; i ++) {
                chart.getLineDataList().add(new LineData());
            }
            long max = 0;
            // 时间线性
            for (int day = 0; day < dayList.size(); day ++) {
                CompareItem dayItem = dayList.get(day);
                String key = dayItem.getKey();
                if (key.startsWith(AppConstants.COMPARE_TITLE_WEEK)) {
                    key = key.substring(AppConstants.COMPARE_TITLE_WEEK.length());
                }
                chart.getxTextList().add(key);
                List<SimpleGross> grosses = dayItem.getGrossList();
                // movie所代表的线段
                for (int line = 0; line < grosses.size(); line ++) {
                    LineData lineData = chart.getLineDataList().get(line);
                    SimpleGross gross = grosses.get(line);
                    if (gross == null) {

                    }
                    else {
                        if (lineData.getValues() == null) {
                            lineData.setValues(new ArrayList<>());
                            lineData.setValuesText(new ArrayList<>());
                            lineData.setStartX(day);
                        }
                        int value;
                        if (gross.isLeft() || (gross.getBean() != null && gross.getBean().getIsLeftAfterDay() > 0)) {
                            // left在线段上与前一天持平
                            try {
                                value = lineData.getValues().get(lineData.getValues().size() - 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                value = 0;
                            }
                        }
                        else {
                            if (region == Region.CHN) {
                                // 人民币单位以百万计
                                value = (int) (gross.getGrossValue() / 1000000);
                            }
                            else {
                                // 美元单位以十万计
                                value = (int) (gross.getGrossValue() / 100000);
                            }
                            if (gross.getGrossValue() > max) {
                                max = gross.getGrossValue();
                            }
                        }
                        lineData.getValues().add(value);
                        if (line == dayItem.getWinIndex()) {
                            lineData.getValuesText().add(gross.getGrossDay());
                        }
                        else {
                            lineData.getValuesText().add("");
                        }
                        lineData.setEndX(day);
                    }
                }
            }

            if (region == Region.CHN) {
                // 人民币单位以百万计
                int top = (int) (max / 1000000 + 1);
                chart.setyCount(top);
            }
            else {
                // 美元单位以十万计
                int top = (int) (max / 100000 + 1);
                chart.setyCount(top);
            }
            observer.onNext(chart);
        };
    }

    private class CompareKeyComparator implements Comparator<CompareItem> {

        @Override
        public int compare(CompareItem o1, CompareItem o2) {
            String key1 = formatKey(o1);
            String key2 = formatKey(o2);
            return key1.compareTo(key2);
        }

        private String formatKey(CompareItem item) {
            String key = item.getKey();
            // left排在最后
            if (AppConstants.COMPARE_TITLE_LEFT.equals(key)) {
                key = "ZZZZZ";
            }
            return key;
        }
    }

}
