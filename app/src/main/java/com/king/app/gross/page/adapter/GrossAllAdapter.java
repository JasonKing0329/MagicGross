package com.king.app.gross.page.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.model.entity.Gross;

import java.util.Map;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class GrossAllAdapter extends BaseRecyclerAdapter<GrossAllAdapter.GrossHolder, Gross> {

    private boolean isSelectionMode;

    private Map<Long, Boolean> checkMap;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_gross_all;
    }

    @Override
    protected GrossHolder newViewHolder(View view) {
        return new GrossHolder(view);
    }

    @Override
    protected void onBindView(GrossHolder holder, int position, Gross data) {

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
            if (checkMap.get(list.get(position).getId()) == null) {
                checkMap.put(list.get(position).getId(), true);
            }
            else {
                checkMap.remove(list.get(position).getId());
            }
            notifyItemChanged(position);
        }
        else {
            super.onClickItem(v, position);
        }
    }

    public static class GrossHolder extends RecyclerView.ViewHolder {
        CardView groupItem;
        TextView tvName;
        TextView tvNameChn;
        TextView tvNameSub;
        CheckBox cbCheck;

        public GrossHolder(View itemView) {
            super(itemView);

//            groupItem = itemView.findViewById(R.id.group_item);
//            tvName = itemView.findViewById(R.id.tv_name);
//            tvNameSub = itemView.findViewById(R.id.tv_name_sub);
//            tvNameChn = itemView.findViewById(R.id.tv_name_chn);
//            cbCheck = itemView.findViewById(R.id.cb_check);
        }
    }
}
