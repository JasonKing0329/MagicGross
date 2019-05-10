package com.king.app.gross.page.adapter;

import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterCompareMovieBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class CompareMovieAdapter extends BaseBindingAdapter<AdapterCompareMovieBinding, Movie> {

    private List<Integer> colorList;

    public CompareMovieAdapter() {
    }

    @Override
    public void setList(List<Movie> list) {
        super.setList(list);
        if (colorList == null) {
            colorList = new ArrayList<>();
        }
        else {
            colorList.clear();
        }
        if (list != null) {
            for (Movie movie:list) {
                colorList.add(ColorUtil.randomWhiteTextBgColor());
            }
        }
    }

    public void refreshColors() {
        if (colorList != null) {
            for (int i = 0; i < colorList.size(); i ++) {
                colorList.set(i, ColorUtil.randomWhiteTextBgColor());
            }
        }
    }

    public List<Integer> getColorList() {
        return colorList;
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_compare_movie;
    }

    @Override
    protected void onBindItem(AdapterCompareMovieBinding binding, int position, Movie bean) {
        binding.setBean(bean);
        binding.executePendingBindings();

        binding.tvName.setTextColor(colorList.get(position));
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
