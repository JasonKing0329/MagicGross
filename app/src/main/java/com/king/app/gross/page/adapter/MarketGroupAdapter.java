package com.king.app.gross.page.adapter;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.HeadChildBindingAdapter;
import com.king.app.gross.databinding.AdapterContinentGrossBinding;
import com.king.app.gross.databinding.AdapterMarketGrossBinding;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.page.bean.ContinentGross;

public class MarketGroupAdapter extends HeadChildBindingAdapter<AdapterContinentGrossBinding, AdapterMarketGrossBinding, ContinentGross, MarketGross> {

    @Override
    protected Class getItemClass() {
        return MarketGross.class;
    }

    @Override
    protected int getHeaderRes() {
        return R.layout.adapter_continent_gross;
    }

    @Override
    protected int getItemRes() {
        return R.layout.adapter_market_gross;
    }

    @Override
    protected void onBindHead(AdapterContinentGrossBinding binding, int position, ContinentGross head) {
        binding.setBean(head);
    }

    @Override
    protected void onBindItem(AdapterMarketGrossBinding binding, int position, MarketGross bean) {
        binding.setBean(bean);
        binding.tvIndex.setVisibility(View.GONE);
    }
}
