package com.king.app.gross.page.adapter;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterGrossSimpleBinding;
import com.king.app.gross.viewmodel.bean.SimpleGross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class GrossSimpleAdapter extends BaseBindingAdapter<AdapterGrossSimpleBinding, SimpleGross> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_gross_simple;
    }

    @Override
    protected void onBindItem(AdapterGrossSimpleBinding binding, int position, SimpleGross bean) {
        binding.setGross(bean);
        binding.executePendingBindings();
    }

}
