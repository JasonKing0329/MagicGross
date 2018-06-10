package com.king.app.gross.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/8 10:04
 */
public class FormatUtil {

    public static String formatUsGross(long gross) {
        DecimalFormat df = new DecimalFormat("###,###,###,###");
        return "$" + df.format(gross);
    }

    public static String formatChnGross(long gross) {
        DecimalFormat df = new DecimalFormat("###,###,###,###");
        return "￥" + df.format(gross);
    }

    /**
     *
     * @param drop
     * @return
     */
    public static String formatDrop(double drop) {
        String num = pointZZ(drop * 100);
        return num + "%";
    }

    /**
     * 将double转化为小数点后最多两位，如果小数点后末尾是0直接去掉
     * @param number
     * @return
     */
    public static String atMostDecimal2(double number) {
        return formatNumber(pointZZ(number));
    }

    /**
     * 金额转换
     *
     * @param beforeFloat 转换前的金额
     * @return 转换后的金额 小数点后保留两位 XX.00
     */
    public static String pointZZ(Double beforeFloat) {
        NumberFormat formatter = new DecimalFormat("####0.00");
        return formatter.format(beforeFloat) + "";
    }

    /**
     * 金额转换
     *
     * @param beforeFloat 转换前的金额
     * @return 转换后的金额 小数点后保留一位 XX.0
     */
    public static String pointZ(Double beforeFloat) {
        NumberFormat formatter = new DecimalFormat("####0.0");
        return formatter.format(beforeFloat) + "";
    }

    /**
     * 格式化字符串，去掉末尾多余的0
     *
     * @param money
     * @return
     */
    public static String formatNumber(String money) {
        StringBuffer buffer = new StringBuffer(money);
        if (money.contains(".")) {
            for (int i = money.length() - 1; i > 1; i--) {
                if (buffer.charAt(i) == '0') {
                    buffer.deleteCharAt(i);
                    if (buffer.charAt(i - 1) == '.') {
                        buffer.deleteCharAt(i - 1);
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }
        return buffer.toString();
    }

}
