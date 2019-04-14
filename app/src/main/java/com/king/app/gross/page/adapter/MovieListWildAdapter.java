package com.king.app.gross.page.adapter;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMovieListBinding;
import com.king.app.gross.databinding.AdapterMovieListWildBinding;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MovieGridItem;

import java.util.Map;

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
        if (TextUtils.isEmpty(bean.getSubName())) {
            binding.tvName.setText(bean.getName());
        }
        else {
            binding.tvName.setText(bean.getName() + ": " + bean.getSubName());
        }
        if (TextUtils.isEmpty(bean.getChnName())) {
            binding.tvSubName.setVisibility(View.GONE);
            binding.tvIndex.setText(String.valueOf(bean.getName().charAt(0)));
        }
        else {
            binding.tvSubName.setVisibility(View.VISIBLE);
            binding.tvSubName.setText(bean.getChnName());
            binding.tvIndex.setText(String.valueOf(bean.getChnName().charAt(0)));
        }
        GradientDrawable drawable = (GradientDrawable) binding.tvIndex.getBackground();
        drawable.setColor(bean.getIndexColor());
        binding.cbCheck.setVisibility(isSelectionMode() ? View.VISIBLE:View.GONE);
        binding.cbCheck.setChecked(getCheckMap().get(bean.getBean().getId()) != null);
        binding.tvBudget.setText("Budget " + FormatUtil.formatUsGross(bean.getBean().getBudget()));
    }

}
