package com.king.app.gross.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/3/14 15:12
 */
public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {

    protected List<T> list;

    protected OnItemClickListener<T> onItemClickListener;

    protected OnItemLongClickListener<T> onItemLongClickListener;

    public void setList(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutRes(), parent, false);
        VH holder = newViewHolder(view);
        view.setOnClickListener(v -> {
            int position = holder.getLayoutPosition();
            onClickItem(v, position);
        });
        view.setOnLongClickListener(v -> {
            int position = holder.getLayoutPosition();
            return onLongClickItem(v, position);
        });
        return holder;
    }

    protected void onClickItem(View v, int position) {
        if (onItemClickListener != null) {
            onItemClickListener.onClickItem(v, position, list.get(position));
        }
    }

    protected boolean onLongClickItem(View v, int position) {
        if (onItemLongClickListener != null) {
            return onItemLongClickListener.onLongClickItem(v, position, list.get(position));
        }
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindView(holder, position, list.get(position));
    }

    protected abstract int getItemLayoutRes();

    protected abstract VH newViewHolder(View view);

    protected abstract void onBindView(VH holder, int position, T data);

    @Override
    public int getItemCount() {
        return list == null ? 0:list.size();
    }

    public interface OnItemClickListener<T> {
        void onClickItem(View view, int position, T data);
    }

    public interface OnItemLongClickListener<T> {
        boolean onLongClickItem(View view, int position, T data);
    }
}
