package com.king.app.gross.page.adapter;

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

        for (int i = 0; i < bean.getValues().size(); i ++) {
            binding.groupCompare.setCell(i, bean.getValues().get(i));
            if (i == bean.getWinIndex()) {
                binding.groupCompare.setCellTextColor(i, binding.groupCompare.getResources().getColor(R.color.actionbar_bg));
            }
            else {
                binding.groupCompare.setCellTextColor(i, binding.groupCompare.getResources().getColor(R.color.text_second));
            }
        }
    }

}
