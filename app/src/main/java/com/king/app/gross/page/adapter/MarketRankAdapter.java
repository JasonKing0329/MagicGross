package com.king.app.gross.page.adapter;

import android.graphics.PorterDuff;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMarketRankItemBinding;
import com.king.app.gross.model.AppGlide;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.viewmodel.bean.RankItem;

public class MarketRankAdapter extends BaseBindingAdapter<AdapterMarketRankItemBinding, RankItem<MarketGross>> {
    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_market_rank_item;
    }

    @Override
    protected void onBindItem(AdapterMarketRankItemBinding binding, int position, RankItem<MarketGross> bean) {
        binding.setItem(bean);
        binding.divider.setVisibility(position == 0 ? View.GONE:View.VISIBLE);
        binding.tvRank.setText(String.valueOf(position + 1));

        int color;
        if (position == 0) {
            color = binding.ivRank.getResources().getColor(R.color.rank_top);
        }
        else if (position < 3) {
            color = binding.ivRank.getResources().getColor(R.color.rank_second);
        }
        else {
            color = binding.ivRank.getResources().getColor(R.color.rank_other);
        }
        binding.ivRank.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (position < 3) {
            binding.clImage.setVisibility(View.VISIBLE);
            binding.tvName.setVisibility(View.GONE);
            AppGlide.with(binding.ivImage.getContext())
                    .load(bean.getImageUrl())
                    .error(R.drawable.bg_movie_default)
                    .into(binding.ivImage);
        }
        else {
            binding.clImage.setVisibility(View.GONE);
            binding.tvName.setVisibility(View.VISIBLE);
        }
    }
}
