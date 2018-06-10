package com.king.app.gross.page.adapter;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterRankTagTypeBinding;
import com.king.app.gross.viewmodel.bean.RankTag;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class TagRankTypeAdapter extends BaseBindingAdapter<AdapterRankTagTypeBinding, RankTag> {

    private int selection;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_rank_tag_type;
    }

    @Override
    protected void onBindItem(AdapterRankTagTypeBinding binding, int position, RankTag bean) {
        binding.setTag(bean);
        binding.executePendingBindings();

        binding.tvTag.setSelected(position == selection);
    }

    @Override
    protected void onClickItem(View v, int position) {
        if (position != selection) {
            notifyItemChanged(selection);
            selection = position;
            notifyItemChanged(selection);
        }
        super.onClickItem(v, position);
    }
}
