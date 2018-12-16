package com.king.app.gross.page.adapter;

import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterMovieListBinding;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MovieGridItem;

import java.util.Map;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class MovieListAdapter extends BaseBindingAdapter<AdapterMovieListBinding, MovieGridItem> {

    private boolean isSelectionMode;

    private Map<Long, Boolean> checkMap;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_movie_list;
    }

    @Override
    protected void onBindItem(AdapterMovieListBinding binding, int position, MovieGridItem bean) {
        binding.setBean(bean);
        if (TextUtils.isEmpty(bean.getSubName())) {
            binding.tvNameSub.setVisibility(View.GONE);
        }
        else {
            binding.tvNameSub.setVisibility(View.VISIBLE);
            binding.tvNameSub.setText(bean.getSubName());
        }
        if (TextUtils.isEmpty(bean.getChnName())) {
            binding.tvNameChn.setVisibility(View.GONE);
        }
        else {
            binding.tvNameChn.setVisibility(View.VISIBLE);
            binding.tvNameChn.setText(bean.getChnName());
        }
        binding.cbCheck.setVisibility(isSelectionMode ? View.VISIBLE:View.GONE);
        binding.cbCheck.setChecked(checkMap.get(bean.getBean().getId()) != null);
        binding.tvBudget.setText(FormatUtil.formatUsGross(bean.getBean().getBudget()));
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public void setCheckMap(Map<Long, Boolean> checkMap) {
        this.checkMap = checkMap;
    }

    @Override
    protected void onClickItem(View v, int position) {
        if (isSelectionMode) {
            if (checkMap.get(list.get(position).getBean().getId()) == null) {
                checkMap.put(list.get(position).getBean().getId(), true);
            }
            else {
                checkMap.remove(list.get(position).getBean().getId());
            }
            notifyItemChanged(position);
        }
        else {
            super.onClickItem(v, position);
        }
    }
}
