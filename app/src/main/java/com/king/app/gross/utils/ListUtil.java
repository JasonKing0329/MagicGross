package com.king.app.gross.utils;

import java.util.List;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/3/26 11:35
 */
public class ListUtil {

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    public static int getSize(List list) {
        return list == null ? 0 : list.size();
    }
}
