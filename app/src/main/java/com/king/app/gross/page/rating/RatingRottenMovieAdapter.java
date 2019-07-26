package com.king.app.gross.page.rating;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.databinding.AdapterRatingMovieBinding;
import com.king.app.gross.databinding.AdapterRatingRottenMovieBinding;
import com.king.app.gross.page.bean.RatingMovie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:44
 */
public class RatingRottenMovieAdapter extends BaseBindingAdapter<AdapterRatingRottenMovieBinding, RatingMovie> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_rating_rotten_movie;
    }

    @Override
    protected void onBindItem(AdapterRatingRottenMovieBinding binding, int position, RatingMovie bean) {
        binding.setBean(bean);
        binding.tvIndex.setText(String.valueOf(position + 1));
        if (bean.getRottenPro() != null) {
            double score = bean.getRottenPro().getRating().getScore();
            if (score < RatingSystem.ROTTEN_SCORE_ROTTEN) {
                binding.ivRottenPro.setImageResource(R.drawable.rotten_rotten);
            }
            else {
                binding.ivRottenPro.setImageResource(R.drawable.rotten_fresh);
            }
        }
        if (bean.getRottenAud() != null) {
            double score = bean.getRottenAud().getRating().getScore();
            if (score < RatingSystem.ROTTEN_SCORE_ROTTEN) {
                binding.ivRottenAud.setImageResource(R.drawable.rotten_aud_rotten);
            }
            else {
                binding.ivRottenAud.setImageResource(R.drawable.rotten_audience);
            }
        }
    }
}
