package com.king.app.gross.page.adapter;

import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterRankItemBinding;
import com.king.app.gross.viewmodel.bean.RankItem;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class RankItemAdapter extends BaseBindingAdapter<AdapterRankItemBinding, RankItem> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_rank_item;
    }

    @Override
    protected void onBindItem(AdapterRankItemBinding binding, int position, RankItem bean) {
        binding.setItem(bean);
        binding.executePendingBindings();

        binding.divider.setVisibility(position == 0 ? View.GONE:View.VISIBLE);

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
    }

}
