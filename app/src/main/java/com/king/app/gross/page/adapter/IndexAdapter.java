package com.king.app.gross.page.adapter;

import android.support.constraint.ConstraintLayout;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterIndexBinding;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/13 10:26
 */
public class IndexAdapter extends BaseBindingAdapter<AdapterIndexBinding, String> {
    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_index;
    }

    @Override
    protected void onBindItem(AdapterIndexBinding binding, int position, String bean) {
        binding.tvName.setText(bean);
        if (position % 2 == 0) {
            binding.vMarginLeft.setVisibility(View.GONE);
            binding.vMarginRight.setVisibility(View.VISIBLE);
        }
        else {
            binding.vMarginLeft.setVisibility(View.VISIBLE);
            binding.vMarginRight.setVisibility(View.GONE);
        }
    }
}
