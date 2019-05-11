package com.king.app.gross.page.adapter;

import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.databinding.AdapterMovieListWildBinding;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MovieGridItem;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class MovieListWildAdapter extends AbsMovieListAdapter<AdapterMovieListWildBinding> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_movie_list_wild;
    }

    @Override
    protected void onBindItem(AdapterMovieListWildBinding binding, int position, MovieGridItem bean) {
        binding.setBean(bean);
        binding.tvIndex.setText(String.valueOf(position + 1));
        if (TextUtils.isEmpty(bean.getSubName())) {
            binding.tvName.setText(bean.getName());
        }
        else {
            binding.tvName.setText(bean.getName() + ": " + bean.getSubName());
        }
        if (TextUtils.isEmpty(bean.getChnName())) {
            binding.tvSubName.setVisibility(View.GONE);
        }
        else {
            binding.tvSubName.setVisibility(View.VISIBLE);
            binding.tvSubName.setText(bean.getChnName());
        }
        binding.cbCheck.setVisibility(isSelectionMode() ? View.VISIBLE:View.GONE);
        binding.cbCheck.setChecked(getCheckMap().get(bean.getBean().getId()) != null);
        if (bean.getBean().getBudget() > 0) {
            binding.tvBudget.setText("Budget " + FormatUtil.formatUsGross(bean.getBean().getBudget()));
            binding.tvBudget.setVisibility(View.VISIBLE);
        }
        else {
            binding.tvBudget.setVisibility(View.GONE);
        }
    }

}
