package com.king.app.gross.page.adapter;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterGrossWeekBinding;
import com.king.app.gross.viewmodel.bean.WeekGross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class GrossWeekAdapter extends BaseBindingAdapter<AdapterGrossWeekBinding, WeekGross> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_gross_week;
    }

    @Override
    protected void onBindItem(AdapterGrossWeekBinding binding, int position, WeekGross bean) {
        binding.setGross(bean);
    }
}
