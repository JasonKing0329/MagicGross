package com.king.app.gross.page.adapter;

import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterCompareMovieBinding;
import com.king.app.gross.model.entity.Movie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class CompareMovieAdapter extends BaseBindingAdapter<AdapterCompareMovieBinding, Movie> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_compare_movie;
    }

    @Override
    protected void onBindItem(AdapterCompareMovieBinding binding, int position, Movie bean) {
        binding.setBean(bean);
        binding.executePendingBindings();

        if (TextUtils.isEmpty(bean.getSubName())) {
            binding.tvNameSub.setVisibility(View.GONE);
        }
        else {
            binding.tvNameSub.setText(bean.getSubName());
            binding.tvNameSub.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(bean.getNameChn())) {
            binding.tvNameChn.setVisibility(View.GONE);
        }
        else {
            if (TextUtils.isEmpty(bean.getSubChnName())) {
                binding.tvNameChn.setText(bean.getNameChn());
            }
            else {
                binding.tvNameChn.setText(bean.getNameChn() + "：" + bean.getSubChnName());
            }
            binding.tvNameChn.setVisibility(View.VISIBLE);
        }
    }

}
