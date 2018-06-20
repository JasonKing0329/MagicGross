package com.king.app.gross.page.adapter;

import android.view.View;
import android.widget.TextView;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterCompareItemBinding;
import com.king.app.gross.viewmodel.bean.CompareItem;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class CompareItemAdapter extends BaseBindingAdapter<AdapterCompareItemBinding, CompareItem> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_compare_item;
    }

    @Override
    protected void onBindItem(AdapterCompareItemBinding binding, int position, CompareItem bean) {
        binding.setBean(bean);
        binding.executePendingBindings();

        // 目前最多只支持3个
        TextView[] views = new TextView[] {
                binding.tvValue1, binding.tvValue2, binding.tvValue3
        };
        for (int i = 0; i < views.length; i ++) {
            if (i < bean.getValues().size()) {
                views[i].setText(bean.getValues().get(i));
                views[i].setVisibility(View.VISIBLE);
            }
            else {
                views[i].setVisibility(View.GONE);
            }

            if (i == bean.getWinIndex()) {
                views[i].setTextColor(views[i].getResources().getColor(R.color.actionbar_bg));
            }
            else {
                views[i].setTextColor(views[i].getResources().getColor(R.color.text_second));
            }
        }
    }

}
