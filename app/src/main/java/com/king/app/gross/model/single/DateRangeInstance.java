package com.king.app.gross.model.single;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/19 13:32
 */
public class DateRangeInstance {

    private String mStartDate;
    private String mEndDate;

    private static DateRangeInstance instance;

    public static DateRangeInstance getInstance() {
        if (instance == null) {
            instance = new DateRangeInstance();
        }
        return instance;
    }

    public void setStartDate(String mStartDate) {
        this.mStartDate = mStartDate;
    }

    public void setEndDate(String mEndDate) {
        this.mEndDate = mEndDate;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public String getEndDate() {
        return mEndDate;
    }
}
