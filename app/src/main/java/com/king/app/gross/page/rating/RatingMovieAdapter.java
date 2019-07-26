package com.king.app.gross.page.rating;

import android.graphics.drawable.GradientDrawable;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.databinding.AdapterRatingMovieBinding;
import com.king.app.gross.page.bean.RatingMovie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:44
 */
public class RatingMovieAdapter extends BaseBindingAdapter<AdapterRatingMovieBinding, RatingMovie> {

    private long systemId;

    public void setSystemId(long systemId) {
        this.systemId = systemId;
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_rating_movie;
    }

    @Override
    protected void onBindItem(AdapterRatingMovieBinding binding, int position, RatingMovie bean) {
        binding.setBean(bean);
        binding.tvIndex.setText(String.valueOf(position + 1));
        if (systemId == RatingSystem.META) {
            double score = bean.getRating().getScore();
            if (score >= RatingSystem.META_GREEN) {
                binding.tvScore.setTextColor(binding.tvScore.getResources().getColor(R.color.meta_good));
            }
            else if (score >= RatingSystem.META_YELLOW) {
                binding.tvScore.setTextColor(binding.tvScore.getResources().getColor(R.color.meta_normal));
            }
            else {
                binding.tvScore.setTextColor(binding.tvScore.getResources().getColor(R.color.meta_bad));
            }
        }
    }
}
