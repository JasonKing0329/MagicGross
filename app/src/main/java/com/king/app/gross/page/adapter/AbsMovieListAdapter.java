package com.king.app.gross.page.adapter;

import android.databinding.ViewDataBinding;
import android.view.View;

import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.viewmodel.bean.MovieGridItem;

import java.util.Map;

public abstract class AbsMovieListAdapter<V extends ViewDataBinding> extends BaseBindingAdapter<V, MovieGridItem> {

    private boolean isSelectionMode;

    private Map<Long, Boolean> checkMap;

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setCheckMap(Map<Long, Boolean> checkMap) {
        this.checkMap = checkMap;
    }

    public Map<Long, Boolean> getCheckMap() {
        return checkMap;
    }

    @Override
    protected void onClickItem(View v, int position) {
        if (isSelectionMode()) {
            if (getCheckMap().get(list.get(position).getBean().getId()) == null) {
                getCheckMap().put(list.get(position).getBean().getId(), true);
            }
            else {
                getCheckMap().remove(list.get(position).getBean().getId());
            }
            notifyItemChanged(position);
        }
        else {
            super.onClickItem(v, position);
        }
    }
}
