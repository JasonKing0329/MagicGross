package com.king.app.gross.page.adapter;

import android.text.TextUtils;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterFullChartMovieBinding;
import com.king.app.gross.model.entity.Movie;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class FullChartMovieAdapter extends BaseBindingAdapter<AdapterFullChartMovieBinding, Movie> {

    private List<Integer> colorList;

    public void setColorList(List<Integer> colorList) {
        this.colorList = colorList;
    }

    public List<Integer> getColorList() {
        return colorList;
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_full_chart_movie;
    }

    @Override
    protected void onBindItem(AdapterFullChartMovieBinding binding, int position, Movie bean) {
        StringBuffer buffer = new StringBuffer(bean.getName());
        if (!TextUtils.isEmpty(bean.getSubName())) {
            buffer.append(":").append(bean.getSubName());
        }
        if (!TextUtils.isEmpty(bean.getNameChn())) {
            buffer.append("\n").append(bean.getNameChn());
            if (!TextUtils.isEmpty(bean.getSubChnName())) {
                buffer.append("：").append(bean.getSubChnName());
            }
        }
        binding.tvName.setText(buffer.toString());
        binding.tvName.setTextColor(colorList.get(position));
    }

}
