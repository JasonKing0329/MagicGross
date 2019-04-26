package com.king.app.gross.page.adapter;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.HeadChildBindingAdapter;
import com.king.app.gross.databinding.AdapterMarketContinentBinding;
import com.king.app.gross.databinding.AdapterMarketItemBinding;
import com.king.app.gross.model.entity.Market;

public class MarketTextAdapter extends HeadChildBindingAdapter<AdapterMarketContinentBinding, AdapterMarketItemBinding, String, Market> {

    private int mSelection = -1;

    private OnClickMarketListener onClickMarketListener;

    public void setOnClickMarketListener(OnClickMarketListener onClickMarketListener) {
        this.onClickMarketListener = onClickMarketListener;
    }

    @Override
    protected Class getItemClass() {
        return Market.class;
    }

    @Override
    protected int getHeaderRes() {
        return R.layout.adapter_market_continent;
    }

    @Override
    protected int getItemRes() {
        return R.layout.adapter_market_item;
    }

    @Override
    protected void onBindHead(AdapterMarketContinentBinding binding, int position, String head) {
        binding.setContinent(head);
        binding.tvName.setOnClickListener(v -> {
            onClickItem(v, position);
            if (onClickMarketListener != null) {
                onClickMarketListener.onClickContinent(position, head);
            }
        });
        binding.tvName.setSelected(position == mSelection);
    }

    @Override
    protected void onBindItem(AdapterMarketItemBinding binding, int position, Market bean) {
        binding.setBean(bean);
        binding.tvChnName.setSelected(position == mSelection);
        binding.tvName.setSelected(position == mSelection);
        binding.clItem.setOnClickListener(v -> {
            onClickItem(v, position);
            if (onClickMarketListener != null) {
                onClickMarketListener.onClickMarket(position, bean);
            }
        });
        binding.tvChnName.setSelected(position == mSelection);
        binding.tvName.setSelected(position == mSelection);
    }

    protected void onClickItem(View v, int position) {
        if (mSelection != -1) {
            notifyItemChanged(mSelection);
        }
        mSelection = position;
        notifyItemChanged(position);
    }

    public boolean isMarketSelected() {
        if (mSelection != -1 && list.get(mSelection) instanceof Market) {
            return true;
        }
        return false;
    }

    public interface OnClickMarketListener {
        void onClickMarket(int position, Market market);
        void onClickContinent(int position, String continent);
    }
}
