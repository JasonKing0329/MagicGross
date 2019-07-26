package com.king.app.gross.page.adapter;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.king.app.gross.R;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.databinding.AdapterMovieBasicBinding;
import com.king.app.gross.databinding.AdapterMovieMarketItemBinding;
import com.king.app.gross.databinding.AdapterMovieMarketTopBinding;
import com.king.app.gross.page.bean.MovieBasicData;
import com.king.app.gross.page.bean.MovieMarketItem;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/17 9:33
 */
public class MovieMarketsAdapter extends RecyclerView.Adapter {

    private final int TYPE_BASIC = 0;
    private final int TYPE_HEAD = 1;
    private final int TYPE_ITEM = 2;

    protected List<Object> list;

    private OnClickItemListener onClickItemListener;
    
    private OnBasicDataListener onBasicDataListener;

    public void setList(List<Object> list) {
        this.list = list;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public void setOnBasicDataListener(OnBasicDataListener onBasicDataListener) {
        this.onBasicDataListener = onBasicDataListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof String) {
            return TYPE_HEAD;
        }
        else if (list.get(position) instanceof MovieMarketItem) {
            return TYPE_ITEM;
        }
        return TYPE_BASIC;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            AdapterMovieMarketTopBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , getHeaderRes(), parent, false);
            BindingHolder holder = new BindingHolder(binding.getRoot());
            return holder;
        }
        else if (viewType == TYPE_ITEM) {
            AdapterMovieMarketItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , getItemRes(), parent, false);
            BindingHolder holder = new BindingHolder(binding.getRoot());
            holder.itemView.setOnClickListener(v -> onClickItem(holder.itemView, holder.getLayoutPosition(), (MovieMarketItem) list.get(holder.getLayoutPosition())));
            return holder;
        }
        else {
            AdapterMovieBasicBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , getBasicRes(), parent, false);
            BindingHolder holder = new BindingHolder(binding.getRoot());
            return holder;
        }
    }

    protected int getBasicRes() {
        return R.layout.adapter_movie_basic;
    }

    protected int getHeaderRes() {
        return R.layout.adapter_movie_market_top;
    }

    protected int getItemRes() {
        return R.layout.adapter_movie_market_item;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEAD) {
            AdapterMovieMarketTopBinding binding = DataBindingUtil.getBinding(holder.itemView);
            onBindHead(binding, position, (String) list.get(position));
            binding.executePendingBindings();
        }
        else if (getItemViewType(position) == TYPE_ITEM) {
            AdapterMovieMarketItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            onBindItem(binding, position, (MovieMarketItem) list.get(position));
            binding.executePendingBindings();
        }
        else {
            AdapterMovieBasicBinding binding = DataBindingUtil.getBinding(holder.itemView);
            onBindBasic(binding, position, (MovieBasicData) list.get(position));
            binding.executePendingBindings();
        }
    }

    protected void onBindHead(AdapterMovieMarketTopBinding binding, int position, String head) {
        binding.setName(head);
    }

    protected void onBindItem(AdapterMovieMarketItemBinding binding, int position, MovieMarketItem item) {
        binding.setBean(item);
    }

    private void onBindBasic(AdapterMovieBasicBinding binding, int position, MovieBasicData data) {
        binding.setData(data);
        binding.ivEditBase.setOnClickListener(e -> onBasicDataListener.onClickEdit());
        binding.rlMojo.setOnClickListener(e -> onBasicDataListener.onClickMojoId());
        binding.rlNa.setOnClickListener(e -> onBasicDataListener.onClickGrossNa());
        binding.rlChn.setOnClickListener(e -> onBasicDataListener.onClickGrossChn());
        binding.rlMarket.setOnClickListener(e -> onBasicDataListener.onClickGrossMarket());
        binding.rlOversea.setOnClickListener(e -> onBasicDataListener.onClickGrossOversea());
        binding.rlWw.setOnClickListener(e -> onBasicDataListener.onClickGrossWorld());

        if (data.getRottenPro() != null && data.getRottenPro().getRating() != null) {
            double score = data.getRottenPro().getRating().getScore();
            if (score < RatingSystem.ROTTEN_SCORE_ROTTEN) {
                binding.ivRottenPro.setImageResource(R.drawable.rotten_rotten);
            }
            else {
                binding.ivRottenPro.setImageResource(R.drawable.rotten_fresh);
            }
        }
        if (data.getRottenAud() != null && data.getRottenAud().getRating() != null) {
            double score = data.getRottenAud().getRating().getScore();
            if (score < RatingSystem.ROTTEN_SCORE_ROTTEN) {
                binding.ivRottenAud.setImageResource(R.drawable.rotten_aud_rotten);
            }
            else {
                binding.ivRottenAud.setImageResource(R.drawable.rotten_audience);
            }
        }
        if (data.getMetaScore() != null && data.getMetaScore().getRating() != null) {
            double score = data.getMetaScore().getRating().getScore();
            GradientDrawable drawable = (GradientDrawable) binding.tvMetaScore.getBackground();
            if (score >= RatingSystem.META_GREEN) {
                drawable.setColor(binding.tvMetaScore.getResources().getColor(R.color.meta_good));
            }
            else if (score >= RatingSystem.META_YELLOW) {
                drawable.setColor(binding.tvMetaScore.getResources().getColor(R.color.meta_normal));
            }
            else {
                drawable.setColor(binding.tvMetaScore.getResources().getColor(R.color.meta_bad));
            }
            binding.tvMetaScore.setBackground(drawable);
        }
    }

    protected void onClickItem(View view, int position, MovieMarketItem item) {
        if (onClickItemListener != null) {
            onClickItemListener.onClickItem(view, position, item);
        }
    }

    public int getSpanSize(int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            return 1;
        }
        else {
            return 3;
        }
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

    public interface OnClickItemListener {
        void onClickItem(View view, int position, MovieMarketItem item);
    }
    
    public interface OnBasicDataListener {
        void onClickEdit();
        void onClickMojoId();
        void onClickGrossNa();
        void onClickGrossChn();
        void onClickGrossOversea();
        void onClickGrossWorld();
        void onClickGrossMarket();
    }
    
}
