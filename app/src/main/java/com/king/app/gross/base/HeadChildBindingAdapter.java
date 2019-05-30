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
 * @date: 2018/8/17 9:33
 */
public abstract class HeadChildBindingAdapter<VH extends ViewDataBinding, VI extends ViewDataBinding, H, I> extends RecyclerView.Adapter {

    private final int TYPE_HEAD = 0;
    private final int TYPE_ITEM = 1;

    protected List<Object> list;

    protected OnClickHeadListener onClickHeadListener;

    protected OnClickItemListener onClickItemListener;

    protected OnLongClickItemListener<I> onLongClickItemListener;

    public void setList(List<Object> list) {
        this.list = list;
    }

    public void setOnClickHeadListener(OnClickHeadListener onClickHeadListener) {
        this.onClickHeadListener = onClickHeadListener;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public void setOnLongClickItemListener(OnLongClickItemListener<I> onLongClickItemListener) {
        this.onLongClickItemListener = onLongClickItemListener;
    }

    protected abstract Class getItemClass();

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getClass() == getItemClass()) {
            return TYPE_ITEM;
        }
        return TYPE_HEAD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            VH binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , getHeaderRes(), parent, false);
            BindingHolder holder = new BindingHolder(binding.getRoot());
            holder.itemView.setOnClickListener(v -> onClickHead(holder.itemView, holder.getLayoutPosition(), (H) list.get(holder.getLayoutPosition())));
            return holder;
        }
        else {
            VI binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , getItemRes(), parent, false);
            BindingHolder holder = new BindingHolder(binding.getRoot());
            holder.itemView.setOnClickListener(v -> onClickItem(holder.itemView, holder.getLayoutPosition(), (I) list.get(holder.getLayoutPosition())));
            holder.itemView.setOnLongClickListener(v -> onLongClickItem(holder.itemView, holder.getLayoutPosition(), (I) list.get(holder.getLayoutPosition())));
            return holder;
        }
    }

    protected abstract int getHeaderRes();

    protected abstract int getItemRes();

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEAD) {
            VH binding = DataBindingUtil.getBinding(holder.itemView);
            onBindHead(binding, position, (H) list.get(position));
            binding.executePendingBindings();
        }
        else {
            VI binding = DataBindingUtil.getBinding(holder.itemView);
            onBindItem(binding, position, (I) list.get(position));
            binding.executePendingBindings();
        }
    }

    protected abstract void onBindHead(VH binding, int position, H head);

    protected abstract void onBindItem(VI binding, int position, I item);

    protected void onClickHead(View view, int position, H head) {
        if (onClickHeadListener != null) {
            onClickHeadListener.onClickHead(view, position, head);
        }
    }

    protected void onClickItem(View view, int position, I item) {
        if (onClickItemListener != null) {
            onClickItemListener.onClickItem(view, position, item);
        }
    }

    private boolean onLongClickItem(View view, int position, I item) {
        if (onLongClickItemListener != null) {
            return onLongClickItemListener.onLongClickItem(view, position, item);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return list == null ? 0:list.size();// 首尾分别为header和footer
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {

        public BindingHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnClickHeadListener<H> {
        void onClickHead(View view, int position, H head);
    }

    public interface OnClickItemListener<I> {
        void onClickItem(View view, int position, I item);
    }

    public interface OnLongClickItemListener<I> {
        boolean onLongClickItem(View view, int position, I item);
    }
}
