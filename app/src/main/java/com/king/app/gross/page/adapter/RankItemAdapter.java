package com.king.app.gross.page.adapter;

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
    }

}
