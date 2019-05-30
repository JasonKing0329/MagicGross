package com.king.app.gross.page.adapter;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMarketPageItemBinding;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.viewmodel.bean.RankItem;

public class MarketPageAdapter extends BaseBindingAdapter<AdapterMarketPageItemBinding, RankItem<MarketGross>> {
    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_market_page_item;
    }

    @Override
    protected void onBindItem(AdapterMarketPageItemBinding binding, int position, RankItem<MarketGross> bean) {
        binding.setRankItem(bean);
        binding.setGross(bean.getData());
    }
}
