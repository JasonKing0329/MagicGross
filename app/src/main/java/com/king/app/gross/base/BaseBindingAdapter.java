package com.king.app.gross.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 16:50
 */
public abstract class BaseBindingAdapter<V extends ViewDataBinding, T> extends RecyclerView.Adapter {

    protected List<T> list;

    protected BaseRecyclerAdapter.OnItemClickListener<T> onItemClickListener;

    protected BaseRecyclerAdapter.OnItemLongClickListener<T> onItemLongClickListener;

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(BaseRecyclerAdapter.OnItemLongClickListener<T> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        V binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                , getItemLayoutRes(), parent, false);
        BindingHolder holder = new BindingHolder(binding.getRoot());
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getLayoutPosition();
            onClickItem(v, position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getLayoutPosition();
            return onLongClickItem(v, position);
        });
        return holder;
    }

    protected abstract int getItemLayoutRes();

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        V binding = DataBindingUtil.getBinding(holder.itemView);
        onBindItem(binding, position, list.get(position));
        binding.executePendingBindings();
    }

    protected abstract void onBindItem(V binding, int position, T bean);

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
    public int getItemCount() {
        return list == null ? 0:list.size();
    }

    public interface OnItemClickListener<T> {
        void onClickItem(View view, int position, T data);
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {

        public BindingHolder(View itemView) {
            super(itemView);
        }
    }
}
