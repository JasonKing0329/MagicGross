package com.king.app.gross.view.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/11 15:02
 */
public class DatePickerFragment extends DialogFragment  {

    private DatePickerDialog.OnDateSetListener onDateSetListener;

    private int year;

    private int month;

    private int day;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        if (year == 0) {
            year = calendar.get(Calendar.YEAR);
        }
        if (month == 0) {
            month = calendar.get(Calendar.MONTH);
        }
        if (day == 0) {
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }
        return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    /**
     *
     * @param date yyyy-MM-dd
     */
    public void setDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
