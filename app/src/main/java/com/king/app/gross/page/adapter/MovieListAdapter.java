package com.king.app.gross.page.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.utils.FormatUtil;

import java.util.Map;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:47
 */
public class MovieListAdapter extends BaseRecyclerAdapter<MovieListAdapter.MovieHolder, Movie> {

    private boolean isSelectionMode;

    private Map<Long, Boolean> checkMap;

    private int mSortType;

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_movie_list;
    }

    @Override
    protected MovieHolder newViewHolder(View view) {
        return new MovieHolder(view);
    }

    @Override
    protected void onBindView(MovieHolder holder, int position, Movie data) {
        holder.tvName.setText(data.getName());
        if (TextUtils.isEmpty(data.getSubName())) {
            holder.tvNameSub.setVisibility(View.GONE);
        }
        else {
            holder.tvNameSub.setText(data.getSubName());
            holder.tvNameSub.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(data.getSubChnName())) {
            holder.tvNameChn.setText(data.getNameChn());
        }
        else {
            holder.tvNameChn.setText(data.getNameChn() + "：" + data.getSubChnName());
        }
        holder.tvDate.setText(data.getDebut());
        holder.cbCheck.setVisibility(isSelectionMode ? View.VISIBLE:View.GONE);
        holder.cbCheck.setChecked(checkMap.get(data.getId()) != null);

        holder.tvGross.setText(FormatUtil.formatUsGross(new DailyModel(data).queryTotalGross(Region.NA.ordinal())));

        if (mSortType == AppConstants.MOVIE_SORT_DATE) {
            if (!TextUtils.isEmpty(data.getDebut())) {
                holder.tvFlag.setText(data.getDebut().substring(0, 4));
            }
            else {
                holder.tvFlag.setText("#");
            }
        }
        else {
            if (!TextUtils.isEmpty(data.getName())) {
                holder.tvFlag.setText(data.getName().substring(0, 1));
            }
            else {
                holder.tvFlag.setText("#");
            }
        }
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public void setCheckMap(Map<Long, Boolean> checkMap) {
        this.checkMap = checkMap;
    }

    public void setSortType(int mSortType) {
        this.mSortType = mSortType;
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

    public static class MovieHolder extends RecyclerView.ViewHolder {
        CardView groupItem;
        TextView tvFlag;
        TextView tvName;
        TextView tvNameChn;
        TextView tvNameSub;
        TextView tvDate;
        TextView tvGross;
        CheckBox cbCheck;

        public MovieHolder(View itemView) {
            super(itemView);

            groupItem = itemView.findViewById(R.id.group_item);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNameSub = itemView.findViewById(R.id.tv_name_sub);
            tvNameChn = itemView.findViewById(R.id.tv_name_chn);
            tvDate = itemView.findViewById(R.id.tv_date);
            cbCheck = itemView.findViewById(R.id.cb_check);
            tvGross = itemView.findViewById(R.id.tv_gross);
            tvFlag = itemView.findViewById(R.id.tv_flag);
        }
    }
}
