package com.king.app.gross.page.adapter;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterGrossSimpleBinding;
import com.king.app.gross.viewmodel.bean.SimpleGross;

import java.util.Map;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class GrossSimpleAdapter extends BaseBindingAdapter<AdapterGrossSimpleBinding, SimpleGross> {

    private boolean isSelectionMode;

    private Map<Long, Boolean> checkMap;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_gross_simple;
    }

    @Override
    protected void onBindItem(AdapterGrossSimpleBinding binding, int position, SimpleGross bean) {
        binding.setGross(bean);
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
