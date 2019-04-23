package com.king.app.gross.page.adapter;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMarketGrossBinding;
import com.king.app.gross.model.entity.MarketGross;

public class MarketGrossAdapter extends BaseBindingAdapter<AdapterMarketGrossBinding, MarketGross> {
    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_market_gross;
    }

    @Override
    protected void onBindItem(AdapterMarketGrossBinding binding, int position, MarketGross bean) {
        binding.setBean(bean);
        binding.tvIndex.setText(String.valueOf(position + 1));
    }
}
