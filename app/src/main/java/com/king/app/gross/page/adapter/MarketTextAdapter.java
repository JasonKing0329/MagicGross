package com.king.app.gross.page.adapter;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMarketItemBinding;
import com.king.app.gross.model.entity.Market;

public class MarketTextAdapter extends BaseBindingAdapter<AdapterMarketItemBinding, Market> {

    private int mSelection = -1;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_market_item;
    }

    @Override
    protected void onBindItem(AdapterMarketItemBinding binding, int position, Market bean) {
        binding.setBean(bean);
        binding.tvChnName.setSelected(position == mSelection);
        binding.tvName.setSelected(position == mSelection);
    }

    @Override
    protected void onClickItem(View v, int position) {
        if (mSelection != -1) {
            notifyItemChanged(mSelection);
        }
        mSelection = position;
        notifyItemChanged(position);
        super.onClickItem(v, position);
    }
}
